package com.familypedia.view.dashboard.character.aboutCharacters

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.familypedia.R
import com.familypedia.databinding.FragmentCharacterTimelineBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.listeners.PostsListener
import com.familypedia.view.dashboard.character.post.AddNewPostActivity
import com.familypedia.view.dashboard.home.adapter.HomePostsAdapter
import com.familypedia.viewmodels.CharacterViewModel
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CharacterTimelineFragment(
    /*val characterId: String, var permittedToPost: PermittedToPost*/
) : Fragment(), PostsListener,
    SuccessFulListener,
    FamilyPediaClickListener, ConfirmationDialogListener {
    private var param1: String? = null
    private var param2: String? = null
    private var characterViewModel: CharacterViewModel? = null
    private var postsList = arrayListOf<PostData>()
    private var homePostsAdapter: HomePostsAdapter? = null
    var postId = ""
    var deleteData: PostData? = null
    var deletePostDialog: ConfirmationDialog? = null
    private var timer: Timer = Timer()
    private val DELAY: Long = 2500 // Milliseconds
    var characterId: String=""
    var permittedToPost: PermittedToPost?=null
    private var _binding: FragmentCharacterTimelineBinding? = null
    private val binding get() = _binding!!
    companion object {
        fun newInstance(
            characterId: String,permittedToPost: PermittedToPost
        ): CharacterTimelineFragment {
            val timelineFragment = CharacterTimelineFragment()
            val args = Bundle()
            args.putString("characterId", characterId)
            args.putSerializable("permittedToPost", permittedToPost)
            timelineFragment.arguments = args
            return timelineFragment
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

            characterId=it.getString("characterId")?:""
            permittedToPost=it.getSerializable("permittedToPost") as PermittedToPost
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_character_timeline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (permittedToPost != PermittedToPost.YES) {
            binding?.ivLocked?.showView()
        } else {
            binding?.ivLocked?.hideView()
            initializeControl()
        }
    }


    private fun initializeControl() {

        setAdapter()
        initViewModel()
        responseData()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
        addHighlightFeature()
        listeners()
        AboutCharacterActivity.profileUpdatedObserver.observe(viewLifecycleOwner) {
            /* postsList.clear()
             homePostsAdapter?.notifyDataSetChanged()
             homePostsAdapter=null
             setAdapter()
             hitHomePostsAPI()*/
            refreshData()
        }
        //LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(mMessageReceiver, IntentFilter(Constants.RECEIVER_DATA_UPDATED));
    }

    /* override fun onDestroy() {
         LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(mMessageReceiver)
         super.onDestroy()
     }*/
    /*private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshData()

        }
    }*/
    private fun listeners() {
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            refreshData()
        }
        binding?.somethingWrong?.clSomthingWentWrong?.familyPediaClickListener(this)
        binding?.ivSearch?.familyPediaClickListener(this)
        binding?.somethingWrong?.tvTapToRetry?.familyPediaClickListener(this)

    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.somethingWrong?.tvTapToRetry -> refreshData()
            binding?.ivSearch -> {
                if (binding?.etSearch?.text.toString().isNotEmpty())
                    homePostsAdapter?.setFilter(postsList, binding?.etSearch?.text.toString())
            }
        }
    }

    private fun setAdapter() {
        //characterTimelineAdapter = CharacterTimelineAdapter(requireContext())

        val dividerItemDecoration =
            DividerItemDecoration(binding?.rvTimeline?.context, LinearLayoutManager.VERTICAL)
        binding?.rvTimeline?.addItemDecoration(dividerItemDecoration)
        homePostsAdapter = HomePostsAdapter(requireActivity(), this, postsList, "")
        binding?.rvTimeline?.adapter = homePostsAdapter
    }


    //EditDelete Listener
    override fun onEditClick(postData: PostData) {
        val bundle = Bundle()
        bundle.putString(Constants.FROM, Constants.FROM_EDIT_POST)
        bundle.putString(Constants.CHARACTER_ID, postData.character?._id)
        bundle.putSerializable(Constants.POST_DATA, postData)
        bundle.putSerializable(Constants.CHARACTER_DATA, postData.character)
        AddNewPostActivity.open(requireActivity(), bundle)
    }

    override fun onDeleteClick(postData: PostData) {
        postId = postData._id ?: ""
        deleteData = postData
        deletePostDialog = ConfirmationDialog(
            requireContext(),
            this,
            getString(R.string.delete),
            getString(R.string.delete_post_confirmation),
            Constants.FROM_DELETE_POST
        )
        deletePostDialog?.show()
    }

    override fun onReportClick(postData: PostData) {
        reportPostAPI(postData._id.toString())
    }

    override fun onYes() {
        deletePostAPI(postId)
    }

    override fun onNo() {
        deletePostDialog?.dismiss()
    }

    //SuccessDialog Listener
    override fun onUpdateSuccessfully(from: String) {
    }

    override fun onDismiss(from: String) {
    }


    private fun refreshData() {
        postsList.clear()
        homePostsAdapter?.notifyDataSetChanged()
        hitHomePostsAPI()
    }

    private fun manageSearch(enable: Boolean) {
        if (enable) {
            binding?.etSearch?.isEnabled = true
            binding?.etSearch?.alpha = 1f
        } else {
            binding?.etSearch?.isEnabled = false
            binding?.etSearch?.alpha = 0.4f
        }
    }

    private fun addHighlightFeature() {
        binding?.etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                Handler().postDelayed({
                    homePostsAdapter?.setFilter(postsList, p0.toString())
                }, 2000)

            }

        })


        binding?.etSearch?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = binding?.etSearch?.text.toString()
                if (searchText.isNotEmpty()) {
                    homePostsAdapter?.setFilter(postsList, searchText)
                }

                return@OnEditorActionListener true
            }
            false
        })

    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
        hitHomePostsAPI()


    }

    private fun hitHomePostsAPI() {
        characterViewModel?.getTimeLinePosts(requireContext(), characterId)
    }

    private fun deletePostAPI(postId: String) {
        characterViewModel?.deletePost(postId, requireContext())
    }

    private fun reportPostAPI(postId: String) {
        characterViewModel?.reportPost(ReportPostRequest(postId), requireActivity())
    }

    private fun responseData() {
        characterViewModel?.timelineListResult?.observe(viewLifecycleOwner) { response ->
            if (response?.data != null && response.data?.isNotEmpty() == true) {
                postsList.clear()

                //postsList.add(response.data[0])
                postsList.addAll(response.data)
                homePostsAdapter?.notifyDataSetChanged()
            } else {
                manageSomethingWentWrong(true)
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
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(viewLifecycleOwner) {
            if (it.apiName == CHARACTER_TIMELINE) {
                if (it.status) {
                    binding?.somethingWrong?.clSomthingWentWrong?.hideView()
                    manageShimmer(true)
                } else {
                    manageShimmer(false)
                }
            }
        }
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(viewLifecycleOwner) { exceptionData ->
            if (exceptionData.apiName == CHARACTER_TIMELINE) {
                if (exceptionData.message == getString(R.string.no_internet)) {
                    manageSomethingWentWrong(false)
                } else {
                    binding?.clParent?.showStringSnackbarError(exceptionData.message)
                    manageSomethingWentWrong(true)
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
                if (errorData.apiName == CHARACTER_TIMELINE) {
                    binding?.clParent?.showStringSnackbarError(errorData.message)
                    manageShimmer(false)
                    manageSomethingWentWrong(true)
                }
            }

        }
    }


    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            postsList.clear()
            homePostsAdapter?.notifyDataSetChanged()
           binding?.shimmerLay?.shimmerLayoutPostList?.showView()
            binding?.shimmerLay?.shimmerLayoutPostList?.startShimmer()
            manageSearch(false)
        } else {
            binding?.shimmerLay?.shimmerLayoutPostList?.stopShimmer()
            binding?.shimmerLay?.shimmerLayoutPostList?.hideView()
            binding?.swipeRefreshLayout?.isRefreshing = false
            manageSearch(true)
        }
    }

    private fun manageSomethingWentWrong(isNoData: Boolean) {
        binding.somethingWrong?.clSomthingWentWrong?.showView()
        manageSearch(false)
        if (isNoData) {
            binding.somethingWrong?.ivSomethingWentWrong?.setImageResource(R.drawable.ic_refresh)
            binding.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_data_available), "")
        } else { //no internet
            binding.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
        }

    }


}