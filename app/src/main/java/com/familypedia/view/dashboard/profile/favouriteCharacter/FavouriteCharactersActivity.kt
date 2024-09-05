package com.familypedia.view.dashboard.profile.favouriteCharacter

import FavouriteCharactersAdapter
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.listeners.RemoveFavouriteCharacterListener
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.viewmodels.CharacterViewModel
import kotlinx.coroutines.launch
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import com.familypedia.databinding.ActivityFavouriteCharactersBinding


class FavouriteCharactersActivity : AppCompatActivity(), FamilyPediaClickListener,
    SuccessFulListener, RemoveFavouriteCharacterListener {
    var favouriteCharacterAdapter: FavouriteCharactersAdapter? = null
    private var characterViewModel: CharacterViewModel? = null
    private var charactersList = arrayListOf<CharacterData>()
    private var searchText = ""
    private var currentPage = 1
    private var characterDataToBeRemoved: CharacterData? = null
    var layoutManager: GridLayoutManager? = null
    private var totalPage = 1
    private var loading = true
    private var dataFetchedFromServer = false
    private lateinit var binding: ActivityFavouriteCharactersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouriteCharactersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.somethingWrong.clSomthingWentWrong.hideView() // Update view references with binding
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        setupViews()
        setAdapter()
        initListeners()
        initViewModel()
        responseData()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
        searchTextListener()
    }

    private fun initListeners() {
        binding.somethingWrong.tvTapToRetry.familyPediaClickListener(this)
        binding.ivSearch.familyPediaClickListener(this)
        binding.toolbarFavouriteCharacters.ivBack.familyPediaClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun searchTextListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                Handler().postDelayed({
                    searchText = p0.toString()
                    if (searchText.isEmpty()) {
                        currentPage = 1
                        hitFavCharactersListAPI(searchText, false)
                    }
                }, 2000)

            }
        })

        binding.etSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchText = binding.etSearch.text.toString()
                currentPage = 1
                hitFavCharactersListAPI(searchText, false)
                return@OnEditorActionListener true
            }
            false
        })

    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.toolbarFavouriteCharacters.ivBack -> onBackPressed()
            binding.ivSearch -> {
                searchText = binding.etSearch.text.toString()
                currentPage = 1
                hitFavCharactersListAPI(searchText, false)
            }
            binding.somethingWrong.tvTapToRetry -> refreshData()
        }
    }

    private fun refreshData() {
        charactersList.clear()
        favouriteCharacterAdapter?.notifyDataSetChanged()
        hitFavCharactersListAPI("", false)

    }

    private fun setupViews() {
        binding.toolbarFavouriteCharacters.tvToolbarTitle.text = getString(R.string.favourite_characters)
    }

    private fun setAdapter() {
        layoutManager = GridLayoutManager(this, 2)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.dp_10)
        //rvFavouriteCharacters.addItemDecoration(SpacesItemDecoration(spacingInPixels))
        binding.rvFavouriteCharacters.layoutManager = layoutManager
        favouriteCharacterAdapter = FavouriteCharactersAdapter(this, charactersList, this)
        binding. rvFavouriteCharacters.adapter = favouriteCharacterAdapter
        binding.rvFavouriteCharacters.addOnScrollListener(recyclerScrollListener)
    }

    private var recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //check for scroll down
                    val visibleItemCount = layoutManager?.childCount
                    val totalItemCount = layoutManager?.itemCount
                    val pastVisiblesItems = layoutManager?.findFirstVisibleItemPosition()
                    if (loading) {
                        if ((visibleItemCount!! + pastVisiblesItems!!) >= totalItemCount!!) {
                            loading = false
                            if (currentPage < totalPage) {
                                hitFavCharactersListAPI(searchText, true)
                            }
                        }
                    }
                }
            }
        }


    override fun removeFavouriteCharacter(character: CharacterData) {
        characterDataToBeRemoved = character
        character._id?.let { characterId ->
            addRemoveBookmark(characterId)
        }
    }

    override fun onListSizeChanged(size: Int) {
        Log.d("", "onListSizeChanged: $size")
        if (size == 0 && dataFetchedFromServer) {
            dataFetchedFromServer = !dataFetchedFromServer
            manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
        } else {
            binding.somethingWrong.clSomthingWentWrong.hideView()
        }
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
        hitFavCharactersListAPI("", false)
    }

    private fun hitFavCharactersListAPI(searchText: String, pagenate: Boolean) {
        binding.somethingWrong.clSomthingWentWrong.hideView()
        this.hideKeyboard(this)
        if (pagenate) {
            currentPage++
        }
        val searchRequest = SearchRequest(searchText.trim(), currentPage)
        characterViewModel?.getBookmarkList(this, searchRequest)
    }

    private fun addRemoveBookmark(characterId: String) {
        val bookmarkRequest = BookmarkRequest(characterId, false)
        characterViewModel?.addRemoveBookmark(this, bookmarkRequest)
    }

    private fun responseData() {
        characterViewModel?.characterListResponseResult?.observe(
            this
        ) { characterResponse ->
            dataFetchedFromServer = true
            if (characterResponse?.data?.data != null && characterResponse.data.data.isNotEmpty()) {
                if (currentPage == 1) {
                    totalPage = characterResponse.data.totalPages ?: 1
                    charactersList.clear()
                }
                val sortedData = characterResponse.data.data.sortedBy {
                    it.name.toString()
                }
                charactersList.addAll(sortedData)
                favouriteCharacterAdapter?.notifyDataSetChanged()
            } else {
                if (currentPage == 1)
                    manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }

        characterViewModel?.simpleResponseResult?.observe(this) { response ->
            if (response.apiName == ADD_REMOVE_BOOKMARK) {
                binding.clParent?.showStringSnackbar(response.message ?: "")
                charactersList.remove(characterDataToBeRemoved)
                favouriteCharacterAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(this) { loadingData ->
            if (loadingData.apiName == BOOKMARK_LIST) {
                if (loadingData.status) {
                    binding.somethingWrong.clSomthingWentWrong.hideView()
                    manageShimmer(true)
                } else {
                    manageShimmer(false)
                }
            } else {
                if (loadingData.status) {
                  //  progressBar.show()
                } else {
                   // progressBar.hide()
                }

            }
        }
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(this) {
            if (it.apiName == BOOKMARK_LIST) {
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
                if (errorData.apiName == BOOKMARK_LIST) {
                    binding.clParent?.showStringSnackbarError(errorData.message)
                    manageShimmer(false)
                    manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
                } else {
                    binding.clParent?.showStringSnackbarError(errorData.message)
                }
            }
        }
    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            if (currentPage == 1) {
                charactersList.clear()
                favouriteCharacterAdapter?.notifyDataSetChanged()
                binding.shimmerLay.shimmerLayout.showView()
                binding.shimmerLay.shimmerLayout.startShimmer()
            } else {
                binding.progressPaging.showView()
            }
        } else {
            if (currentPage == 1) {
                binding.shimmerLay.shimmerLayout.stopShimmer()
                binding.shimmerLay.shimmerLayout.hideView()
                binding.swipeRefreshLayout.isRefreshing = false
            } else {
                binding.progressPaging.hideView()
                loading = false
            }
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        binding.somethingWrong.clSomthingWentWrong.showView()
        if (errorType == ErrorType.NO_DATA_AVAILABLE) {
            //showHideViews(false)
            binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.mipmap.ic_no_biographies_found
                )
            )
            binding.somethingWrong.tvOops.visibility = View.GONE
            binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_biographies), "")
        } else if (errorType == ErrorType.NO_INTERNET) { //no internet
            //showHideViews(true)
            binding.somethingWrong.tvOops.visibility = View.VISIBLE
            binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.no_internet_connect_new
                )
            )
            binding.somethingWrong.tvOops.setTextOnTextView(getString(R.string.tv_oops), "")
            binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_internet), "")
        } else {
            //showHideViews(true)
            binding.somethingWrong.tvOops.visibility = View.VISIBLE
            binding.somethingWrong.ivSomethingWentWrong.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.no_internet_connect_new
                )
            )
            binding.somethingWrong.tvOops.setTextOnTextView(getString(R.string.tv_oops), "")
            binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.something_went_wrong), "")
        }
    }

    private fun showHideViews(show: Boolean) {
        if (show) {
            binding.somethingWrong.ivSomethingWentWrong.showView()
            binding.somethingWrong.ivRefresh.showView()
            binding.somethingWrong.tvTapToRetry.showView()
        } else {
            binding.somethingWrong.ivSomethingWentWrong.hideView()
            binding.somethingWrong.ivRefresh.hideView()
            binding.somethingWrong.tvTapToRetry.hideView()
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
            Utility.clearAllPreferences(this@FavouriteCharactersActivity)
            AuthViewsHolderActivity.open(this@FavouriteCharactersActivity, null)
            finishAffinity()
        }
    }


    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, FavouriteCharactersActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
}