package com.familypedia.view.dashboard.friends

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ActivitySearchFriendsBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.listeners.RecentSearchesListener
import com.familypedia.utils.listeners.UserListener
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.friends.adapter.UserAdapter
import com.familypedia.view.dashboard.search.RecentSearchAdapter
import com.familypedia.viewmodels.FriendsViewModel

import kotlinx.coroutines.launch
import java.util.*


class SearchFriendsActivity : AppCompatActivity(), FamilyPediaClickListener, SuccessFulListener,
    UserListener, RecentSearchesListener {

    private var usersAdapter: UserAdapter? = null
    private var friendsViewModel: FriendsViewModel? = null
    private var userList = arrayListOf<User>()
    private var searchText = ""
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    var linearLayoutManager: LinearLayoutManager? = null
    private var errorType: ErrorType = ErrorType.INIT
    private var  recentSearchesList= arrayListOf<String?>()
    private var timer: Timer = Timer()
    private val DELAY: Long = 1500 // Milliseconds
    private lateinit var binding: ActivitySearchFriendsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        Utility.setLocale(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySearchFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControl()
    }

    private fun initializeControl() {
        initClickListeners()
        initViews()
        setAdapter()
        initViewModel()
        responseData()
        searchTextListener()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
       // manageSomethingWentWrong(ErrorType.INIT)
        friedRequestLiveData.observe(this) { friendStatus ->
            userList.forEach { user ->
                if (user._id == friendStatus.userId) {
                    if (friendStatus.type== FRIEND_STATUS_ENUM.REQUEST_SENT) {
                        user.isPending = 1
                    }else if (friendStatus.type== FRIEND_STATUS_ENUM.ACCEPT_REJECT){
                        if (friendStatus.status){ //accepted
                            user.isFriend=1
                        }else{ //rejected
                            user.isFriend=0

                        }
                    }
                }
            }
            usersAdapter?.notifyDataSetChanged()

        }
    }

    private fun initViews() {
        binding?.toolbarAddNewPosListingt?.tvToolbarTitle?.text = getString(R.string.search_friends)
    }

    private fun initClickListeners() {
        binding?.toolbarAddNewPosListingt?.ivBack?.familyPediaClickListener(this)
        binding?.somethingWrong?.tvTapToRetry?.familyPediaClickListener(this)
        binding?.ivSearch?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.toolbarAddNewPosListingt?.ivBack -> onBackPressed()
            binding?.ivSearch -> {
                searchText=binding?.etSearch?.text.toString()
                if (searchText.isNotEmpty()) {
                    currentPage = 1
                    hitSearchUserAPI(searchText, false)
                }
            }
            binding?.somethingWrong?.tvTapToRetry -> {
                if (errorType == ErrorType.INIT) {
                    binding?.etSearch?.requestFocus()
                    showKeyboard(this)
                } else {
                    hitSearchUserAPI(searchText, false)
                }

            }

        }
    }

    private fun searchTextListener() {
       /* etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            searchText = p0.toString()
                            if (searchText.isNotEmpty()) {
                                currentPage = 1
                                hitSearchUserAPI(searchText, false)
                            }
                        }
                    },
                    DELAY
                )
            }
        })*/

        binding?.etSearch?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchText=binding?.etSearch?.text.toString()
                if (searchText.isNotEmpty()) {
                    currentPage = 1
                    hitSearchUserAPI(searchText, false)
                }

                return@OnEditorActionListener true
            }
            false
        })

/*
        etSearch.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= etSearch.getRight() - etSearch.getCompoundDrawables()
                        .get(DRAWABLE_RIGHT).getBounds().width()
                ) {
                    searchText=etSearch.text.toString()
                    if (searchText.isNotEmpty()) {
                        currentPage = 1
                        hitSearchUserAPI(searchText, false)
                    }
                    return@OnTouchListener true
                }
            }
            false
        })*/

    }

    private fun setAdapter() {
        linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        usersAdapter = UserAdapter(
            this,
            userList,
            this
        )
        binding?.rvUserList?.adapter = usersAdapter
        binding?.rvUserList?.layoutManager = linearLayoutManager
        binding?.rvUserList?.addOnScrollListener(recyclerScrollListener)

        //recentSearches
        Utility.getRecentSearchesUser(this)?.let {
            recentSearchesList.addAll(it)
        }
        if (recentSearchesList != null && recentSearchesList.isNotEmpty()) {
            binding?.llRecentSearches?.showView()
            recentSearchesList?.reverse()
            val recentSearchAdapter = RecentSearchAdapter(this, recentSearchesList, this)
            binding?.rvRecentSearches?.adapter = recentSearchAdapter
        }else{
            binding?.llRecentSearches?.hideView()
            manageSomethingWentWrong(ErrorType.INIT)
        }
    }

    //For Paging
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
                                hitSearchUserAPI(searchText, true)
                            }
                            loading = true
                        }
                    }
                }
            }
        }

    override fun sendFriendRequest(user: User) {
        user._id?.let {
            user.isPending=1
            usersAdapter?.notifyDataSetChanged()
            hitSendFriendRequest(it)
        }
    }

    override fun reportUser(user_id: String) {

    }

    override fun blockUser(user_id: String) {
    }

    override fun unBlockUser(user_id: String) {
    }

    override fun acceptRequest(user: User) {
        user._id?.let {
            user.isRecived=0
            user.isFriend = 1
            usersAdapter?.notifyDataSetChanged()
            val request = AcceptRejectFriendRequest(user._id!!, true)
            friendsViewModel?.acceptRejectFriendRequest(this, request)
        }
    }

    override fun rejectRequest(user: User) {
        user._id?.let {
            user.isRecived=0
            usersAdapter?.notifyDataSetChanged()
            val request = AcceptRejectFriendRequest(user._id!!, false)
            friendsViewModel?.acceptRejectFriendRequest(this, request)
        }
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        friendsViewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)
        friendsViewModel?.init()
    }

    private fun hitSearchUserAPI(searchText: String, pagenate: Boolean) {
        this.hideKeyboard(this)
        saveSearchString()
        if (pagenate) {
            currentPage++
        }
        val request = SearchRequest(searchText.trim(), currentPage)
        friendsViewModel?.searchFriends(this, request)
    }

    private fun hitSendFriendRequest(userId: String) {
        friendsViewModel?.sendFriendRequest(this, userId)
    }

    private fun responseData() {
        friendsViewModel?.userListResponseResult?.observe(
            this
        ) { userResponse ->
            if (userResponse?.data?.user != null && userResponse.data.user.isNotEmpty()) {
                //tvSelectCharacter?.showView()
                if (currentPage == 1) {
                    totalPage = userResponse.data.totalPages ?: 1
                    userList.clear()
                }
                userList.addAll(userResponse?.data?.user)
                usersAdapter?.notifyDataSetChanged()
                binding?.somethingWrong?.clSomthingWentWrong?.hideView()

            } else {
                manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }
        friendsViewModel?.simpleResponseResult?.observe(this) { response ->
            if (response.apiName == SEND_FRIEND_REQUEST) {
                binding?.clParent?.showStringSnackbar(response.message ?: "")
            }
            if (response.apiName == ACCEPT_REJECT_FRIEND_REQUEST) {
                binding?.clParent?.showStringSnackbar(response.message ?: "")
            }

        }
    }

    private fun manageDataLoadingStatus() {
        friendsViewModel?.updateLoaderStatus?.observe(this) { loadingData ->
            if (loadingData.apiName == SEARCH_FRIENDS) {
                if (loadingData.status) {
                    binding?.somethingWrong?.clSomthingWentWrong?.hideView()
                    manageShimmer(true)
                    //tvSelectCharacter?.hideView()
                } else {
                    manageShimmer(false)
                }
            }

        }
    }

    private fun onGettingException() {
        friendsViewModel?.exception?.observe(this) {
            if (it.apiName == SEARCH_FRIENDS) {
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
        friendsViewModel?.error?.observe(this) { errorData ->
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
                if (errorData.apiName == SEARCH_FRIENDS) {
                    manageShimmer(false)
                    manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                }
                binding?.clParent?.showStringSnackbarError(errorData.message)
            }
        }
    }

    private fun manageShimmer(showShimmer: Boolean) {
        /*if (showShimmer) {
            shimmerLayout.showView()
            shimmerLayout.startShimmer()
        } else {
            shimmerLayout.stopShimmer()
            shimmerLayout.hideView()
            swipeRefreshLayout.isRefreshing = false
        }*/
        if (showShimmer) {
            binding?.llRecentSearches?.hideView()
            if (currentPage == 1) {
                userList.clear()
                usersAdapter?.notifyDataSetChanged()
                binding?.progressLoading?.showView()
            } else {
                binding?.progressPaging?.showView()
            }
        } else {
            if (currentPage == 1)
                binding?.progressLoading?.hideView()
            else
                binding?.progressPaging?.hideView()
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        clearData()
        this.errorType = errorType
        if (currentPage == 1) {
            binding?.somethingWrong?.clSomthingWentWrong?.showView()
            binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.no_internet_connect_new
                )
            )
            binding?.somethingWrong?.tvOops?.visibility = View.VISIBLE
            binding?.somethingWrong?.tvOops?.setTextOnTextView(getString(R.string.tv_oops), "")
           // ivRefresh.showView()
            binding?.somethingWrong?.tvTapToRetry?.showView()
            if (errorType == ErrorType.NO_DATA_AVAILABLE) {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.no_data_avail_new))
                binding?.somethingWrong?.tvOops?.visibility = View.GONE
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(String.format(resources.getString(R.string.not_available),getString(R.string.tv_user)) , "")
            } else if (errorType == ErrorType.NO_INTERNET) { //no
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                       this,
                        R.drawable.no_internet_connect_new
                    )
                )
                binding?.somethingWrong?.tvOops?.visibility = View.VISIBLE
                binding?.somethingWrong?.tvOops?.setTextOnTextView(getString(R.string.tv_oops), "")
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
                binding?.somethingWrong?.tvOops?.visibility = View.GONE
                binding?.somethingWrong?.ivRefresh?.hideView()
                binding?.somethingWrong?.tvTapToRetry?.hideView()
            } else {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                     this,
                        R.drawable.no_internet_connect_new
                    )
                )
                binding?.somethingWrong?.tvOops?.visibility = View.VISIBLE
                binding?.somethingWrong?.tvOops?.setTextOnTextView(getString(R.string.tv_oops), "")
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
            Utility.clearAllPreferences(this@SearchFriendsActivity)
            AuthViewsHolderActivity.open(this@SearchFriendsActivity, null)
            finishAffinity()
        }
    }

    private fun clearData(){
        userList.clear()
        usersAdapter?.notifyDataSetChanged()
    }

    companion object {
        val friedRequestLiveData=MutableLiveData<FriendStatus>()
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, SearchFriendsActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    private fun saveSearchString(){
        if (!recentSearchesList.contains(searchText)) {
            Utility.saveRecentSearchesUser(this, searchText)
        }else{
            recentSearchesList.remove(searchText)
            recentSearchesList.add(0, searchText)
            recentSearchesList.reverse()
            Utility.saveRecentUser(this,recentSearchesList)
        }
    }
    override fun onRecentSearchClick(searchString: String) {
        binding?.etSearch?.setText(searchString)
        searchText=binding?.etSearch?.text.toString()
        if (searchText.isNotEmpty()) {
            currentPage = 1
            hitSearchUserAPI(searchText, false)
        }
    }
}