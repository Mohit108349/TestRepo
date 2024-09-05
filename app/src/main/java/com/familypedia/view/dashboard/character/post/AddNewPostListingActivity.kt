package com.familypedia.view.dashboard.character.post


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ActivityAddNewPostListingBinding
import com.familypedia.network.CharacterData
import com.familypedia.network.SearchRequest
import com.familypedia.utils.*
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.DashboardActivity
import com.familypedia.view.dashboard.character.CharactersFragment
import com.familypedia.view.dashboard.character.adapter.CharactersAdapter
import com.familypedia.view.dashboard.character.adapter.CharactersListener
import com.familypedia.viewmodels.CharacterViewModel
import kotlinx.coroutines.launch


class AddNewPostListingActivity : AppCompatActivity(), FamilyPediaClickListener, CharactersListener,
    SuccessFulListener {

    private var charactersAdapter: CharactersAdapter? = null
    private var characterViewModel: CharacterViewModel? = null
    private var charactersList = arrayListOf<CharacterData>()
    private var searchText = ""
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    var linearLayoutManager: LinearLayoutManager? = null
    private var errorType: ErrorType = ErrorType.INIT
    private lateinit var binding: ActivityAddNewPostListingBinding
    val  charactersFragment = CharactersFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewPostListingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        setAdapter()
        initViews()
        initViewModel()
        responseData()
        searchTextListener()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
        manageSomethingWentWrong(ErrorType.INIT)
    }

    private fun initViews() {
        binding?.toolbarAddNewPosListingt?.tvToolbarTitle?.text = getString(R.string.add_new_post)
        binding?.toolbarAddNewPosListingt?.ivBack?.familyPediaClickListener(this)
        binding?.somethingWrong?.tvTapToRetry?.familyPediaClickListener(this)
        binding?.ivSearch?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.toolbarAddNewPosListingt?.ivBack -> onBackPressed()
            binding?.ivSearch -> {
                searchText = binding?.etSearch?.text.toString()
                if (searchText.isNotEmpty()) {
                    currentPage = 1
                    hitSearchCharacterAPI(searchText, false)
                }
            }
            binding?.somethingWrong?.tvTapToRetry -> {
                if (errorType == ErrorType.INIT) {
                    binding?.etSearch?.requestFocus()
                    showKeyboard(this)
                } else {
                    hitSearchCharacterAPI(searchText, false)
                }

            }
        }
    }


    private fun searchTextListener() {
        binding?.etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                Handler().postDelayed({
                    searchText = p0.toString()
                    if (searchText.isEmpty()) {
                        currentPage = 1
                        hitSearchCharacterAPI(searchText, false)
                    }
                }, 3000)

            }
        })
        binding?.etSearch?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchText = binding?.etSearch?.text.toString()
                if (searchText.isNotEmpty()) {
                    currentPage = 1
                    hitSearchCharacterAPI(searchText, false)
                }

                return@OnEditorActionListener true
            }
            false
        })
       /* etSearch.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= etSearch.getRight() - etSearch.compoundDrawables
                        .get(DRAWABLE_RIGHT).getBounds().width()
                ) {
                    searchText = etSearch.text.toString()
                    if (searchText.isNotEmpty()) {
                        currentPage = 1
                        hitSearchCharacterAPI(searchText, false)
                    }
                    return@OnTouchListener true
                }
            }
            false
        })*/

    }

    private fun setAdapter() {
        linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        charactersAdapter = CharactersAdapter(
            this,
            charactersList,
            this,
            Constants.FROM_ADD_NEW_POST
        ,null)
        binding?.rvCharactersList?.adapter = charactersAdapter
        binding?.rvCharactersList?.layoutManager = linearLayoutManager
        binding?.rvCharactersList?.addOnScrollListener(recyclerScrollListener)
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
                                hitSearchCharacterAPI(searchText, true)
                            }
                            loading = true
                        }
                    }
                }
            }
        }


    override fun onListSizeChanged(size: Int) {
    }

    override fun onEditCharacterClicked(characterData: CharacterData) {
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
        hitSearchCharacterAPI("", false)
    }

    private fun hitSearchCharacterAPI(searchText: String, pagenate: Boolean) {
        this.hideKeyboard(this)
        if (pagenate) {
            currentPage++
        }
        val request = SearchRequest(searchText.trim(), currentPage)
        characterViewModel?.getUsersCharacterAndPermittedCharactersList(this, request)
    }

    private fun responseData() {
        characterViewModel?.characterListResponseResult?.observe(
            this
        ) { characterResponse ->
            if (characterResponse?.data?.data != null && characterResponse.data.data.isNotEmpty()) {
                binding?.tvSelectCharacter?.showView()
                binding?.tvChooseTheBio?.isVisible=false
                binding?.tvChooseTheBioSmall?.isVisible=false

                if (currentPage == 1) {
                    totalPage = characterResponse.data.totalPages ?: 1
                    charactersList.clear()
                }
                charactersList.addAll(characterResponse?.data?.data)
                charactersAdapter?.notifyDataSetChanged()
                binding?.somethingWrong?.clSomthingWentWrong?.hideView()

            } else {
                binding?.tvSelectCharacter?.hideView()
                manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(this) {
            if (it.status) {
                binding?.somethingWrong?.clSomthingWentWrong?.hideView()
                manageShimmer(true)
                binding?.tvSelectCharacter?.hideView()
                binding?.tvChooseTheBio?.isVisible=true
                binding?.tvChooseTheBioSmall?.isVisible=true
            } else {
                manageShimmer(false)
            }

        }
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(this) {
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
        characterViewModel?.error?.observe(this) { errorData ->
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
                manageShimmer(false)
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
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
            if (currentPage == 1) {
                charactersList.clear()
                charactersAdapter?.notifyDataSetChanged()
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
        this.errorType = errorType
        if (currentPage == 1) {
            binding?.somethingWrong?.clSomthingWentWrong?.showView()
            binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.no_internet_connect_new
                )
            )
           // ivRefresh.showView()
            binding?.somethingWrong?.tvTapToRetry?.showView()
            if (errorType == ErrorType.NO_DATA_AVAILABLE) {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.mipmap.no_data_avail_new

                    )
                )
                binding?.somethingWrong?.tvTapToRetry?.text= getString(R.string.tv_create_your_first_bio)
                binding?.somethingWrong?.tvTapToRetry?.setOnClickListener{
                    DashboardActivity.isCallFromPost =true
                 val intent =Intent(this ,AddNewPostListingActivity::class.java)
                    startActivity(intent)
                }

                binding?.somethingWrong?.tvOops?.visibility = View.GONE
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.remember_to_create), "")
                binding?.tvChooseTheBio?.isVisible=true
                binding?.tvChooseTheBioSmall?.isVisible=true
            } else if (errorType == ErrorType.NO_INTERNET) { //no internet
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.no_internet_connect_new
                    )
                )
                binding?.somethingWrong?.tvOops?.visibility = View.VISIBLE
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
            } else if (errorType == ErrorType.INIT) {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.mipmap.ic_search_something
                    )
                )
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(
                    getString(R.string.tap_to_search_character),
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
                binding?.somethingWrong?.tvOops?.visibility = View.VISIBLE
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
            Utility.clearAllPreferences(this@AddNewPostListingActivity)
            AuthViewsHolderActivity.open(this@AddNewPostListingActivity, null)
            finishAffinity()
        }
    }

    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, AddNewPostListingActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
//    fun onClick(view: View?) {
//        val fragment: Fragment = CharactersFragment()
//        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.container, fragment)
//        transaction.addToBackStack(null)
//        transaction.commit()
//    }

}