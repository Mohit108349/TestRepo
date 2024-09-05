package com.familypedia.view.notifications

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ActivityNotificationsBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_SPLASH
import com.familypedia.utils.Constants.NOTIFICATION
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REMOVED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_ACCEPTED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_RECEIVED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_REJECTED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_CREATE_BIOGRAPHY
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_ACCEPTED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_RECEIVED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_REJECTED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_NEW_POST_CREATED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_POST_EDITED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_POST_REMOVED
import com.familypedia.utils.Constants.POST_ID
import com.familypedia.utils.listeners.NotificationListener
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.DashboardActivity
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity
import com.familypedia.view.dashboard.character.post.PostDetailActivity
import com.familypedia.view.dashboard.profile.ViewProfileActivity
import com.familypedia.view.dashboard.profile.permissions.PostPermissionsActivity
import com.familypedia.viewmodels.FriendsViewModel
import com.familypedia.viewmodels.NotificationViewModel
import com.familypedia.viewmodels.PermissionsViewModel
import com.google.gson.Gson
import io.ak1.pix.helpers.hide
import io.ak1.pix.helpers.show
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity(), FamilyPediaClickListener,
    SuccessFulListener, NotificationListener {
    private lateinit var binding: ActivityNotificationsBinding
    private var notificationsViewModel: NotificationViewModel? = null
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    private var notificationsList = arrayListOf<NotificationData>()
    private var errorType: ErrorType? = ErrorType.NO_DATA_AVAILABLE
    private var notificationAdapter: NotificationsAdapter? = null
    private var friendsViewModel: FriendsViewModel? = null
    private var permissionsViewModel: PermissionsViewModel? = null
    private var from = ""
    private var lastVisibleItemPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeControl()
    }

    private fun initializeControl() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        Utility.setLocale(this)
        from = intent.extras?.getString(FROM) ?: ""
        setupViews()
        setupListeners()
        setAdapters()
        initViewModel()
    }

    private fun setupListeners() {
        binding.toolbarNotificatoins.ivBack.familyPediaClickListener(this)
        //  clSomthingWentWrong.familyPediaClickListener(this)
        binding.somethingWrong.tvTapToRetry.familyPediaClickListener(this)
    }

    private fun setupViews() {
        binding.toolbarNotificatoins.tvToolbarTitle.text = getString(R.string.notifications)
    }

    private fun setAdapters() {
        notificationAdapter = NotificationsAdapter(this, notificationsList, this)
        binding.rvNotifications.adapter = notificationAdapter
        binding.rvNotifications.addOnScrollListener(recyclerScrollListener)
    }

    //For Paging
    private var recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItemPosition =
                    (binding.rvNotifications?.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                println("lastVisibleItemPosition: $lastVisibleItemPosition")
                markNotificationRead()

                if (dy > 0) { //check for scroll down
                    val visibleItemCount = binding.rvNotifications?.childCount
                    val totalItemCount = binding.rvNotifications?.layoutManager?.itemCount
                    val pastVisiblesItems =
                        (binding.rvNotifications?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (loading) {
                        if ((visibleItemCount!! + pastVisiblesItems!!) >= totalItemCount!!) {
                            loading = false
                            if (currentPage < totalPage) {
                                hitNotificationsApi(true)
                            }
                        }
                    }
                }
            }
        }

    override fun onViewClick(view: View) {
        when (view) {
            binding.toolbarNotificatoins.ivBack -> onBackPressed()
            // clSomthingWentWrong -> hitNotificationsApi(false)
            binding.somethingWrong.tvTapToRetry -> hitNotificationsApi(false)
        }
    }

    private var notificationDataToBeRemoved: NotificationData? = null
    override fun onRequestAccept(data: NotificationData) {
        manageRequest(data, true)
    }

    override fun onRequestReject(data: NotificationData) {
        manageRequest(data, false)
    }

    private fun manageRequest(data: NotificationData, accept: Boolean) {
        notificationDataToBeRemoved = data
        if (data.notification_type == Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_RECEIVED) {
            data.sender_user?._id?.let {
                acceptRejectFriendRequest(it, accept)
            }
        } else if (data.notification_type == Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_RECEIVED) {
            data.sender_user?._id?.let { userId ->
                data.character?._id?.let { characterId ->
                    val request = AskCharacterPermissionRequest(userId, characterId)
                    acceptDeclineRequest(accept, request)
                }
            }
        }

       notificationsList.remove(notificationDataToBeRemoved)
    }


    override fun onItemClick(data: NotificationData) {
        when (data.notification_type) {
            NOTIFICATION_TYPE_NEW_POST_CREATED ->
                navigationToPostDetail(data)
            NOTIFICATION_TYPE_CREATE_BIOGRAPHY ->
                navigateToBiographyProfile(data)
            NOTIFICATION_TYPE_POST_EDITED ->
                navigationToPostDetail(data)
            NOTIFICATION_TYPE_FRIEND_REQUEST_RECEIVED ->  //open user page
                navigateToUserProfile(data)
            NOTIFICATION_TYPE_FRIEND_REQUEST_REJECTED -> //open user page
                navigateToUserProfile(data)
            NOTIFICATION_TYPE_FRIEND_REQUEST_ACCEPTED -> //open user page
                navigateToUserProfile(data)
            NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_RECEIVED ->//permission list page
                navigateToPostPermission()
            NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_ACCEPTED -> //biography profile page
                navigateToBiographyProfile(data)
            NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_REJECTED -> // biography profile
                navigateToBiographyProfile(data)
            NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REMOVED ->  //biography profile
                navigateToBiographyProfile(data)
            NOTIFICATION_TYPE_POST_REMOVED -> {

            }
        }

    }

    private fun navigateToUserProfile(data: NotificationData) {
        val bundle = Bundle()
        bundle.putString(FROM, NOTIFICATION)
        bundle.putString(Constants.USER_ID, data.sender_user?._id)
        ViewProfileActivity.open(this, bundle)
    }

    private fun navigateToBiographyProfile(data: NotificationData) {
        val bundle = Bundle()
        bundle.putString(FROM, NOTIFICATION)
        bundle.putString(Constants.USER_NAME, data.character?.name)
        Log.d(">>>", "${data.character?.name}")
        bundle.putString(Constants.USER_ID, data.character?._id)
        Log.d("-->>>", "${ data.character?._id}")
        bundle.putString(Constants.PROFILE_PIC, data.character?.profile_pic)
        bundle.putSerializable(Constants.PERMITTED_USERS, data.character)
        AboutCharacterActivity.open(this, bundle)
    }

    private fun navigationToPostDetail(data: NotificationData) {
        if (data.postId != null && data.postId.isNotEmpty()) {
            val bundle = Bundle()
            bundle.putString(FROM, NOTIFICATION)
            bundle.putString(POST_ID, data.postId)
            PostDetailActivity.open(this, bundle)
        }
    }


    private fun navigateToPostPermission() {
        val bundle = Bundle()
        bundle.putString(FROM, NOTIFICATION)
        PostPermissionsActivity.open(this, bundle)
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        notificationsViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        notificationsViewModel?.init()
        friendsViewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)
        friendsViewModel?.init()
        permissionsViewModel = ViewModelProvider(this).get(PermissionsViewModel::class.java)
        permissionsViewModel?.init()
        hitNotificationsApi(false)
        responseDataObserver()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
    }

    private fun hitNotificationsApi(pagenate: Boolean) {
        if (pagenate) {
            currentPage++
        }
        notificationsViewModel?.getNotifications(this, currentPage)
    }

    private fun acceptRejectFriendRequest(userId: String, status: Boolean?) {
        //showAcceptRejectLoading(true)
        val request = AcceptRejectFriendRequest(userId, status)
        friendsViewModel?.acceptRejectFriendRequest(this, request)
    }


    private fun acceptDeclineRequest(
        acceptRequest: Boolean,
        request: AskCharacterPermissionRequest
    ) {
        permissionsViewModel?.acceptDeclinePermission(this, acceptRequest, request)
    }

    val readNotificationIds = arrayListOf<String>()
    private fun readNofications() {
        notificationsList.forEachIndexed { index, notificationData ->
            //removing friendrequest and biographyrequest bcz we want that notification in that list for last
            if (index <= lastVisibleItemPosition) {
                if (notificationData.notification_type!="friendRequestReceive" && notificationData.notification_type!="biographyPermissionRequestRecieved"){
                    readNotificationIds.add(notificationData._id ?: "")
                }
            }
        }
        if (readNotificationIds.isNotEmpty()) {
            val readNotificationRequest = ReadNotificationRequest(readNotificationIds)
            println("noti data:: " + Gson().toJson(readNotificationRequest))
            notificationsViewModel?.readNotification(this, readNotificationRequest)
        }
    }

    private fun markNotificationRead() {
        //lifecycleScope.launch {
            notificationsList.forEachIndexed { index, notificationData ->
                if (index <= lastVisibleItemPosition) {
                    if (notificationData.read_status == false) {

                        notificationData.read_status=true
                        //delay(100)
                        notificationAdapter?.notifyDataSetChanged()
                    }
                }
          //  }
/*
            delay(2000)
            notificationsList.forEach { item ->
                item.read_status = true
            }
            notificationAdapter?.notifyDataSetChanged()
*/
        }
    }

    private fun responseDataObserver() {
        notificationsViewModel?.notificationResponseResult?.observe(this) { notificationData ->
            Log.v(">>>11", ""+notificationData)
            if (notificationData != null && notificationData.data?.docs?.isNotEmpty() == true) {
                Log.v(">>>", ""+notificationData)
                if (currentPage == 1) {
                    totalPage = notificationData.data.totalPages ?: 1
                    notificationsList.clear()
                }
                notificationsList.addAll(notificationData.data.docs)
                notificationAdapter?.notifyDataSetChanged()
                binding.somethingWrong.clSomthingWentWrong.hideView()

            } else {
                if (currentPage == 1 && notificationsList.isEmpty())
                    manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }

        friendsViewModel?.simpleResponseResult?.observe(this) { response ->
            if (response != null) {
                removeData(response)
            }
        }
        permissionsViewModel?.simpleResponseResult?.observe(this) { response ->
            if (response != null) {
                removeData(response)
            }
        }

        notificationsViewModel?.simpleResponseResult?.observe(this) { response ->
            if (response != null) {
                notificationsList.forEach { item ->
                    item.read_status = true
                }
                notificationAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun removeData(response: SimpleResponseModel) {
        //notificationsList.remove(notificationDataToBeRemoved)
        binding.clParent.showStringSnackbar(response.message ?: "")
        notificationAdapter?.notifyDataSetChanged()
    }

    private fun manageDataLoadingStatus() {
        notificationsViewModel?.updateLoaderStatus?.observe(this) { loadingData ->
            if (loadingData.apiName == GET_NOTIFICATION_HISTORY) {
                if (loadingData.status) {
                    binding.somethingWrong.clSomthingWentWrong.hideView()
                    binding.progBar.progressBar.hide()
                    manageShimmer(true)
                } else {
                    manageShimmer(false)
                }
            } else {
                manageShimmer(false)
                if (loadingData.apiName != READ_NOTIFICATION) {
                    if (loadingData.status) {
                        binding.progBar.progressBar.show()
                    } else {
                        binding.progBar.progressBar.hide()
                    }
                }
            }
        }
        permissionsViewModel?.updateLoaderStatus?.observe(this) { loadingData ->
            if (loadingData.status) {
                binding.progBar.progressBar.show()
            } else {
                binding.progBar.progressBar.hide()
            }
        }
        friendsViewModel?.updateLoaderStatus?.observe(this) { loadingData ->
            if (loadingData.status) {
                binding.progBar.progressBar.show()
            } else {
                binding.progBar.progressBar.hide()
            }
        }
    }

    private fun onGettingException() {
        notificationsViewModel?.exception?.observe(this) {
            if (it.apiName == GET_NOTIFICATION_HISTORY) {
                if (it.message == getString(R.string.no_internet)) {
                    manageSomethingWentWrong(ErrorType.NO_INTERNET)
                } else {
                    binding.clParent?.showStringSnackbarError(it.message)
                    manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                }
                manageShimmer(false)
            } else {
                binding.clParent?.showStringSnackbarError(it.message)
            }
        }
    }

    private fun onGettingError() {
        notificationsViewModel?.error?.observe(this) { errorData ->
            if (errorData.apiName == GET_NOTIFICATION_HISTORY) {
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
                    binding.clParent?.showStringSnackbarError(errorData.message)

                    if (errorData.apiName == PENDING_FRIEND_REQUEST) {
                        manageShimmer(false)
                        manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                    }
                }
            } else {
                binding.clParent?.showStringSnackbarError(errorData.message)

            }
        }
    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            if (currentPage == 1) {
                notificationsList.clear()
                notificationAdapter?.notifyDataSetChanged()
                binding.shimmerLay.shimmerLayout.showView()
                binding.shimmerLay.shimmerLayout.startShimmer()
            } else {
                binding.progressPaging.showView()
            }
        } else {
            if (currentPage == 1) {
                binding.shimmerLay.shimmerLayout.stopShimmer()
                binding.shimmerLay.shimmerLayout.hideView()
            } else {
                binding.progressPaging.hideView()
                loading = true
            }
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        this.errorType = errorType
        if (currentPage == 1) {
            binding.somethingWrong.clSomthingWentWrong.showView()
            binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.no_internet_connect_new
                )
            )
            // ivRefresh.showView()
            binding.somethingWrong.tvTapToRetry.showView()
            if (errorType == ErrorType.NO_DATA_AVAILABLE) {
                binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.mipmap.ic_no_notifications
                    )
                )
                binding.somethingWrong.tvOops.hideView()
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(
                    getString(R.string.no_notifications_available),
                    ""
                )

                //  tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_data_available), "")
            } else if (errorType == ErrorType.NO_INTERNET) { //no internet
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_internet), "")
                binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.no_internet_connect_new
                    )
                )
            } else if (errorType == ErrorType.INIT) {
                binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.mipmap.ic_search_something
                    )
                )
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(
                    getString(R.string.tap_to_search_friends),
                    ""
                )
                binding.somethingWrong.ivRefresh.hideView()
                binding.somethingWrong.tvTapToRetry.hideView()
            } else {
                binding.somethingWrong. ivSomethingWentWrong.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.no_internet_connect_new
                    )
                )
                binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.something_went_wrong), "")
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
            Utility.clearAllPreferences(this@NotificationsActivity)
            AuthViewsHolderActivity.open(this@NotificationsActivity, null)
            finishAffinity()
        }
    }

    companion object {
        var isNotification =false
        fun open(currActivity: Activity, bundle: Bundle?) {
            val intent = Intent(currActivity, NotificationsActivity::class.java)
            bundle?.let {
                intent.putExtras(bundle)
            }
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        if (isNotification) {
            DashboardActivity.open(this, bundleOf())
        } else {

        if (from == NOTIFICATION) {
            println(":::-->>>$from")
            val bundle = Bundle()
            bundle.putString(FROM, FROM_SPLASH)
            finish()
            DashboardActivity.open(this, bundle)
        } else {
            super.onBackPressed()
            this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
        }
    }
    }


    override fun onStop() {
        readNofications()
        super.onStop()
    }
}