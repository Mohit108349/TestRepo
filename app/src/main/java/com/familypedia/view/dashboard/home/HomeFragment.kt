package com.familypedia.view.dashboard.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.FragmentHomeBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.Constants.CHARACTER_DATA
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_DELETE_POST
import com.familypedia.utils.Constants.FROM_EDIT_POST
import com.familypedia.utils.Constants.FROM_HOME
import com.familypedia.utils.Constants.POST_DATA
import com.familypedia.utils.Constants.SEE_MORE
import com.familypedia.utils.listeners.PostsListener
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.character.addCharacter.AddCharacterListingActivity
import com.familypedia.view.dashboard.character.post.AddNewPostActivity
import com.familypedia.view.dashboard.character.post.AddNewPostListingActivity
import com.familypedia.view.dashboard.home.adapter.HomeCharactersAdapter
import com.familypedia.view.dashboard.home.adapter.HomePostsAdapter
import com.familypedia.view.notifications.NotificationsActivity
import com.familypedia.viewmodels.CharacterViewModel
import com.familypedia.viewmodels.NotificationViewModel

import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment(), FamilyPediaClickListener, SuccessFulListener, PostsListener,
    ConfirmationDialogListener, GestureDetector.OnGestureListener {
    private var param1: String? = null
    private var param2: String? = null
    private var characterViewModel: CharacterViewModel? = null
    private var notificationViewModel: NotificationViewModel? = null
    private var postsList = arrayListOf<PostData>()
    private var homePostsAdapter: HomePostsAdapter? = null
    var deletePostDialog: ConfirmationDialog? = null
    var reportPostDialog: ReportPostDialog? = null
    var postId = ""
    var deleteData: PostData? = null
    private var favCharactersList = arrayListOf<CharacterData>()

    private var favCharactersAdapter: HomeCharactersAdapter? = null
    private var searchText = ""
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    private var postsLayoutManager: LinearLayoutManager? = null
    private var timer: Timer = Timer()
    private val DELAY: Long = 1500 // Milliseconds
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var timerRunnable: Runnable? = null
    private var timerHandler: Handler? = null
    private var SECOND_IN_MILLI: Long = 9000
    private var action_type = ""

    override fun onResume() {
        super.onResume()
        startTimer()
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    private fun startTimer() {
        hitNotificationCountAPI()
        timerHandler = Handler(Looper.getMainLooper())
        timerRunnable = object : Runnable {
            override fun run() {
                hitNotificationCountAPI()
                timerHandler?.postDelayed(this, SECOND_IN_MILLI)
            }
        }
        timerRunnable?.let {
            timerHandler?.postDelayed(it, SECOND_IN_MILLI)
        }
    }

    private fun stopTimer() {
        timerRunnable?.let {
            timerHandler?.removeCallbacks(it)
        }
    }

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
    ): View?{
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.root.setOnTouchListener { v, event ->
            if (gestureDetector.onTouchEvent(event)) {
                true
            } else {
                binding?.root!!.onTouchEvent(event)
            }
/*
            if (event.action == MotionEvent.ACTION_MOVE) {
                //do something
            }
            true*/
        }

        return binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }

    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    private fun initializeControl() {
        Utility.setLocale(requireActivity())
        gestureDetector = GestureDetector(this)

        viewClickListeners()
        initializeAdapter()
        initViewModel()
        responseData()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
        searchTextListener()
    }


    private fun initializeAdapter() {
        //Fav Characters
        favCharactersAdapter = HomeCharactersAdapter(requireActivity(), favCharactersList)
        binding?.rvCharactersList?.adapter = favCharactersAdapter
        //Posts
        val dividerItemDecoration =
            DividerItemDecoration(binding?.rvPosts?.context, LinearLayoutManager.VERTICAL)
        postsLayoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding?.rvPosts?.addItemDecoration(dividerItemDecoration)
        homePostsAdapter = HomePostsAdapter(requireActivity(), this, postsList, FROM_HOME, binding?.rvPosts)
        binding?.rvPosts?.adapter = homePostsAdapter
        binding?.rvPosts?.layoutManager = postsLayoutManager
        binding?.rvPosts?.addOnScrollListener(recyclerScrollListener)

        binding?.rvPosts?.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                //gestureDetector.onTouchEvent(e)
                println("events:: ${e.action}")

                when (e.action) {
                    /*MotionEvent.ACTION_MOVE ->
                        rv.requestDisallowInterceptTouchEvent(true)*/

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_DOWN -> {
                    }

                    else -> {
                        println("events:: else")
                        //rv.requestDisallowInterceptTouchEvent(true)
                    }
                }
                /* if (Math.abs(e.getX() - preX) > Math.abs(e.getY() - preY)) {
                     rv.requestDisallowInterceptTouchEvent(true)
                 } else if (Math.abs(e.getY() - preY) > Y_BUFFER) {
                     rv.requestDisallowInterceptTouchEvent(false)
                 }

                 preX = e.getX();
                 preY = e.getY();*/


                return false
            }
        })
    }


    //For Paginghome
    private var recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //check for scroll down
                    val visibleItemCount = postsLayoutManager?.childCount
                    val totalItemCount = postsLayoutManager?.itemCount
                    val pastVisiblesItems = postsLayoutManager?.findFirstVisibleItemPosition()
                    if (loading) {
                        if ((visibleItemCount!! + pastVisiblesItems!!) >= totalItemCount!!) {
                            loading = false
                            if (currentPage < totalPage) {
                                hitHomePostsAPI(searchText, true)
                            }
                        }
                    }
                }
            }
        }

    private fun viewClickListeners() {
        binding?.btnAddPost?.familyPediaClickListener(this)
        binding?.ivNotification?.familyPediaClickListener(this)
        //clSomthingWentWrong.familyPediaClickListener(this)
        binding?.someThingWrong?.tvTapToRetry?.familyPediaClickListener(this)
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            refreshData()
        }
    }

    private fun searchTextListener() {
        binding?.etSearch?.addTextChangedListener(object : TextWatcher {
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
                            if (searchText.isEmpty()) {
                                currentPage = 1
                                hitHomePostsAPI(searchText, false)
                            }
                        }
                    },
                    DELAY
                )
            }
        })
        binding?.etSearch?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchText = binding?.etSearch?.text.toString()
                if (searchText.isNotEmpty()) {
                    currentPage = 1
                    hitHomePostsAPI(searchText, false)
                }
                return@OnEditorActionListener true
            }
            false
        })
        binding?.etSearch?.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding?.etSearch?.getRight()!! - binding?.etSearch?.getCompoundDrawables()
                        ?.get(DRAWABLE_RIGHT)?.getBounds()?.width()!!
                ) {
                    searchText = binding?.etSearch?.text.toString()
                    if (searchText.isNotEmpty()) {
                        currentPage = 1
                        hitHomePostsAPI(searchText, false)
                    }
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.btnAddPost -> AddNewPostListingActivity.open(requireActivity())
            binding?.ivNotification -> NotificationsActivity.open(requireActivity(), null)
            // clSomthingWentWrong -> refreshData()
            binding?.someThingWrong?.tvTapToRetry -> refreshData()
        }
    }

    private fun refreshData() {
        initializeAdapter()
        currentPage = 1
        postsList.clear()
        homePostsAdapter?.notifyDataSetChanged()
        hitHomePostsAPI("", false)
        Handler(Looper.getMainLooper()).postDelayed({
            hitFavCharactersListAPI()
        }, 2000)
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        currentPage = 1
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        notificationViewModel?.init()


        hitHomePostsAPI("", false)
        Handler(Looper.getMainLooper()).postDelayed({
            hitFavCharactersListAPI()
        }, 2000)
    }

    private fun hitHomePostsAPI(searchText: String, pagenate: Boolean) {
        requireActivity().hideKeyboard(requireActivity())
        if (pagenate) {
            currentPage++
        }
        val searchRequest = SearchRequest(searchText.trim(), currentPage)
        characterViewModel?.getHomePosts(requireActivity(), searchRequest)
    }

    private fun deletePostAPI(postId: String) {
        characterViewModel?.deletePost(postId, requireActivity())
    }

    private fun reportPostAPI(postId: String) {
        characterViewModel?.reportPost(ReportPostRequest(postId), requireActivity())
    }

    private fun hitFavCharactersListAPI() {
        val searchRequest = SearchRequest("", 1)
        characterViewModel?.getBookmarkList(requireActivity(), searchRequest)
    }


    private fun hitNotificationCountAPI() {
        notificationViewModel?.getUnreadNotifications(requireActivity())
    }

    private fun responseData() {
        characterViewModel?.postsListResult?.observe(viewLifecycleOwner) { response ->
            if (response?.data?.data != null && response.data.data?.isNotEmpty() == true) {
                if (currentPage == 1) {
                    totalPage = response.data.totalPages ?: 1
                    postsList.clear()
                }
                postsList.addAll(response.data.data)
                homePostsAdapter?.notifyDataSetChanged()
                binding?.someThingWrong?.clSomthingWentWrong?.hideView()
                manageShimmer(false)
            } else {
                if (currentPage == 1)
                    manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }

        characterViewModel?.simpleResponseResult?.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.apiName == DELETE_POST) {
                    postsList.remove(deleteData)
                    homePostsAdapter?.notifyDataSetChanged()
                }
                if (response.apiName == REPORT_POST) {
                    SuccessfulDialog(
                        requireActivity(), Constants.FROM_REPORT_POST,
                        this,
                        getString(R.string.post_reported),
                        response.message.toString()
                    ).show()
                    return@observe
                }
                binding?.clParent?.showStringSnackbar(response.message ?: "")
            }
        }
        notificationViewModel?.unreadNotificationContResponseResult?.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                response.data?.unread_notification_count?.let { count ->
                    if (count > 0) {
                        binding?.tvMsgCount?.showView()
                        if (count > 9) {
                            binding?.tvMsgCount?.setTextOnTextView("9+", "")
                        } else {
                            binding?.tvMsgCount?.setTextOnTextView(count.toString(), "")
                        }
                    } else {
                        binding?.tvMsgCount?.hideView()
                    }
                }
            }
        }
        notificationLiveData.observe(requireActivity()) { readStatus ->
            if (readStatus) {
                try {
                    binding?.tvMsgCount?.setTextOnTextView("", "")
                    binding?.tvMsgCount?.hideView()
                } catch (e: Exception) {

                }
            }
        }

        characterViewModel?.characterListResponseResult?.observe(
            requireActivity()
        ) { characterResponse ->
            if (characterResponse?.data?.data != null && characterResponse.data.data.isNotEmpty()) {
                binding?.tvFavouriteCharacter?.showView()
                binding?.divider?.showView()
                favCharactersList.clear()
                characterResponse.data.data.forEachIndexed { index, characterData ->
                    if (index < 10)
                        favCharactersList.add(characterData)
                }
                if (favCharactersList.size == 10) {
                    val characSeeMore = CharacterData(
                        null,
                        SEE_MORE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                    favCharactersList.add(characSeeMore) //for more
                }
                favCharactersAdapter?.notifyDataSetChanged()
            } else {
                binding?.tvFavouriteCharacter?.hideView()
                binding?.rvCharactersList?.hideView()

                //manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(viewLifecycleOwner) {
            if (it.apiName == HOME_POST_LIST) {
                if (it.status) {
                    binding?.someThingWrong?.clSomthingWentWrong?.hideView()
                    manageShimmer(true)
                } else {
                    manageShimmer(false)
                }
            }
        }
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(viewLifecycleOwner) { exceptionData ->
            if (exceptionData.apiName == HOME_POST_LIST) {
                if (exceptionData.message == getString(R.string.no_internet)) {
                    if (currentPage == 1)
                        manageSomethingWentWrong(ErrorType.NO_INTERNET)
                } else {
                    binding?.clParent?.showStringSnackbarError(exceptionData.message)
                    if (currentPage == 1)
                        manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                }
                manageShimmer(false)
            } else if (exceptionData.apiName == DELETE_POST) {
                binding?.clParent?.showStringSnackbarError(exceptionData.message)
            }
        }
    }

    private fun onGettingError() {
        characterViewModel?.error?.observe(viewLifecycleOwner) { errorData ->
            if (errorData.status == Constants.STATUS_ACCOUNT_NOT_VERIFIED) {
                Utility.unAuthorizedInactiveUser(requireActivity(), errorData.message)
                return@observe
            } else if (errorData.status == Constants.STATUS_USER_DELETED) {
                SuccessfulDialog(
                    requireActivity(), Constants.FROM_ACCOUNT_DELETED,
                    this,
                    getString(R.string.account_deleted),
                    errorData.message
                ).show()
                return@observe
            } else {
                if (errorData.apiName == HOME_POST_LIST) {
                    binding?.clParent?.showStringSnackbarError(errorData.message)
                    manageShimmer(false)
                    if (currentPage == 1)
                        manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                }
            }
        }
    }


    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            if (currentPage == 1) {
                postsList.clear()
                homePostsAdapter?.notifyDataSetChanged()
                binding?.shimmerLay?.shimmerLayoutPostList?.showView()
                binding?.shimmerLay?.shimmerLayoutPostList?.startShimmer()
            } else {
                binding?.progressPaging?.showView()
            }
        } else {
            if (currentPage == 1) {
                binding?.shimmerLay?.shimmerLayoutPostList?.stopShimmer()
                binding?.shimmerLay?.shimmerLayoutPostList?.hideView()
                binding?.swipeRefreshLayout?.isRefreshing = false
            } else {
                binding?.progressPaging?.hideView()
                loading = true
            }
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        binding?.someThingWrong?.clSomthingWentWrong?.showView()
        if (errorType == ErrorType.NO_DATA_AVAILABLE) {
            //val isFirstLogin = getPreferencesBoolean(requireActivity(), IS_FIRST_LOGIN) ?: false
            //if (isFirstLogin) {
            binding?.someThingWrong?.tvTapToRetry?.hideView()
            /*} else {
                tvTapToRetry.showView()
            }*/
            val icon = /*if (isFirstLogin)*/
                R.mipmap.ic_family_pedia /*else R.mipmap.no_data_avail_new*/
            binding?.someThingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    icon
                )
            )
            val title = /*if (isFirstLogin)*/
                getString(R.string.welcome_to_familyPedia) /*else getString(R.string.no_data_available)*/
            val desc = getString(R.string.no_data_avaliable_new_description)

            binding?.someThingWrong?.tvOops?.setTextOnTextView(title, "")
            binding?.someThingWrong?.tvSomethingWentWrong?.setTextOnTextView(
                desc,
                ""
            )
            binding?.someThingWrong?.llClickOnMsg?.setOnClickListener {
                AddCharacterListingActivity.open(requireActivity())
            }


        } else if (errorType == ErrorType.SOMETHING_WENT_WRONG) {

            binding?.someThingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.no_internet_connect_new
                )
            )
            binding?.someThingWrong?.tvOops?.setTextOnTextView(getString(R.string.tv_oops), "")
            binding?.someThingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.something_went_wrong), "")

        } else { //no internet
            binding?.someThingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.no_internet_connect_new
                )
            )
            binding?.someThingWrong?.tvOops?.setTextOnTextView(getString(R.string.tv_oops), "")
            binding?.someThingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
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

    private var postDataToBeUpdated: PostData? = null

    override fun onEditClick(postData: PostData) {
        postDataToBeUpdated = postData
        val bundle = Bundle()
        bundle.putString(FROM, FROM_EDIT_POST)
        bundle.putString(Constants.CHARACTER_ID, postData.character?._id)
        bundle.putSerializable(POST_DATA, postData)
        bundle.putSerializable(CHARACTER_DATA, postData.character)

        //AddNewPostActivity.open(requireActivity(), bundle)
        val intent = Intent(requireContext(), AddNewPostActivity::class.java)
        intent.putExtras(bundle)

        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val updatedData =
                        result.data?.extras?.getSerializable(Constants.SELECTED_LIST) as PostData
                    postDataToBeUpdated?.eventType = updatedData.eventType
                    postDataToBeUpdated?.startingDate = updatedData.startingDate
                    postDataToBeUpdated?.endingDate = updatedData.endingDate
                    postDataToBeUpdated?.location = updatedData.location
                    postDataToBeUpdated?.description = updatedData.description
                    postDataToBeUpdated?.attachPhotos = updatedData.attachPhotos
                    homePostsAdapter?.notifyDataSetChanged()

                    /*homePostsAdapter=null
                    initializeAdapter()
                    homePostsAdapter.*/
                } catch (e: Exception) {
                }
            }
        }

    override fun onDeleteClick(postData: PostData) {
        postId = postData._id ?: ""
        deleteData = postData
        deletePostDialog = ConfirmationDialog(
            requireActivity(),
            this,
            getString(R.string.delete),
            getString(R.string.delete_post_confirmation),
            FROM_DELETE_POST
        )
        deletePostDialog?.show()
          action_type = "delete_post"
    }

    override fun onReportClick(postData: PostData) {
        postId = postData._id ?: ""
        action_type = "report_post"
        showReportPostDialog()
    }

    private fun showReportPostDialog() {
        reportPostDialog = ReportPostDialog(
            requireActivity(),
            this,
            getString(R.string.report),
            getString(R.string.confirm_report_post),
        )
        reportPostDialog?.show()
    }

    override fun onYes() {
        if (action_type == "delete_post")
            deletePostAPI(postId)
        if (action_type == "report_post")
            reportPostAPI(postId)
    }

    override fun onNo() {
        deletePostDialog?.dismiss()
    }

    private fun performAccountDeleted() {
        lifecycleScope.launch {
            Utility.clearAllPreferences(requireActivity())
            AuthViewsHolderActivity.open(requireActivity(), null)
            requireActivity().finishAffinity()
        }
    }

    companion object {
        val notificationLiveData = MutableLiveData<Boolean>()
    }



    override fun onDown(p0: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(p0: MotionEvent) {
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }



    override fun onLongPress(p0: MotionEvent) {
    }

    override fun onFling(
        p0: MotionEvent?,
        p1: MotionEvent,
        p2: Float,
        p3: Float
    ): Boolean {
        p0.let {
            p1.let {
                try {
                    val diffY = p1.y - p0!!.y
                    val diffX = p1.x - p0!!.x
                    if (abs(diffX) > abs(diffY)) {
                        if (abs(diffX) > swipeThreshold && abs(p2) > swipeVelocityThreshold) {
                            binding?.rvPosts?.requestDisallowInterceptTouchEvent(true)
                            if (diffX > 0) {
                                //Toast.makeText(applicationContext, "Left to Right swipe gesture", Toast.LENGTH_SHORT).show()
                            } else {
                                //Toast.makeText(applicationContext, "Right to Left swipe gesture", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
        return true
    }



}

