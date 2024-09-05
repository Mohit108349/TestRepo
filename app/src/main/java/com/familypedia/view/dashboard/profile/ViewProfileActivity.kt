package com.familypedia.view.dashboard.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.familypedia.R
import com.familypedia.databinding.ActivityViewProfileBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_FRIEND_REQUEST
import com.familypedia.utils.Constants.FROM_PROFILE_FRAGMENT
import com.familypedia.utils.Constants.FROM_VIEW_USER
import com.familypedia.utils.Constants.NOTIFICATION
import com.familypedia.utils.Constants.USER_DATA
import com.familypedia.utils.Constants.USER_ID
import com.familypedia.utils.listeners.UserListener
import com.familypedia.view.dashboard.character.CharacterByUserActivity
import com.familypedia.view.dashboard.character.adapter.CharactersAdapter
import com.familypedia.view.dashboard.character.adapter.CharactersListener
import com.familypedia.view.dashboard.friends.SearchFriendsActivity.Companion.friedRequestLiveData
import com.familypedia.viewmodels.FriendsViewModel


class ViewProfileActivity : AppCompatActivity(), FamilyPediaClickListener, UserListener,
    ConfirmationDialogListener, SuccessFulListener, UnfriendConfirmationDialogListener {
    private var from = ""
    private var friendsViewModel: FriendsViewModel? = null
    private var userId: String = ""
    private var defaultText = ""
    private var friendRequestStatus = ""
    private var currentUserId: String? = null
    private var inProgress = false
    private var isBlocked = false
    private var isFriend = false
    private var user_friend: User? = null
    private var action_type = ""
    private var myUserId = ""
    private var userData: User? = null
    private lateinit var binding: ActivityViewProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_profile)
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        currentUserId = Utility.getPreferencesString(this, USER_ID) ?: ""
        defaultText = getString(R.string.n_a)
        from = intent.extras?.getString(FROM) ?: ""
        setClickListeners()

        if (from == FROM_PROFILE_FRAGMENT) {
            binding.isOwnProfile = true
            binding.toolbarViewProfile.ivOptions.hideView()
            binding.toolbarViewProfile.ivEdit.showView()
            binding.toolbarViewProfile.tvToolbarTitle.text = getString(R.string.view_profile)
            userId = Utility.getPreferencesString(this, Constants.USER_ID)
        } else if (from == FROM_FRIEND_REQUEST || from == FROM_VIEW_USER || from == NOTIFICATION) {

            if (from == FROM_FRIEND_REQUEST)
                binding.toolbarViewProfile.tvToolbarTitle.text = getString(R.string.user_detail)
            else if (from == FROM_VIEW_USER)
                binding.toolbarViewProfile.tvToolbarTitle.text = getString(R.string.user_detail)
            else if (from == NOTIFICATION)
                binding.toolbarViewProfile.tvToolbarTitle.text = getString(R.string.notification_detail)

            userId = intent.extras?.getString(USER_ID) ?: ""
            myUserId = Utility.getPreferencesString(this, Constants.USER_ID)
            if (userId == myUserId) {
                binding.isOwnProfile = true
                binding.toolbarViewProfile.ivOptions.hideView()
                binding.toolbarViewProfile.ivEdit.showView()
            } else {
                binding.toolbarViewProfile.ivEdit.hideView()
                binding.toolbarViewProfile.ivOptions.showView()
            }
        }
        initViewModel()
        responseData()
        dataLoading()
        onGettingException()
        onGettingError()
    }

    override fun onResume() {
        super.onResume()
        if (from == FROM_PROFILE_FRAGMENT) {
            setupMyProfileViewsData()
        }
    }

    private fun setClickListeners() {
        binding.toolbarViewProfile.ivBack.familyPediaClickListener(this)
        binding.toolbarViewProfile.ivEdit.familyPediaClickListener(this)
        binding.toolbarViewProfile.ivOptions.familyPediaClickListener(this)
        binding.btnSendFriendRequest.btnContinue.familyPediaClickListener(this)
        binding.btnAccept.familyPediaClickListener(this)
        binding. btnReject.familyPediaClickListener(this)
        binding.btnSeeAllBiographies.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.toolbarViewProfile.ivBack -> onBackPressed()
            binding.toolbarViewProfile. ivEdit -> EditProfileActivity.open(this)
            binding.toolbarViewProfile.ivOptions -> Utility.showPopupProfile(
                binding.toolbarViewProfile.ivOptions, this@ViewProfileActivity, this,
                userId, isBlocked
            )
            binding.btnSendFriendRequest.btnContinue -> {
                if (!isFriend) {
                    hitSendFriendRequest()
                } else
                    hitUnfriendUser()
            }
            binding.btnAccept -> acceptRejectFriendRequest(true)
            binding.btnReject -> acceptRejectFriendRequest(null)
            binding.btnSeeAllBiographies -> {
                if (isBlocked) {
                    action_type = "unblock"
                    showUnblockDialog()
                } else {
                    val bundle = Bundle()
                    bundle.putSerializable(USER_DATA, userData)
                    CharacterByUserActivity.open(this, bundle)
                }
            }
        }
    }

    private fun setupMyProfileViewsData() {
        binding.tvName.setTextOnTextView(
            Utility.getPreferencesString(this, Constants.USER_NAME),
            defaultText
        )
        binding.tvEmail.setTextOnTextView(
            Utility.getPreferencesString(this, Constants.EMAIL), defaultText
        )
        binding.tvGender.setTextOnTextView(
            Utility.getPreferencesString(this, Constants.GENDER),
            defaultText
        )
        val phnNumber =  Utility.getPreferencesString(this, Constants.PHONE_NUMBER)
        if (phnNumber.isNotBlank()){
            val countryCode = Utility.getPreferencesString(this, Constants.COUNTRY_CODE)
            binding.tvPhoneNumber.setTextOnTextView(countryCode + phnNumber, defaultText)
        }else{
            binding.tvPhoneNumber.text = getString(R.string.n_a)
        }

        binding.tvDOB.setTextOnTextView(
            Utility.getPreferencesString(this, Constants.DATE_OF_BIRTH),
            defaultText
        )
        binding. tvCountry.setTextOnTextView(
            Utility.getPreferencesString(this, Constants.COUNTRY),
            defaultText
        )
        binding.tvCity.setTextOnTextView(
            Utility.getPreferencesString(this, Constants.CITY),
            defaultText
        )
        binding.ivProfileImage.loadImagesWithGlide(
            Constants.IMAGE_URL + Utility.getPreferencesString(
                this,
                Constants.PROFILE_PIC
            ),
            true
        )
    }

    private fun setBiographiesAdapter(characterList: List<CharacterData>) {
        binding.rvBiographies.adapter = CharactersAdapter(this, characterList, characterListener, "",null,blockedState = isBlocked)
    }

    private val characterListener = object : CharactersListener {
        override fun onListSizeChanged(size: Int) {

        }

        override fun onEditCharacterClicked(characterData: CharacterData) {

        }

        override fun onBlock(boolean: Boolean) {
            action_type = "unblock"
           showUnblockDialog()
        }
    }

    private fun setupMyProfileViewsDataFromAPI(
        characterOuterData: CharacterOuterData,
        user: User,
        status: String
    ) {
        userData = user

        binding.biographiesCount = characterOuterData.totalDocs ?: 0
        //
        friendRequestStatus = ""
        user_friend = user
        binding.tvName.setTextOnTextView(user.name, defaultText)
        binding.tvEmail.setTextOnTextView(user.email, defaultText)
        binding.tvGender.setTextOnTextView(user.gender, defaultText)
        val phnNumber =  user.phone_number?:""
        if (phnNumber.isNotBlank()){
            val countryCode = user.country_code
            binding.tvPhoneNumber.setTextOnTextView(countryCode + phnNumber, defaultText)
        }else{
            binding.tvPhoneNumber.text = getString(R.string.n_a)
        }


/*        user.phone_number?.let {
            tvPhoneNumber.setTextOnTextView(user.country_code + user.phone_number, defaultText)
        }*/
        binding.tvDOB.setTextOnTextView(user.date_of_birth, defaultText)
        binding.tvCountry.setTextOnTextView(user.country, defaultText)
        binding.tvCity.setTextOnTextView(user.city, defaultText)
        binding.ivProfileImage.loadImagesWithGlide(
            Constants.IMAGE_URL + user.profile_pic, true
        )

        //status = 'already friends',status = 'pending request',status = 'receive friend request',status = 'not friends'
        if (!user._id.equals(currentUserId)) {
            when (status) {
                "already friends" -> {
                    isFriend = true
                    binding.btnSendFriendRequest.btnContinue.showView()
                    binding.llReceivedFriendRequest.hideView()
                    binding.btnSendFriendRequest.btnContinue.text = getString(R.string.unfriend)
                }
                "pending request" -> {
                    binding.llReceivedFriendRequest.hideView()
                    binding.btnSendFriendRequest.btnContinue.showView()
                    binding.btnSendFriendRequest.btnContinue.text = getString(R.string.request_pending)
                    manageButton(false)
                }
                "receive friend request" -> {
                    binding.btnSendFriendRequest.btnContinue.hideView()
                    binding.llReceivedFriendRequest.showView()
                }
                "not friends" -> {
                    binding.llReceivedFriendRequest.hideView()
                    binding.btnSendFriendRequest
                    binding.btnSendFriendRequest.btnContinue.text = getString(R.string.send_friend_request)
                    manageButton(true)
                }
                "blocked" -> {
                    isBlocked = true
                    binding.llReceivedFriendRequest.hideView()
                    binding.btnSendFriendRequest.btnContinue.hideView()
                 }
                else -> {
                    binding.btnSendFriendRequest.btnContinue.hideView()
                    binding.llReceivedFriendRequest.hideView()
                }
            }
        } else {
            binding.btnSendFriendRequest.btnContinue.hideView()
            binding.llReceivedFriendRequest.hideView()
        }

        characterOuterData.data?.let {
            setBiographiesAdapter(it)
        }
    }

    private fun manageButton(activateButton: Boolean) {
        if (!activateButton) {
            binding.btnSendFriendRequest.btnContinue.alpha = 0.4f
            binding.btnSendFriendRequest.btnContinue.isClickable = false
        } else {
            binding.btnSendFriendRequest.btnContinue.alpha = 1f
            binding.btnSendFriendRequest.btnContinue.isClickable = true
        }
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        friendsViewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)
        friendsViewModel?.init()
        hitGetProfileAPI()
    }

    private fun hitGetProfileAPI() {
        friendsViewModel?.getFriendProfile(this, userId)
    }

    private fun hitSendFriendRequest() {
        if (!inProgress) {
            inProgress = true
            friendsViewModel?.sendFriendRequest(this, userId)
        }
    }

    private fun hitUnfriendUser() {
        showUnfriendDialog()
    }

    var unfriendDialog: UnfriendConfirmationDialog? = null
    private fun showUnfriendDialog() {
        unfriendDialog = UnfriendConfirmationDialog(
            this,
            this,
            getString(R.string.unfriend),
            String.format(resources.getString(R.string.confirm_unfriend), user_friend?.name),
            user_friend
        )
        unfriendDialog?.show()
    }

    private var acceptRejectStatus: Boolean? = false
    private fun acceptRejectFriendRequest(status: Boolean?) {
        acceptRejectStatus = status
        if (!inProgress) {
            inProgress = true
            val request = AcceptRejectFriendRequest(userId, status)
            friendsViewModel?.acceptRejectFriendRequest(this, request)
        }

    }

    private fun responseData() {
        friendsViewModel?.friendsProfileResponseResult?.observe(this) { profile ->
            if (profile.data?.friend != null) {

                setupMyProfileViewsDataFromAPI(
                    profile.data.character,
                    profile.data?.friend,
                    profile.data.status ?: ""
                )
            }
        }
        friendsViewModel?.simpleResponseResult?.observe(this) { response ->
            if (response.apiName == SEND_FRIEND_REQUEST) {
                inProgress = false
                binding.btnSendFriendRequest.btnContinue.text = getString(R.string.request_pending)
                manageButton(false)
                binding.clParent?.showStringSnackbar(response.message ?: "")

                friedRequestLiveData.postValue(
                    FriendStatus(
                        FRIEND_STATUS_ENUM.REQUEST_SENT,
                        userId,
                        true
                    )
                )
            } else if (response.apiName == ACCEPT_REJECT_FRIEND_REQUEST) {
                inProgress = false
                binding.llReceivedFriendRequest.hideView()
                binding.clParent?.showStringSnackbar(response.message ?: "")
                acceptRejectStatus?.let { status ->
                    friedRequestLiveData.postValue(
                        FriendStatus(
                            FRIEND_STATUS_ENUM.ACCEPT_REJECT,
                            userId,
                            status
                        )
                    )
                }
            } else if (response.apiName == REPORT_USER) {
                SuccessfulDialog(
                    this@ViewProfileActivity, Constants.FROM_REPORT_USER,
                    this,
                    getString(R.string.user_reported),
                    response.message.toString()
                ).show()
            } else if (response.apiName == BLOCK_USER) {
                binding.llReceivedFriendRequest.hideView()
                inProgress = false
                isBlocked = true
                binding.btnSendFriendRequest.btnContinue.hideView()
                SuccessfulDialog(
                    this@ViewProfileActivity, Constants.FROM_BLOCK_USER,
                    this,
                    getString(R.string.user_blocked),
                    response.message.toString()
                ).show()
                hitGetProfileAPI()
            } else if (response.apiName == UNFRIEND_USER) {
                isFriend = false
                inProgress = false
                binding.btnSendFriendRequest.btnContinue.text = getString(R.string.send_friend_request)
                manageButton(true)
                binding.clParent?.showStringSnackbar(response.message ?: "")
            } else if (response.apiName == UNBLOCK_USER) {
                isBlocked = false
                isFriend = false
                binding.btnSendFriendRequest.btnContinue.showView()
                binding.btnSendFriendRequest.btnContinue.text = getString(R.string.send_friend_request)
                binding.clParent?.showStringSnackbar(response.message ?: "")
                hitGetProfileAPI()
            }
        }
    }


    private fun dataLoading() {
        friendsViewModel?.updateLoaderStatus?.observe(this) { loading ->
            if (loading.status) {
                binding.btnSendFriendRequest.btnContinue.showView()
            } else {
                inProgress = false
                binding.btnSendFriendRequest.btnContinue.hideView()
            }

        }
    }

    private fun onGettingException() {
        friendsViewModel?.exception?.observe(this) { exceptionData ->
            /*if (exceptionData.apiName == FRIEND_PROFILE) {
                hitGetProfileAPI()
            } else {
            */
            inProgress = false
            binding.clParent.showStringSnackbarError(exceptionData.message)
            //}
        }
    }

    private fun onGettingError() {
        friendsViewModel?.error?.observe(this) { errorData ->
            inProgress = false
            binding.clParent.showStringSnackbarError(errorData.message)
        }
    }


    companion object {
        fun open(currActivity: Activity, bundle: Bundle) {
            val intent = Intent(currActivity, ViewProfileActivity::class.java)
            intent.putExtras(bundle)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }



    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }

    override fun sendFriendRequest(user: User) {

    }

    override fun reportUser(user_id: String) {
        action_type = "report"
        showReportUser()
    }

    override fun blockUser(user_id: String) {
        action_type = "block"
        showBlockDialog()
    }

    override fun unBlockUser(user_id: String) {
        action_type = "unblock"
        showUnblockDialog()
    }

    override fun acceptRequest(user: User) {
    }

    override fun rejectRequest(user: User) {
    }

    var blockDialog: BlockConfirmationDialog? = null
    private fun showBlockDialog() {
        blockDialog = BlockConfirmationDialog(
            this,
            this,
            getString(R.string.block),
            getString(R.string.confirm_block),
            user_friend
        )
        blockDialog?.show()
    }

    var unblockDialog: BlockConfirmationDialog? = null
    private fun showUnblockDialog() {
        unblockDialog = BlockConfirmationDialog(
            this,
            this,
            getString(R.string.unblock),
            getString(R.string.confirm_unblock),
            user_friend
        )
        unblockDialog?.show()
    }

    var userReportDialog: BlockConfirmationDialog? = null
    private fun showReportUser() {
        userReportDialog = BlockConfirmationDialog(
            this,
            this,
            getString(R.string.report),
            getString(R.string.confirm_report_user),
            user_friend
        )
        userReportDialog?.show()
    }

    override fun onYes() {
        if (userId.isNotEmpty()) {
            if (action_type == "block")
                friendsViewModel?.blockUser(this, BlockUserRequest(userId))
            if (action_type == "unblock")
                friendsViewModel?.unBlockUser(this, BlockUserRequest(userId))
            if (action_type == "report")
                friendsViewModel?.reportUser(this, ReportUserRequest(userId))
        }
    }

    override fun onNo() {
        if (action_type == "block")
            blockDialog?.dismiss()
        if (action_type == "unblock")
            unblockDialog?.dismiss()
        if (action_type == "report")
            userReportDialog?.dismiss()
    }

    override fun onUpdateSuccessfully(from: String) {

    }

    override fun onDismiss(from: String) {

    }

    override fun onUnfriend() {
        friendsViewModel?.unfriendUser(this, UnfriendUserRequest(userId))
    }

    override fun onDecline() {
        unfriendDialog?.dismiss()
    }


}