package com.familypedia.view.dashboard.friends

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ActivityYourInvitedFriendsBinding
import com.familypedia.network.GET_NOTIFICATION_HISTORY
import com.familypedia.network.InvitationData
import com.familypedia.network.PENDING_FRIEND_REQUEST
import com.familypedia.utils.*
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.friends.adapter.InvitedFriendsAdapter
import com.familypedia.viewmodels.NotificationViewModel
import kotlinx.coroutines.launch

class YourInvitedFriendsActivity : AppCompatActivity(),FamilyPediaClickListener,SuccessFulListener {

    private var notificationsViewModel: NotificationViewModel? = null
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    private var invitationListData = arrayListOf<InvitationData>()
    private var errorType: ErrorType? = ErrorType.NO_DATA_AVAILABLE
    private var invitedFriendsAdapter:InvitedFriendsAdapter?=null
    private lateinit var binding: ActivityYourInvitedFriendsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = ActivityYourInvitedFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControl()
    }
    private fun initializeControl(){
        Utility.setLocale(this)
        setListeners()
        setupViewModel()
        setAdapter()

    }
    private fun setupViewModel(){
        notificationsViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        notificationsViewModel?.init()
        hitInvitationListApi(false)
        responseDataObserver()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
    }

    private fun setAdapter(){
        invitedFriendsAdapter= InvitedFriendsAdapter(this,invitationListData)
        binding?.rvInvitedUsers?.adapter=invitedFriendsAdapter
        binding?.rvInvitedUsers?.addOnScrollListener(recyclerScrollListener)
    }

    //For Paging
    private var recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //check for scroll down
                    val visibleItemCount = binding?.rvInvitedUsers?.childCount
                    val totalItemCount = binding?.rvInvitedUsers?.layoutManager?.itemCount
                    val pastVisiblesItems =
                        (binding?.rvInvitedUsers?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (loading) {
                        if ((visibleItemCount!! + pastVisiblesItems!!) >= totalItemCount!!) {
                            loading = false
                            if (currentPage < totalPage) {
                                hitInvitationListApi(true)
                            }
                        }
                    }
                }
            }
        }

    private fun hitInvitationListApi(pagenate:Boolean){
        if (pagenate)
            currentPage++
        notificationsViewModel?.getInvitationList(this,currentPage)
    }


    private fun setListeners(){
        binding?.toolbarYourInvitedFriends?.tvToolbarTitle?.text=getString(R.string.your_invited_friends)
        binding?.toolbarYourInvitedFriends?.ivBack?.familyPediaClickListener(this)
        binding.somethingWrong?.tvTapToRetry?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when(view){
            binding?.toolbarYourInvitedFriends?.ivBack->onBackPressed()
            binding.somethingWrong?.tvTapToRetry->hitInvitationListApi(false)
        }
    }


    private fun responseDataObserver() {
        notificationsViewModel?.invitationResponseResult?.observe(this, { invitationData ->
            if (invitationData != null && invitationData.data?.docs?.isNotEmpty() == true) {
                if (currentPage == 1) {
                    totalPage = invitationData.data.totalPages ?: 1
                    invitationListData.clear()
                }
                invitationListData.addAll(invitationData.data.docs)
                invitedFriendsAdapter?.notifyDataSetChanged()
                binding.somethingWrong?.clSomthingWentWrong?.hideView()
            } else {
                if (currentPage === 1 && invitationListData.isEmpty())
                    manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }

        })
    }

    private fun manageDataLoadingStatus() {
        notificationsViewModel?.updateLoaderStatus?.observe(this, {
            if (it.status) {
                binding.somethingWrong?.clSomthingWentWrong?.hideView()
                manageShimmer(true)
            } else {
                manageShimmer(false)
            }
        })
    }


    private fun onGettingException() {
        notificationsViewModel?.exception?.observe(this, {
            if (it.apiName == GET_NOTIFICATION_HISTORY) {
                if (it.message == getString(R.string.no_internet)) {
                    manageSomethingWentWrong(ErrorType.NO_INTERNET)
                } else {
                    binding?.clParent?.showStringSnackbarError(it.message)
                    manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                }
                manageShimmer(false)
            } else {
                binding?.clParent?.showStringSnackbarError(it.message)
            }
        })
    }

    private fun onGettingError() {
        notificationsViewModel?.error?.observe(this, { errorData ->
            if (errorData.status == Constants.STATUS_ACCOUNT_NOT_VERIFIED) {
                Utility.unAuthorizedInactiveUser(this, errorData.message)
                return@observe
            } else if (errorData.status == Constants.STATUS_USER_DELETED) {
                SuccessfulDialog(
                    this, Constants.FROM_ACCOUNT_DELETED,
                    this,
                    getString(R.string.account_deleted),
                    errorData.message
                ).show()
                return@observe
            } else {
                binding?.clParent?.showStringSnackbarError(errorData.message)

                if (errorData.apiName == PENDING_FRIEND_REQUEST) {
                    manageShimmer(false)
                    manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                }
            }
        })
    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            if (currentPage == 1) {
                invitationListData.clear()
                invitedFriendsAdapter?.notifyDataSetChanged()
                binding?.shimmerLay?.shimmerLayout?.showView()
                binding?.shimmerLay?.shimmerLayout?.startShimmer()
            } else {
                binding?.progressPaging?.showView()
            }
        } else {
            if (currentPage == 1) {
                binding?.shimmerLay?.shimmerLayout?.stopShimmer()
                binding?.shimmerLay?.shimmerLayout?.hideView()
            } else {
                binding?.progressPaging?.hideView()
                loading=true
            }
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        this.errorType = errorType
        if (currentPage == 1) {
            binding?.somethingWrong?.clSomthingWentWrong?.showView()
            binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.no_internet_connect_new
                )
            )
            //ivRefresh.showView()
            binding?.somethingWrong?.tvTapToRetry?.showView()
            if (errorType == ErrorType.NO_DATA_AVAILABLE) {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.mipmap.no_data_avail_new
                    )
                )
                binding?.somethingWrong?.tvOops?.hideView()
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_invited_friends_available) , "")

            } else if (errorType == ErrorType.NO_INTERNET) { //no internet
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.no_internet_connect_new
                    )
                )
                binding?.somethingWrong?.tvOops?.showView()
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
            } else if (errorType == ErrorType.INIT) {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.mipmap.ic_search_something
                    )
                )
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(
                    getString(R.string.tap_to_search_friends),
                    ""
                )
                binding?.somethingWrong?.ivRefresh?.hideView()
                binding?.somethingWrong?.tvTapToRetry?.hideView()
            } else {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.no_internet_connect_new
                    )
                )
                binding?.somethingWrong?.tvOops?.showView()
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.something_went_wrong), "")
            }
        }
    }

    override fun onUpdateSuccessfully(from: String) {
        if (from == Constants.FROM_ACCOUNT_DELETED) {
            performAccountDeleted()
        }
    }

    override fun onDismiss(from: String) {
        if (from == Constants.FROM_ACCOUNT_DELETED) {
            performAccountDeleted()
        }
    }

    private fun performAccountDeleted() {
        lifecycleScope.launch {
            Utility.clearAllPreferences(this@YourInvitedFriendsActivity)
            AuthViewsHolderActivity.open(this@YourInvitedFriendsActivity, null)
            finishAffinity()
        }
    }



    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, YourInvitedFriendsActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }
}