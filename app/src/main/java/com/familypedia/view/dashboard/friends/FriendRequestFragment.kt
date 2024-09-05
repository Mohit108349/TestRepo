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
import com.familypedia.databinding.FragmentFriendRequestBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_FRIEND_REQUEST
import com.familypedia.utils.Constants.USER_ID
import com.familypedia.utils.listeners.FriendRequestsListener
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.friends.adapter.FriendRequestsAdapter
import com.familypedia.view.dashboard.profile.ViewProfileActivity
import com.familypedia.viewmodels.FriendsViewModel

import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FriendRequestFragment : Fragment(), FriendRequestsListener, FamilyPediaClickListener,
    SuccessFulListener {
    private var param1: String? = null
    private var param2: String? = null
    private var dataList = arrayListOf<RequestList>()
    private var friendsViewModel: FriendsViewModel? = null
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    var linearLayoutManager: LinearLayoutManager? = null
    private var errorType: ErrorType = ErrorType.INIT
    private var requestAdapter: FriendRequestsAdapter? = null
    private var dataFetchedFromServer=false
    private var _binding: FragmentFriendRequestBinding? = null
    private val binding get() = _binding!!
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
    ): View? {
        _binding = FragmentFriendRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.somethingWrong?.clSomthingWentWrong?.hideView()
        initializeControl()

    }

    private fun initializeControl() {
        binding?.somethingWrong?.tvTapToRetry?.text=resources.getText(R.string.tv_retry)
        initClickListeners()
        initializeAdapter()

        initViewModel()
        responseData()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
    }

    private fun initClickListeners() {
       // clSomthingWentWrong.familyPediaClickListener(this)
        binding?.somethingWrong?.tvTapToRetry?.familyPediaClickListener(this)
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            refreshData()
        }
    }

    override fun onViewClick(view: View) {
        when (view) {
           // clSomthingWentWrong -> refreshData()
            binding?.somethingWrong?.tvTapToRetry -> refreshData()
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
            FriendRequestsAdapter(requireContext(), dataList, "", FROM_FRIEND_REQUEST, this)
        binding?.rvFriendRequest?.apply {
            layoutManager = linearLayoutManager
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


    private var requestDataToBeRemoved: RequestList? = null
    override fun onRequestAccept(user: User, requestData: RequestList) {
        user._id?.let { id ->
            requestDataToBeRemoved=requestData
            acceptRejectFriendRequest(id, true)
        }
    }

    override fun onRequestReject(user: User, requestData: RequestList) {
        user._id?.let { id ->
            requestDataToBeRemoved=requestData
            acceptRejectFriendRequest(id, null)
        }
    }

    override fun onItemClick(user: User) {
        val bundle = Bundle()
        bundle.putString(FROM, FROM_FRIEND_REQUEST)
        bundle.putString(USER_ID, user._id)
        ViewProfileActivity.open(requireActivity(), bundle)
    }

    override fun onListSizeChanged(size: Int) {
        if (size==0 && dataFetchedFromServer){
            dataFetchedFromServer=!dataFetchedFromServer
            manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
        }
    }



    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        friendsViewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)
        friendsViewModel?.init()
        hitRequestsAPI(false)
    }

    private fun hitRequestsAPI(pagenate: Boolean) {
      //  clSomthingWentWrong.hideView()
        if (pagenate)
            currentPage++
        friendsViewModel?.getPendingFriendRequests(requireContext(), currentPage)
    }

    private fun acceptRejectFriendRequest(userId: String, status: Boolean?) {
        showAcceptRejectLoading(true)
        val request = AcceptRejectFriendRequest(userId, status)
        friendsViewModel?.acceptRejectFriendRequest(requireContext(), request)
    }

    private fun responseData() {
        friendsViewModel?.pendingRequestsResponseResult?.observe(viewLifecycleOwner) { response ->
            if (response.data != null) {
                dataFetchedFromServer=true
                if (currentPage == 1) {
                    dataList.clear()
                    totalPage = response.data.requests?.totalPages ?: 1
                }
                response.data.requests?.docs?.forEach { request ->
                    dataList.add(request)
                }
                requestAdapter?.notifyDataSetChanged()
                binding?.somethingWrong?.clSomthingWentWrong?.hideView()
            }

            if (currentPage == 1 && dataList.isEmpty()) {
                manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }

        friendsViewModel?.simpleResponseResult?.observe(viewLifecycleOwner, { response ->
            if (response != null) {
                binding?.clParent?.showStringSnackbar(response.message ?: "")
                if(requestDataToBeRemoved!=null){
                    dataList.remove(requestDataToBeRemoved)
                    requestAdapter?.notifyDataSetChanged()
                    requestDataToBeRemoved=null
                }else {
                    hitRequestsAPI(false)
                }
            }
            showAcceptRejectLoading(false)
        })
    }

    private fun manageDataLoadingStatus() {
        friendsViewModel?.updateLoaderStatus?.observe(viewLifecycleOwner, {
            if (it.apiName == PENDING_FRIEND_REQUEST) {
                if (it.status) {
                    binding?.somethingWrong?.clSomthingWentWrong?.hideView()
                    manageShimmer(true)
                } else {
                    manageShimmer(false)
                    showAcceptRejectLoading(false)
                }
            }
        })
    }

    private fun showAcceptRejectLoading(showProgress: Boolean) {
        if (showProgress) {
            //progressBar.show()
        } else {
           // progressBar.hide()
        }
    }


    private fun onGettingException() {
        friendsViewModel?.exception?.observe(viewLifecycleOwner) {
            if (it.apiName == PENDING_FRIEND_REQUEST) {
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

                if (errorData.apiName == PENDING_FRIEND_REQUEST) {
                    manageShimmer(false)
                    manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                }
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
                loading = true
            }
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        this.errorType = errorType
        if (currentPage == 1) {
            binding?.somethingWrong?.clSomthingWentWrong?.showView()
            binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.no_internet_connect_new
                )
            )
           // ivRefresh.showView()
           // tvTapToRetry.showView()
            if (errorType == ErrorType.NO_DATA_AVAILABLE) {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.mipmap.ic_no_friends_found))
                binding?.somethingWrong?.tvOops?.hideView()
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_friend_requests_available) , "")
                //      tvSomethingWentWrong.setTextOnTextView(getString(R.string.no) +" " +getString(R.string.friend_request), "")
               // tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_data_available), "")
            } else if (errorType == ErrorType.NO_INTERNET) { //no internet
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.no_internet_connect_new
                    )
                )
                binding?.somethingWrong?.tvOops?.showView()
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
            } else if (errorType == ErrorType.INIT) {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
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
                        requireContext(),
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
            Utility.clearAllPreferences(requireContext())
            AuthViewsHolderActivity.open(requireActivity(), null)
            requireActivity().finishAffinity()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}