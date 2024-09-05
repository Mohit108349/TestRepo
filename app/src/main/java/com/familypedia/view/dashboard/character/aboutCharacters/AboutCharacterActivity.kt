package com.familypedia.view.dashboard.character.aboutCharacters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.familypedia.BuildConfig
import com.familypedia.R
import com.familypedia.databinding.ActivityAboutCharacterBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.Constants.CHARACTER_DATA
import com.familypedia.utils.Constants.CHARACTER_ID
import com.familypedia.utils.Constants.DYNAMIC_LINK
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_ADD_POST
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.Constants.NOTIFICATION
import com.familypedia.utils.Constants.PROFILE_PIC
import com.familypedia.utils.Constants.USER_ID
import com.familypedia.utils.Constants.USER_NAME
import com.familypedia.utils.listeners.BioListeners
import com.familypedia.utils.listeners.DeleteListener
import com.familypedia.view.dashboard.DashboardActivity
import com.familypedia.view.dashboard.character.post.AddNewPostActivity
import com.familypedia.viewmodels.CharacterViewModel
import com.familypedia.viewmodels.PermissionsViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import kotlinx.coroutines.launch

enum class PermittedToPost {
    YES,
    NO,
    PENDING
}

class AboutCharacterActivity : AppCompatActivity(), FamilyPediaClickListener, BioListeners,
    DeleteListener {
    private val titles = arrayListOf<String>()
    private var characterViewModel: CharacterViewModel? = null
    private var permissionsViewModel: PermissionsViewModel? = null
    private var characterId = ""
    private var characterName = ""
    private var characterPic = ""
    private var mInvitationUrl = ""
    private var bundleData: Bundle? = null
    private var bundle: Bundle? = null
    private var buttonText = ""
    private var isMyBio: Boolean = false
   val pendingDynamicLinkData: PendingDynamicLinkData? = null
    val deepLink: Uri? = pendingDynamicLinkData?.link

    // private var characterData: CharacterData? = null
    private var bookmarkStatus = false
    private var permittedToPost = PermittedToPost.NO
    private var userId = ""
    private var characterData: CharacterData? = null
    private var inProgress = false
    var deletePostDialog: ConfirmationDialog? = null
    private var from = ""
    val path = deepLink?.path
    private var fromUpdatePost = false
    private var mBinding: ActivityAboutCharacterBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about_character)
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        //titles.add(getString(R.string.about))

        userId = Utility.getPreferencesString(this, USER_ID)
        favProgress(true)
        manageButton(false)
        showButtonProgress()
        setClickListeners()
        getIntentData()
        initViewModel()
        responseData()
        setObserver()
        onGettingError()
        dataLoadingObserver()
        onGettingException()
        /*characterViewModel?.let {
            initViewPager(it, characterId)
        }*/
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constants.RECEIVER_DATA_UPDATED))


    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            fromUpdatePost = true
            hitCharacterProfileAPI()
        }
    }

    private fun setClickListeners() {
        mBinding?.toolbar?.ivBack?.familyPediaClickListener(this)
        mBinding?.layoutButton?.btnContinue?.familyPediaClickListener(this)
        mBinding?.btnFavourite?.familyPediaClickListener(this)
        mBinding?.somethingWrong?.tvTapToRetry?.familyPediaClickListener(this)
        mBinding?.toolbar?.ivOptions?.familyPediaClickListener(this)
        mBinding?.ivProfileImage?.familyPediaClickListener(this)
        mBinding?.tvCharacterName?.familyPediaClickListener(this)


    }

    override fun onViewClick(view: View) {
        when (view) {
            mBinding?.toolbar?.ivBack -> {
                if(isBackFromSplash)
                    DashboardActivity.open(this, bundleOf())
                else
                    backPress()
            }
            mBinding?.toolbar?.ivOptions -> Utility.showPopupBio(
                mBinding?.toolbar?.ivOptions!!, this@AboutCharacterActivity, characterId, this, isMyBio
            )

            mBinding?.layoutButton?.btnContinue -> {
                if (permittedToPost == PermittedToPost.YES) {
                    val bundle = Bundle()
                    bundle.putString(FROM, FROM_ADD_POST)
                    bundle.putString(CHARACTER_ID, characterId)
                    bundle.putSerializable(CHARACTER_DATA, characterData)
                    AddNewPostActivity.open(this, bundle)
                } else if (permittedToPost == PermittedToPost.NO) {
                    askPermission()
                }
            }
            mBinding?.btnFavourite -> addRemoveBookmark()
            mBinding?.somethingWrong?.tvTapToRetry -> hitCharacterProfileAPI()
            mBinding?.ivProfileImage -> openCharacterInfo()
            mBinding?.tvCharacterName -> openCharacterInfo()
        }
    }

    private fun openCharacterInfo() {
        /*val bundle = Bundle()
        bundle.putString(USER_ID,characterId)
        bundle.putSerializable(PERMITTED_TO_POST,permittedToPost)
        CharacterInfoActivity.open(this,bundle)
        */
    }

    private fun setViews() {
        if (from == NOTIFICATION)
            mBinding?.toolbar?.tvToolbarTitle?.text = getString(R.string.notification_detail)
        else
            mBinding?.toolbar?.tvToolbarTitle?.text = getString(R.string.about_character)
        mBinding?.toolbar?.ivOptions?.showView()

        mBinding?.ivProfileImage?.loadImagesWithGlide(IMAGE_URL + characterPic, true)
        println("::-->> $IMAGE_URL + $characterPic")
        mBinding?.tvCharacterName?.setTextOnTextView(characterName, "")
    }

    private fun setBookmarkView() {
        if (bookmarkStatus) {
            mBinding?.btnFavourite?.background =
                ContextCompat.getDrawable(this, R.drawable.ic_heart_filled)
        } else {
            mBinding?.btnFavourite?.background = ContextCompat.getDrawable(this, R.drawable.ic_heart)
        }
    }

    private fun getIntentData() {
        bundleData = intent.extras
        characterId = intent.extras?.getString(USER_ID) ?: ""
        characterName = intent.extras?.getString(USER_NAME) ?: ""
        characterPic = intent.extras?.getString(PROFILE_PIC) ?: ""
        from = intent.extras?.getString(FROM) ?: ""


        /*try {
            val characterData = intent.extras?.getSerializable(PERMITTED_USERS) as CharacterData
            setViewProfileData(characterData)
        }catch (e:Exception){

        }*/


        //bookmarkStatus = characterData?.bookmark ?: false

        //getData()
        setViews()
    }

    private fun setViewProfileData(charactersData: CharacterData) {
        val defaultText = getString(R.string.n_a)
        if (charactersData.status == Constants.STATUS_ALIVE || charactersData.status == getString(R.string.yes))
            mBinding?.tvStatus?.setTextOnTextView(getString(R.string.yes), defaultText)
        else if (charactersData.status == Constants.STATUS_DEAD)
            mBinding?.tvStatus?.setTextOnTextView(getString(R.string.no), defaultText)
        else
            mBinding?.tvStatus?.setTextOnTextView(charactersData.status, defaultText)

        characterName = charactersData.name?:"n/a"
//        val fullTitle = characterName + " " + "-" + " " + getString(R.string.timeline)
        titles.add(getString(R.string.timeline))
        mBinding?.tvCharacterName?.setTextOnTextView(characterName, "")
        mBinding?.tvDob?.setTextOnTextView(charactersData.date_of_birth, defaultText)
        mBinding?.tvCityOfBirth?.setTextOnTextView(charactersData.city_of_birth, defaultText)
        mBinding?.tvCountryOfBirth?.setTextOnTextView(charactersData.country_of_birth, defaultText)
        if (charactersData.date_of_death != null && charactersData.date_of_death.isNotEmpty()) {
            mBinding?.tvDod?.setTextOnTextView(charactersData.date_of_death, defaultText)
        } else {
            mBinding?.tvDod?.hideView()
            mBinding?.tvDodStatic?.hideView()
        }
        mBinding?.showProfileDetail = true
    }

    private fun getData(characterData: CharacterData) {
        setViewProfileData(characterData)
        this.characterData = characterData
        characterData.bookmark?.forEach { bookmark ->
            if (bookmark._id == userId) {
                bookmarkStatus = true
            }
        }
        characterData?.permittedUser?.let { permittedUsers ->
            if (permittedUsers.contains(userId)) {
                permittedToPost = PermittedToPost.YES
            }
        }
        characterData?.characterEditDeleterequestUser?.let { requestSentlist ->
            if (requestSentlist.contains(userId)) {
                permittedToPost = PermittedToPost.PENDING
            }
        }
        if (characterData?.userId == userId) {
            permittedToPost = PermittedToPost.YES
            isMyBio = true
        }

        when (permittedToPost) {
            PermittedToPost.YES -> {
                mBinding?.btnFavourite?.showView()
                buttonText = getString(R.string.add_new_post)
                manageButton(true)
            }
            PermittedToPost.PENDING -> {
                mBinding?.btnFavourite?.hideView()
                buttonText = getString(R.string.permission_requested)
                manageButton(false)
            }
            else -> {
                mBinding?.btnFavourite?.hideView()
                buttonText = getString(R.string.request_post_permissions)
                manageButton(true)
            }
        }
        mBinding?.layoutButton?.btnContinue?.text = buttonText
        setBookmarkView()

        //INIT VIEWPAGER
        //  if (!fromUpdatePost) {
        characterViewModel?.let {
            initViewPager(it, characterId)
        }
        //   }
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
        permissionsViewModel = ViewModelProvider(this).get(PermissionsViewModel::class.java)
        permissionsViewModel?.init()
        hitCharacterProfileAPI()

        /*profileUpdatedObserver.observe(this) {
            if (it) {
                // fromUpdatePost = true
                //  hitCharacterProfileAPI()
            }
        }*/
    }

    private fun hitCharacterProfileAPI() {
        manageShimmer(true)
        characterViewModel?.getCharacterProfile(this, characterId)
    }

    private fun addRemoveBookmark() {
        favProgress(true)
        val bookmarkRequest = BookmarkRequest(characterId, !bookmarkStatus)
        characterViewModel?.addRemoveBookmark(this, bookmarkRequest)
    }

    private fun askPermission() {
        if (!inProgress) {
            inProgress = true
            val request = AskCharacterPermissionRequest(
                userId = Utility.getPreferencesString(
                    this,
                    Constants.USER_ID
                ), characterId = characterId
            )
            permissionsViewModel?.askCharacterPermission(this, request)
        }
    }


    private fun responseData() {
        permissionsViewModel?.simpleResponseResult?.observe(this) { response ->
            mBinding?.clParent?.showStringSnackbar(response.message ?: "")
            permittedToPost = PermittedToPost.PENDING
            buttonText = getString(R.string.permission_requested)
            mBinding?.layoutButton?.btnContinue?.text = buttonText
            manageButton(false)
            inProgress = false
        }
        characterViewModel?.simpleResponseResult?.observe(this) { response ->
            if (response.apiName == ADD_REMOVE_BOOKMARK) {
                bookmarkStatus = !bookmarkStatus
                mBinding?.clParent?.showStringSnackbar(response.message ?: "")
                setBookmarkView()
                favProgress(false)
            }
        }

        //profile
        characterViewModel?.characterResponseResult?.observe(this) { characterResponse ->
            if (characterResponse?.data != null) {
                getData(characterResponse.data)
                revertButtonProgress()
                favProgress(false)
                mBinding?.somethingWrong?.clSomthingWentWrong?.hideView()
                manageShimmer(false)
            } else {
                // manageSomethingWentWrong(true)
            }
        }
    }

    private fun dataLoadingObserver() {
        permissionsViewModel?.updateLoaderStatus?.observe(this) { loading ->
            if (loading.apiName == REQUEST_CHARACTER_PERMISSION) {
                if (loading.status)
                    showButtonProgress()
                else {
                    inProgress = false
                    revertButtonProgress()
                }
            }
            characterViewModel?.updateLoaderStatus?.observe(this) { loadingData ->
                if (loading.apiName == ADD_REMOVE_BOOKMARK) {
                    //favProgress(loadingData.status)
                } else if (loading.apiName == GET_CHARACTER_PROFILE) {
                    if (loadingData.status) {
                        mBinding?.somethingWrong?.clSomthingWentWrong?.hideView()
                        manageShimmer(true)
                    } else {
                        manageShimmer(false)
                    }
                }
            }
        }
    }

    private fun favProgress(loading: Boolean) {
        when (permittedToPost) {
            PermittedToPost.YES -> {
                if (loading) {
                    mBinding?.progressFav?.showView()
                    mBinding?.btnFavourite?.hideView()
                } else {
                    mBinding?.progressFav?.hideView()
                    mBinding?.btnFavourite?.showView()
                }
            }
            PermittedToPost.PENDING -> {

            }
            else -> {

            }
        }

    }

    private fun onGettingException() {
        permissionsViewModel?.exception?.observe(this) { exceptionData ->
            mBinding?.clParent?.showStringSnackbarError(exceptionData.message)
            if (exceptionData.apiName == REQUEST_CHARACTER_PERMISSION) {
                revertButtonProgress()
                inProgress = false
            }

        }
        characterViewModel?.exception?.observe(this) { exceptionData ->
            mBinding?.clParent?.showStringSnackbarError(exceptionData.message)
            favProgress(false)
            if (exceptionData.apiName == GET_CHARACTER_PROFILE) {
                manageShimmer(false)
                if (exceptionData.message == getString(R.string.no_internet))
                    manageSomethingWentWrong(ErrorType.NO_INTERNET)
                else
                    manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
            }
        }
    }

    private fun onGettingError() {
        permissionsViewModel?.error?.observe(this) { errorData ->
            mBinding?.clParent?.showStringSnackbarError(errorData.message)
            if (errorData.apiName == REQUEST_CHARACTER_PERMISSION) {
                revertButtonProgress()
                inProgress = false
            }
        }
        characterViewModel?.error?.observe(this) { errorData ->
            mBinding?.clParent?.showStringSnackbarError(errorData.message)
            favProgress(false)
            if (errorData.apiName == GET_CHARACTER_PROFILE) {
                manageShimmer(false)
                manageSomethingWentWrong(ErrorType.SOMETHING_WENT_WRONG)
            }
        }
    }


    private fun showButtonProgress() {
        mBinding?.layoutButton?.btnContinue?.text = ""
        mBinding?.layoutButton?.progressBar?.showView()
    }

    private fun revertButtonProgress() {
        mBinding?.layoutButton?.btnContinue?.text = buttonText
        mBinding?.layoutButton?.progressBar?.hideView()
    }


    /************************SETUP VIEW-PAGER TABS*************************************/
    private fun initViewPager(characterViewModel: CharacterViewModel, characterId: String) {
        mBinding?.viewPager?.adapter =
            ViewPagerFragmentAdapter(
                this,
                bundleData,
                characterViewModel,
                characterId,
                permittedToPost
            )
        TabLayoutMediator(
            mBinding?.tabLayout!!, mBinding?.viewPager!!
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()

        if (permittedToPost != PermittedToPost.YES)
            addIconsToTab()
    }

    private fun addIconsToTab() {
        mBinding?.tabLayout?.getTabAt(1)?.setIcon(R.drawable.ic_lock)
        restrictTouch()
    }

    private fun restrictTouch() {
        val tabStrip = mBinding?.tabLayout?.getChildAt(0) as LinearLayout
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).setOnTouchListener { p0, p1 ->
                if (i == 1)
                    toast(getString(R.string.need_permission))
                true
            }
        }
        mBinding?.viewPager?.isUserInputEnabled = false //enable disable swipe for viewPager2
    }

    private class ViewPagerFragmentAdapter(
        fragmentActivity: FragmentActivity,
        val bundleData: Bundle?,
        val characterViewModel: CharacterViewModel,
        val characterId: String,
        val permittedToPost: PermittedToPost,
    ) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            /*when (position) {
                0 -> return AboutCharacterFragment(bundleData, characterViewModel, permittedToPost)
                1 -> return CharacterTimelineFragment(characterId)
            }*/
            //return AboutCharacterFragment(bundleData, characterViewModel, permittedToPost)

            return CharacterTimelineFragment.newInstance(characterId, permittedToPost)
        }

        override fun getItemCount(): Int {
            return 1
        }
    }

    private fun manageButton(activateButton: Boolean) {
        if (!activateButton) {
            mBinding?.layoutButton?.btnContinue?.alpha = 0.4f
            mBinding?.layoutButton?.btnContinue?.isClickable = false
        } else {
            mBinding?.layoutButton?.btnContinue?.alpha = 1f
            mBinding?.layoutButton?.btnContinue?.isClickable = true
        }

    }

    private fun manageSomethingWentWrong(errorType: ErrorType) {
        //this.errorType = errorType
        mBinding?.somethingWrong?.clSomthingWentWrong?.showView()
        mBinding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.no_internet_connect_new
            )
        )
        // ivRefresh.showView()
        mBinding?.somethingWrong?.tvTapToRetry?.showView()
        if (errorType == ErrorType.NO_DATA_AVAILABLE) {
            mBinding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.mipmap.no_data_avail_new
                )
            )
            mBinding?.somethingWrong?.tvOops?.setTextOnTextView(getString(R.string.no_data_available), "")
            mBinding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_data_available), "")
        } else if (errorType == ErrorType.NO_INTERNET) { //no internet
            mBinding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.no_internet_connect_new
                )
            )
            mBinding?.somethingWrong?.tvOops?.setTextOnTextView(getString(R.string.tv_oops), "")
            mBinding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.no_internet), "")
        } else {
            mBinding?.somethingWrong?.ivSomethingWentWrong?.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.no_internet_connect_new))
            mBinding?.somethingWrong?.tvOops?.setTextOnTextView(getString(R.string.tv_oops), "")
            mBinding?.somethingWrong?.tvSomethingWentWrong?.setTextOnTextView(getString(R.string.something_went_wrong), "")
        }

    }

    private fun manageShimmer(showShimmer: Boolean) {
        if (showShimmer) {
            //view_pager.hideView()
            mBinding?.shimmerLay?.shimmerLayoutPostList?.showView()
            mBinding?.shimmerLay?.shimmerLayoutPostList?.startShimmer()
            //  clSomthingWentWrong.hideView()
        } else {
            //view_pager.showView()
            mBinding?.shimmerLay?.shimmerLayoutPostList?.stopShimmer()
            mBinding?.shimmerLay?.shimmerLayoutPostList?.hideView()
        }
    }

    companion object {
        var isBackFromSplash=false
        fun open(currActivity: Activity, bundleData: Bundle) {
            val intent = Intent(currActivity, AboutCharacterActivity::class.java)
            intent.putExtras(bundleData)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }

        var profileUpdatedObserver = MutableLiveData<Boolean>()

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBackPressed() {
        if(isBackFromSplash)
            DashboardActivity.open(this, bundleOf())
        else
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
    private fun backPress(){
        if(from ==  DYNAMIC_LINK){
            bundle?.putString(FROM, Constants.DYNAMIC_LINK)
            val i = Intent(this, DashboardActivity::class.java)
            startActivity(i)
        }else {
            onBackPressed()
        }
    }

    override fun deleteBio(id: String) {
        var logoutDialog: ConfirmationDialog? = null
        logoutDialog = ConfirmationDialog(
            this,
            object : ConfirmationDialogListener {
                override fun onYes() {
                    characterViewModel?.deleteBio(characterId, this@AboutCharacterActivity)
                }

                override fun onNo() {
                    logoutDialog?.dismiss()
                }
            },
            getString(R.string.delete_bio_confirmation),
            getString(R.string.confirmation_delete_bio),
            Constants.FROM_DELETE_POST
        )
        logoutDialog.show()

    }

    override fun shareBio(id: String) {
        createInvitationLink()
    }

    private fun setObserver() {
        characterViewModel?.simpleResponseResult?.observe(this)
        { response ->
            if (response != null) {
                if (response.apiName == DELETE_BIO) {
                    mBinding?.clParent?.showStringSnackbar(response.message.toString())
                    lifecycleScope.launch {
                        // delay(2000)

                        val returnIntent = Intent()
                        //returnIntent.putExtra(Constants.SELECTED_LIST, )
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                }
            }
        }
    }

    override fun onEditClick(character: CharacterData) {

    }

    override fun onDeleteClick(character: CharacterData) {

    }

    override fun onReportClick(character: CharacterData) {

    }

    private fun createInvitationLink() {
        val userId = Utility.getPreferencesString(this, Constants.CHARACTER_ID)
        val link = "https://familypedia.page.link/share-biography-link/?id=$characterId"
        println("::$link")
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(link))
            .setDomainUriPrefix("https://familypedia.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID,).setMinimumVersion(BuildConfig.VERSION_CODE)
                    .build()
            )
            .setIosParameters(
                DynamicLink.IosParameters.Builder("com.family.pedia").setAppStoreId("123456789")
                    .setMinimumVersion("1.0.1").build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener { shortDynamicLink ->

                mInvitationUrl =
                    String.format(resources.getString(R.string.invitation_text_for_whatsapp),
                        "  $characterName "
                    ) +" " +shortDynamicLink.shortLink.toString()
                shareViaWhatsapp()
            }.addOnFailureListener { exception ->
                toast(getString(R.string.failed_to_generate_link))
            }
    }
    private fun shareViaWhatsapp() {
        if (mInvitationUrl.isNotEmpty()) {
            val whatsappIntent = Intent(Intent.ACTION_SEND)
            whatsappIntent.type = "text/plain"
            whatsappIntent.setPackage("com.whatsapp")
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, mInvitationUrl)
            try {
                startActivity(whatsappIntent)
            } catch (ex: ActivityNotFoundException) {
                toast(getString(R.string.whatsapp_not_installed))
            }
        } else {
            toast(getString(R.string.wait_for_link))
        }
    }
}