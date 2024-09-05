package com.familypedia.view.dashboard.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.familypedia.DeleteAccountScreenActivity
import com.familypedia.R
import com.familypedia.databinding.FragmentProfileBinding
import com.familypedia.network.LOGOUT
import com.familypedia.tutorial.TutorialActivity
import com.familypedia.utils.*
import com.familypedia.utils.Constants.ABOUT_US
import com.familypedia.utils.Constants.EMAIL
import com.familypedia.utils.Constants.FAQ
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_PROFILE_FRAGMENT
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.Constants.PHONE_NUMBER
import com.familypedia.utils.Constants.PRIVACY_POLICY
import com.familypedia.utils.Constants.PROFILE_PIC
import com.familypedia.utils.Constants.TERMS_AND_CONDITIONS
import com.familypedia.utils.Constants.USER_NAME
import com.familypedia.utils.Utility.getPreferencesString
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.dashboard.friends.YourInvitedFriendsActivity
import com.familypedia.view.dashboard.profile.favouriteCharacter.FavouriteCharactersActivity
import com.familypedia.view.dashboard.profile.permissions.PostPermissionsActivity
import com.familypedia.view.notifications.NotificationsActivity
import com.familypedia.viewmodels.AuthViewModel

import kotlinx.coroutines.launch
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment(), FamilyPediaClickListener, ConfirmationDialogListener,
    SuccessFulListener {
    private var param1: String? = null
    private var param2: String? = null
    private var authViewModel: AuthViewModel? = null
    private var _binding: FragmentProfileBinding? = null
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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setUIData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }

    private fun initializeControl() {
        setListeners()
        initViewModel()
        responseData()
        onGettingError()
        onGettingException()
        showProgressBar()
    }


    private fun setListeners() {
        binding?.llLogout?.familyPediaClickListener(this)
        binding?.llViewProfile?.familyPediaClickListener(this)
        binding?.ivEditProfile?.familyPediaClickListener(this)
        binding?.llChangePassword?.familyPediaClickListener(this)
        binding?.llFavCharacters?.familyPediaClickListener(this)
        binding?.llPostPermissions?.familyPediaClickListener(this)
        binding?.ivNotification?.familyPediaClickListener(this)
        binding?.llAboutUs?.familyPediaClickListener(this)
        binding?.llTnC?.familyPediaClickListener(this)
        binding?.llPrivacyPolicy?.familyPediaClickListener(this)
        binding?.llDeleteAccount?.familyPediaClickListener(this)
        binding?.llInviteYourFriends?.familyPediaClickListener(this)
        binding?.llFAQ?.familyPediaClickListener(this)

        binding?.llTutorial?.familyPediaClickListener(this)
        //tvBlockedByAdmin.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.llLogout -> showLogoutDialog()
            binding?.llViewProfile -> {
                val bundle = Bundle()
                bundle.putString(FROM, FROM_PROFILE_FRAGMENT)
                ViewProfileActivity.open(requireActivity(), bundle)
            }
            binding?.ivEditProfile -> EditProfileActivity.open(requireActivity())
            binding?.llDeleteAccount -> DeleteAccountScreenActivity.open(requireActivity())
            binding?.llChangePassword -> ChangePasswordActivity.open(requireActivity())
            binding?.llFavCharacters -> FavouriteCharactersActivity.open(requireActivity())
            binding?.llPostPermissions -> PostPermissionsActivity.open(requireActivity(), null)
            binding?.ivNotification -> NotificationsActivity.open(requireActivity(), null)
            binding?.llAboutUs -> AboutUsTnCPrivacyPolicyActivity.open(requireActivity(), ABOUT_US)
            binding?.llFAQ -> AboutUsTnCPrivacyPolicyActivity.open(requireActivity(), FAQ)
            binding?.llTnC -> AboutUsTnCPrivacyPolicyActivity.open(requireActivity(), TERMS_AND_CONDITIONS)
            binding?.llPrivacyPolicy -> AboutUsTnCPrivacyPolicyActivity.open(requireActivity(), PRIVACY_POLICY)
            binding?.llInviteYourFriends -> YourInvitedFriendsActivity.open(requireActivity())
            binding?.llTutorial ->TutorialActivity.open(requireActivity())
            //tvBlockedByAdmin->showBlockedByAdminDialog()
        }
    }

    private fun setUIData() {
       Log.d("::-->","${getPreferencesString(requireContext(),EMAIL)}")
        val myString =getPreferencesString(requireContext(), USER_NAME)
        val capitalizedString = myString.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }
        binding?.tvUserName?.text =  capitalizedString
        /*tvUserEmail.text = getPreferencesString(requireContext(), EMAIL)*/
        println("The Value is :: ${getPreferencesString(requireActivity(), Constants.LOGIN_FROM)}")
        val valueToSet = getPreferencesString(requireContext(),EMAIL)
        if (valueToSet.isNotBlank()){
            binding?.tvUserEmail?.setTextOnTextView(
                getPreferencesString(requireActivity(), EMAIL),
                getString(R.string.n_a)
            )
        }
        else{
            val phoneValueToSet = getPreferencesString(requireContext(), PHONE_NUMBER)
            if (phoneValueToSet.isNotBlank()){
                binding?.tvUserEmail?.setTextOnTextView(
                    getPreferencesString(requireActivity(), PHONE_NUMBER),
                    getString(R.string.n_a)
                )
            }
        }

/*        tvUserEmail.setTextOnTextView(
            getPreferencesString(requireActivity(), Constants.LOGIN_FROM),
            getString(R.string.n_a)
        )*/

/*
        tvUserEmail.setTextOnTextView(
            Utility.getPreferencesString(requireActivity(), Constants.LOGIN_FROM),
            getString(R.string.n_a)
        )
*/

        binding?.ivProfileImage?.loadImagesWithGlide(
            IMAGE_URL + getPreferencesString(requireActivity(), PROFILE_PIC),
            true
        )

    }

    var logoutDialog: ConfirmationDialog? = null
    private fun showLogoutDialog() {
        logoutDialog = ConfirmationDialog(
            requireContext(),
            this,
            getString(R.string.logout),
            getString(R.string.confirmation_logout),
            LOGOUT
        )
        logoutDialog?.show()
    }

    private fun showBlockedByAdminDialog() {
        SuccessfulDialog(
            requireContext(), "",
            this,
            getString(R.string.blocked),
            getString(R.string.blocked_desc)
        ).show()

    }

    override fun onYes() {
        //onLogoutPressed
        logoutAPI()
        logoutDialog?.dismiss()

    }

    override fun onNo() {

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

    /*********************************************************VIEWMODELS AND API RESPONSE OBSERVERS***************************************************/

    private fun initViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.init()
    }

    private fun logoutAPI() {
        authViewModel?.logoutAPI(requireContext())
    }

    private fun responseData() {
        authViewModel?.simpleResponseResult?.observe(viewLifecycleOwner) { simpleResponse ->
            if (simpleResponse != null) {
                binding?.clParent?.showStringSnackbar(simpleResponse.message ?: "")
                Utility.clearAllPreferences(requireContext())
                Utility.savePreferencesBoolean(requireActivity(), Constants.IS_WALKTHROUGH_SEEN,true)
                AuthViewsHolderActivity.open(requireActivity(), null)
                requireActivity().finishAffinity()
            }
        }
    }

    private fun showProgressBar() {
        authViewModel?.updateLoaderStatus?.observe(viewLifecycleOwner) {
            if (it.status) {
                requireActivity().window?.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                binding?.llLoggingOut?.showView()
            } else {
                requireActivity().window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding?.llLoggingOut?.hideView()
            }
        }
    }

    private fun onGettingException() {
        authViewModel?.exception?.observe(viewLifecycleOwner) {
            binding?.clParent?.showStringSnackbarError(it.message)
        }
    }

    private fun onGettingError() {
        authViewModel?.error?.observe(viewLifecycleOwner) { errorData ->
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
            }
        }
    }

}