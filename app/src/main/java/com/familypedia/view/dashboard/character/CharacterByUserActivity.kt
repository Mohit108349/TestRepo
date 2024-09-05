package com.familypedia.view.dashboard.character

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ActivityCharacterByUserBinding
import com.familypedia.network.CharacterData
import com.familypedia.network.User
import com.familypedia.network.UserRequest
import com.familypedia.utils.*
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.Constants.USER_DATA
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.character.adapter.CharactersAdapter
import com.familypedia.view.dashboard.character.adapter.CharactersListener
import com.familypedia.view.dashboard.profile.ViewProfileActivity
import com.familypedia.viewmodels.CharacterViewModel
import io.ak1.pix.helpers.hide
import kotlinx.coroutines.launch

class CharacterByUserActivity : AppCompatActivity(), FamilyPediaClickListener,
    CharactersListener, SuccessFulListener {
    private var charactersAdapter: CharactersAdapter? = null
    private var characterViewModel: CharacterViewModel? = null
    private var charactersList = arrayListOf<CharacterData>()
    private var userData: User? = null
    private var userId = ""
    private var currentPage = 1
    private var totalPage = 1
    private var loading = true
    private lateinit var binding: ActivityCharacterByUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the View Binding object
        binding = ActivityCharacterByUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        binding.toolbarCharacterList.tvToolbarTitle.text = getString(R.string.biographies_list)
        userData = intent.extras?.getSerializable(USER_DATA) as User
        binding.ivCharacterImage.loadImagesWithGlide(
            IMAGE_URL + userData?.profile_pic,
            false
        )
        userId = userData?._id ?: ""
        binding.tvCharacterName.setTextOnTextView(userData?.name, "")

        setAdapter()
        initListeners()
        initViewModel()
        responseData()
        manageDataLoadingStatus()
        onGettingError()
        onGettingException()
    }

    private fun initListeners() {
        binding.somethingWrong.tvTapToRetry.familyPediaClickListener(this)
        binding.toolbarCharacterList.ivBack.familyPediaClickListener(this)
        binding.ivCharacterImage.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.somethingWrong.tvTapToRetry -> {
                hitCharactersListAPI(false)
            }
            binding.toolbarCharacterList.ivBack -> onBackPressed()
            binding.ivCharacterImage -> {
                if (userId != null && userId.isNotEmpty()) {
                    val bundle = Bundle()
                    bundle.putString(Constants.FROM, Constants.FROM_VIEW_USER)
                    bundle.putString(Constants.USER_ID, userId)
                    ViewProfileActivity.open(this, bundle)
                }
            }
        }
    }

    private fun setAdapter() {
        charactersAdapter = CharactersAdapter(
            this,
            charactersList,
            this,
            Constants.FROM_CHARACTER_BY_USER,
        null)
        binding.rvAllBiography.adapter = charactersAdapter
        binding.rvAllBiography.addOnScrollListener(recyclerScrollListener)
    }

    //For Paging
    private var recyclerScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //check for scroll down
                    val visibleItemCount = binding.rvAllBiography.layoutManager?.childCount
                    val totalItemCount = binding.rvAllBiography.layoutManager?.itemCount
                    val pastVisiblesItems =
                        (binding.rvAllBiography.layoutManager as LinearLayoutManager)?.findFirstVisibleItemPosition()
                    if (loading) {
                        if ((visibleItemCount!! + pastVisiblesItems!!) >= totalItemCount!!) {
                            loading = false
                            if (currentPage < totalPage) {
                                hitCharactersListAPI(true)
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
        hitCharactersListAPI(false)
    }

    private fun hitCharactersListAPI(pageNate: Boolean) {
        if (pageNate) currentPage++
        characterViewModel?.getCharactersByAnotherUser(this, UserRequest(userId,currentPage))
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
                charactersList.addAll(characterResponse.data.data)
                charactersAdapter?.notifyDataSetChanged()
            } else {
                manageSomethingWentWrong(ErrorType.NO_DATA_AVAILABLE)
            }
        }
    }

    private fun manageDataLoadingStatus() {
        characterViewModel?.updateLoaderStatus?.observe(this) {
            if (it.status) {
                binding.somethingWrong.clSomthingWentWrong.hideView()
                manageShimmer(true)
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
                binding.clParent?.showStringSnackbarError(it.message)
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
                binding.clParent?.showStringSnackbarError(errorData.message)
                manageShimmer(false)
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
            }
        }
    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            if (currentPage == 1) {
                binding.characterListShimmer.shimmerLayout.showView()
                binding.characterListShimmer.shimmerLayout.startShimmer()
            }else
                binding.progressPaging.showView()
        } else {
            if (currentPage==1) {
                binding.characterListShimmer.shimmerLayout.stopShimmer()
                binding.characterListShimmer.shimmerLayout.hideView()
            }else{
                binding.progressPaging.hide()
                loading=true
            }
        }
    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        binding.somethingWrong.clSomthingWentWrong.showView()
        if (errorType == ErrorType.NO_DATA_AVAILABLE) {
            binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_data_available), "")
        } else if (errorType == ErrorType.NO_INTERNET) { //no internet
            binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.no_internet), "")
        } else {
            binding.somethingWrong.tvSomethingWentWrong.setTextOnTextView(getString(R.string.something_went_wrong), "")
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
            Utility.clearAllPreferences(this@CharacterByUserActivity)
            AuthViewsHolderActivity.open(this@CharacterByUserActivity, null)
            finishAffinity()
        }
    }

    companion object {
        fun open(currActivity: Activity, bundle: Bundle) {
            val intent = Intent(currActivity, CharacterByUserActivity::class.java)
            intent.putExtras(bundle)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }

}