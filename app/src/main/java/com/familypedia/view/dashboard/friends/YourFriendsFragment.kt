package com.familypedia.view.dashboard.friends

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.FragmentYourFriendsBinding
import com.familypedia.network.AskCharacterPermissionRequest
import com.familypedia.network.SearchRequest
import com.familypedia.network.User
import com.familypedia.utils.*
import com.familypedia.utils.listeners.PermissionsListener
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.profile.ViewProfileActivity
import com.familypedia.view.dashboard.profile.permissions.adapter.RequestsAdapter
import com.familypedia.viewmodels.FriendsViewModel

import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class YourFriendsFragment : Fragment(), PermissionsListener, FamilyPediaClickListener,
    SuccessFulListener {
    private var param1: String? = null
    private var param2: String? = null
    private var dataList = arrayListOf<User>()
    private var friendsViewModel: FriendsViewModel? = null
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    var linearLayoutManager: LinearLayoutManager? = null
    private var errorType: ErrorType = ErrorType.INIT
    private var requestAdapter: RequestsAdapter? = null
    private lateinit var binding: FragmentYourFriendsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentYourFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }

    private fun initializeControl() {
        binding?.sonethingWrong?.tvTapToRetry?.text=resources.getText(R.string.tv_retry)
        initClickListeners()
        initializeAdapter()

        initViewModel()
        responseData()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
    }

    private fun initClickListeners() {
      //  clSomthingWentWrong.familyPediaClickListener(this)
        binding?.sonethingWrong?.tvTapToRetry?.familyPediaClickListener(this)
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            refreshData()
        }
    }

    override fun onViewClick(view: View) {
        when (view) {
           // clSomthingWentWrong -> refreshData()
            binding?.sonethingWrong?.tvTapToRetry -> refreshData()
        }
    }

    private fun refreshData() {
        dataList.clear()
        requestAdapter?.notifyDataSetChanged()
        hitRequestsAPI(false)
    }

    private fun initializeAdapter() {
        linearLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        requestAdapter =
            RequestsAdapter(requireContext(), dataList, "", Constants.FROM_FRIEND_LIST, this)
        binding?.rvFriends?.apply {
            layoutManager=linearLayoutManager
            adapter = requestAdapter
            addOnScrollListener(recyclerScrollListener)
        }

    }

    private var recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //check for scroll down
                    val visibleItemCount = linearLayoutManager?.childCount
                    val totalItemCount = linearLayoutManager?.itemCount
                    val pastVisiblesItems = linearLayoutManager?.findFirstVisibleItemPosition()
                    if (loading) {
                        if ((visibleItemCount!! + pastVisiblesItems!!) >= totalItemCount!!) {
                            loading = false
                            if (currentPage < totalPage) {
                                hitRequestsAPI(true)
                            }
                        }
                    }
                }
            }
        }

    override fun onRequestAccept(data: AskCharacterPermissionRequest) {

    }

    override fun onRequestReject(data: AskCharacterPermissionRequest) {

    }

    override fun onDelete(data: AskCharacterPermissionRequest) {
    }

    override fun onItemClick(user:User) {
        val bundle = Bundle()
        bundle.putString(Constants.FROM, Constants.FROM_FRIEND_REQUEST)
        bundle.putString(Constants.USER_ID, user._id)
        ViewProfileActivity.open(requireActivity(), bundle)
    }


    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        friendsViewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)
        friendsViewModel?.init()
        hitRequestsAPI(false)
    }

    private fun hitRequestsAPI(pagenate:Boolean) {
        if (pagenate)
            currentPage++
        val request=SearchRequest("",currentPage)
        friendsViewModel?.getFriendsList(requireContext(),request)
    }


    private fun responseData() {
        friendsViewModel?.friendListResponseResult?.observe(viewLifecycleOwner) { response ->
            if (response.data != null) {
                if (currentPage == 1) {
                    dataList.clear()
                    totalPage = response.data.totalPages ?: 1
                }
                response.data.docs.forEach { data ->
                    if (data.friend != null)
                        dataList.add(data.friend)
                }
                requestAdapter?.notifyDataSetChanged()
            }

            if (currentPage == 1 && dataList.isEmpty()) {
                manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }
    }

    private fun manageDataLoadingStatus() {
        friendsViewModel?.updateLoaderStatus?.observe(viewLifecycleOwner) {
            if (it.status) {
                binding?.sonethingWrong?.clSomthingWentWrong?.hideView()
                manageShimmer(true)
            } else {
                manageShimmer(false)
            }
        }
    }


    private fun onGettingException() {
        friendsViewModel?.exception?.observe(viewLifecycleOwner) {
            if (it.message == getString(R.string.no_internet)) {
                manageSomethingWentWrong(ErrorType.NO_INTERNET)
            } else {
                binding?.clParent?.showStringSnackbarError(it.message)
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
            }
            manageShimmer(false)
        }
    }

    private fun onGettingError() {
        friendsViewModel?.error?.observe(viewLifecycleOwner) { errorData ->
            if (errorData.status == Constants.STATUS_ACCOUNT_NOT_VERIFIED) {
                Utility.unAuthorizedInactiveUser(requireContext(), errorData.message)
                return@observe
            } else if (errorData.status == Constants.STATUS_USER_DELETED) {
                SuccessfulDialog(
                    requireContext(), Constants.FROM_ACCOUNT_DELETED,
                    this,
                    getString(R.string.account_deleted),
                    errorData.message
                ).show()
                return@observe
            } else {
                binding?.clParent?.showStringSnackbarError(errorData.message)
                manageShimmer(false)
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
            }
        }
    }

    //
    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            if (currentPage == 1) {
                dataList.clear()
                requestAdapter?.notifyDataSetChanged()
                binding?.shimmerLay?.shimmerLayout?.showView()
                binding?.shimmerLay?.shimmerLayout?.startShimmer()
            } else {
                binding?.progressPaging?.showView()
            }
        } else {
            if (currentPage == 1) {
                binding?.shimmerLay?.shimmerLayout?.stopShimmer()
                binding?.shimmerLay?.shimmerLayout?.hideView()
                binding?.swipeRefreshLayout?.isRefreshing = false
            } else {
                binding?.progressPaging?.hideView()
                loading=true
            }
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        this.errorType = errorType
        if (currentPage == 1) {
            binding?.sonethingWrong?.clSomthingWentWrong?.showView()
            binding?.sonethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.no_internet_connect_new
                )
            )
          //  ivRefresh.showView()
           // tvTapToRetry.showView()
            if (errorType == ErrorType.NO_DATA_AVAILABLE) {
                binding?.sonethingWrong?.tvOops?.hideView()
                binding?.sonethingWrong?.ivSomethingWentWrong?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_no_friends_found))
                //tvSomethingWentWrong.setTextOnTextView(String.format(resources.getString(R.string.not_available),getString(R.string.friends)) , "")
                binding?.sonethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_friends_avaialable) , "")

            } else if (errorType == ErrorType.NO_INTERNET) { //no internet
                binding?.sonethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.no_internet_connect_new
                    )
                )
                binding?.sonethingWrong?.tvOops?.hideView()
                binding?.sonethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
            } else if (errorType == ErrorType.INIT) {
                binding?.sonethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.mipmap.ic_search_something
                    )
                )
                binding?.sonethingWrong?.tvSomethingWentWrong?.setTextOnTextView(
                    getString(R.string.tap_to_search_friends),
                    ""
                )
                binding?.sonethingWrong?.tvOops?.hideView()
                binding?.sonethingWrong?.ivRefresh?.hideView()
                binding?.sonethingWrong?.tvTapToRetry?.hideView()
            } else {
                binding?.sonethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.no_internet_connect_new
                    )
                )
                binding?.sonethingWrong?.tvOops?.hideView()
                binding?.sonethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.something_went_wrong), "")
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
            Utility.clearAllPreferences(requireContext())
            AuthViewsHolderActivity.open(requireActivity(), null)
            requireActivity().finishAffinity()
        }
    }

}