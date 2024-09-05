package com.familypedia.view.dashboard.character.addCharacter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ActivityAddCharacterListingBinding
import com.familypedia.network.CharacterData
import com.familypedia.network.SearchRequest
import com.familypedia.utils.*
import com.familypedia.utils.Constants.CHARACTER_NAME
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_ADD_CHARACTER
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.character.adapter.CharactersAdapter
import com.familypedia.view.dashboard.character.adapter.CharactersListener
import com.familypedia.viewmodels.CharacterViewModel

import kotlinx.coroutines.launch

class AddCharacterListingActivity : AppCompatActivity(), FamilyPediaClickListener,
    CharactersListener, SuccessFulListener {
    private var characterAdapter: CharactersAdapter? = null
    private var charactersList = arrayListOf<CharacterData>()
    private var characterNameText = ""
    private var characterViewModel: CharacterViewModel? = null
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    var linearLayoutManager: LinearLayoutManager? = null
    private var errorType: ErrorType = ErrorType.INIT
    private lateinit var binding: ActivityAddCharacterListingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCharacterListingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        setListeners()
        characterNameTextWatcher()
        setAdapter()
        manageButton(false)

        initViewModel()
        responseData()
        //searchTextListener()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
        manageSomethingWentWrong(ErrorType.INIT)
    }

    private fun setListeners() {
        binding.toolbarAddCharacterList?.ivBack?.familyPediaClickListener(this)
        binding?.btnContinue?.familyPediaClickListener(this)
        binding?.toolbarAddCharacterList?.tvToolbarTitle?.text = getString(R.string.add_new_character_)
        binding?.btnContinue?.text=getString(R.string.click_to_create_biography)
        binding?.somethingWrong?.tvTapToRetry?.familyPediaClickListener(this)
        binding?.somethingWrong?.clSomthingWentWrong?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.toolbarAddCharacterList?.ivBack -> onBackPressed()
            binding?.btnContinue -> {
                    val bundle = Bundle()
                    bundle.putString(FROM, FROM_ADD_CHARACTER)
                    bundle.putString(CHARACTER_NAME, characterNameText)
                    AddNewCharacterActivity.open(this, bundle)

            }
            binding?.somethingWrong?.clSomthingWentWrong -> {
                if (errorType == ErrorType.INIT) {
                    binding?.etCharacterName?.requestFocus()
                    showKeyboard(this)
                } else {
                    hitSearchCharacterAPI(characterNameText, false)
                }
            }
            binding?.somethingWrong?.tvTapToRetry -> {
                if (errorType == ErrorType.INIT) {
                    binding?.etCharacterName?.requestFocus()
                    showKeyboard(this)
                } else {
                    hitSearchCharacterAPI(characterNameText, false)
                }
            }
        }
    }

    private fun setAdapter() {
        linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        characterAdapter = CharactersAdapter(this, charactersList, this, "",null)
        binding?.rvCharacterList?.adapter = characterAdapter
        binding?.rvCharacterList?.layoutManager = linearLayoutManager
        binding?.rvCharacterList?.addOnScrollListener(recyclerScrollListener)

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
                                hitSearchCharacterAPI(characterNameText, true)
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

    private fun characterNameTextWatcher() {
        binding?.etCharacterName?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0?.trim().toString().isNotEmpty()) {
                    manageButton(true)
                } else {
                    manageButton(false)
                }
                characterNameText = p0.toString().trim()
            }

            override fun afterTextChanged(p0: Editable?) {
                /*Handler().postDelayed({
                    characterNameText = p0.toString()
                    if (characterNameText.trim().isNotEmpty()) {
                        hitSearchCharacterAPI(characterNameText, false)
                    }
                }, 3500)
                */
            }
        })
        binding?.etCharacterName?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (characterNameText.isNotEmpty()) {
                    currentPage = 1
                    hitSearchCharacterAPI(characterNameText, false)
                }

                return@OnEditorActionListener true
            }
            false
        })

        binding?.etCharacterName?.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding?.etCharacterName?.getRight()!! - binding?.etCharacterName?.getCompoundDrawables()
                    ?.get(DRAWABLE_RIGHT)?.getBounds()?.width()!!
                ) {
                    if (characterNameText.isNotEmpty()) {
                        currentPage = 1
                        hitSearchCharacterAPI(characterNameText, false)
                    }
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun manageButton(activateButton: Boolean) {
        if (!activateButton) {
            binding?.btnContinue?.alpha = 0.4f
            binding?.btnContinue?.isClickable = false
        } else {
            binding?.btnContinue?.alpha = 1f
            binding?.btnContinue?.isClickable = true
        }
    }


    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
    }

    private fun hitSearchCharacterAPI(searchText: String, pagenate: Boolean) {
        this.hideKeyboard(this)

        if (pagenate) {
            currentPage++
        }
        val request = SearchRequest(searchText.trim(), currentPage)
        characterViewModel?.searchCharacter(this, request)
    }

    private fun responseData() {
        characterViewModel?.characterListResponseResult?.observe(
            this
        ) { characterResponse ->
            if (characterResponse?.data?.data != null && characterResponse.data.data.isNotEmpty()) {
                if (currentPage == 1) {
                    totalPage = characterResponse.data.totalPages ?: 1
                    charactersList.clear()
                }
                charactersList.addAll(characterResponse?.data?.data)
                characterAdapter?.notifyDataSetChanged()
                binding?.somethingWrong?.clSomthingWentWrong?.hideView()

            } else {
                manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(this, {
            if (it.status) {
                binding?.somethingWrong?.clSomthingWentWrong?.hideView()
                manageShimmer(true)
            } else {
                manageShimmer(false)
            }

        })
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
                characterAdapter?.notifyDataSetChanged()
                binding?.progressLoading?.showView()
            } else {
                binding?.progressLoading?.showView()
            }
        } else {
            if (currentPage == 1)
                binding?.progressLoading?.hideView()
            else
                binding?.progressLoading?.hideView()
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
                binding?.somethingWrong?.tvOops?.visibility= View.GONE
                binding?.somethingWrong?.tvTapToRetry?.showView()
               // clSomthingWentWrong.showView()
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.mipmap.no_data_avail_new
                    )
                )
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_biographie), "")
            } else if (errorType == ErrorType.NO_INTERNET) { //no internet
                binding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
            } else if (errorType == ErrorType.INIT) {
                binding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.mipmap.ic_search_something
                    )
                )
                binding?.somethingWrong?.tvOops?.visibility= View.GONE
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
                binding?.somethingWrong?.tvTapToRetry?.showView()
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
            Utility.clearAllPreferences(this@AddCharacterListingActivity)
            AuthViewsHolderActivity.open(this@AddCharacterListingActivity, null)
            finishAffinity()
        }
    }


    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, AddCharacterListingActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
}