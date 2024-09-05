package com.familypedia.view.auth


import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.familypedia.R
import com.familypedia.databinding.FragmentSignupWithEmailBinding
import com.familypedia.network.SignupRequestNew
import com.familypedia.utils.*
import com.familypedia.utils.Constants.PRIVACY_POLICY
import com.familypedia.utils.Constants.TERMS_AND_CONDITIONS
import com.familypedia.utils.Utility.isValidEmail
import com.familypedia.utils.Utility.isValidPassword
import com.familypedia.view.dashboard.profile.AboutUsTnCPrivacyPolicyActivity
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SignupWithEmailFragment : Fragment(), FamilyPediaClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var currActivity: Activity? = null
    private var callback: OnClickCallback? = null
    private var name: String = "";
    private var email: String? = null
    private var type: String = "email"
    private var password: String = ""
    private var confirmPassword: String = ""
    private var _binding: FragmentSignupWithEmailBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as OnClickCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSignupWithEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        currActivity = requireActivity()
        initializeFlow()
    }

    private fun initializeFlow() {
        setViews()
        setSpan()
        setClickListeners()
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilPassword.isErrorEnabled = false
                binding.tilConfirmPassword.isErrorEnabled = false
                binding.ivShowHidesConformEmail.isVisible=true
                binding.ivShowHideNormalEmail.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding. tilPassword.isErrorEnabled = false
                binding.tilConfirmPassword.isErrorEnabled = false
                binding.ivShowHidesConformEmail.isVisible=true
                binding. ivShowHideNormalEmail.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun setViews() {
        binding.layoutSignup.btnContinue.text = getString(R.string.sign_up)
//        etName.setFilters(arrayOf(
//            InputFilter { src, start, end, dst, dstart, dend ->
//                if (src == "") { // for backspace
//                    return@InputFilter src
//                }
//                if (src.toString().matches(Regex("[a-zA-Z ]+"))) {
//                    src
//                } else etName.getText().toString()
//            }
//        ))
    }

    private fun setClickListeners() {

        var isVisible = false
        binding.layoutSignup.btnContinue.setOnClickListener {
            val direction =
                SignupWithEmailFragmentDirections.signupWithEmailFragmentToVerificationFragment(
                    Constants.FROM_EMAIL,email?:"",null)
            findNavController().navigate(direction)
        }
        binding.OptionPhn.setSafeOnClickListener {
            // findNavController().navigate(R.id.action_signupWithEmailFragment_to_signUpWithPhoneFragment)

            findNavController().navigate(
                SignupWithEmailFragmentDirections.actionSignupWithEmailFragmentToSignUpWithPhoneFragment())

        }
        binding.ivShowHideNormalEmail.setOnClickListener {
            if (isVisible) {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowHideNormalEmail.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowHideNormalEmail.setBackgroundResource(R.drawable.password_invisible)
            }
        }
        binding.ivShowHidesConformEmail.setOnClickListener {
            if (isVisible) {
                binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowHidesConformEmail.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowHidesConformEmail.setBackgroundResource(R.drawable.password_invisible)
            }
        }


        /* etName.setFilters(
             arrayOf<InputFilter>(
                 Utility.BLOCK_SPACE,
             )
         )*/

        binding.layoutSignup.btnContinue.familyPediaClickListener(this)
        /* etName.addTextChangedListener(object : TextWatcher {
             override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

             }

             override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                 if (p0.toString().contains("\\s"))
                 etName.setText(p0.toString().replace("\\s", ""))
             }

             override fun afterTextChanged(p0: Editable?) {
             }

         })*/
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.layoutSignup.btnContinue -> {
                if (validateFields()) {
                    binding.layoutSignup.btnContinue.isClickable = false
                    val signupRequest = SignupRequestNew(
                        name = name,
                        password = password,
                        type = type,
                        countryCode =null,
                        phoneNumber = null,
                        email = email)
                    callback?.onSignupButtonClick(signupRequest, getString(R.string.sign_up))


                    /*createDynamicLink(email)*/
                    /* val signupRequest = SignupRequest(name, email, password)
                     callback?.onSignupButtonClick(signupRequest,getString(R.string.sign_up))*/
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        name = binding.etName.text.toString()
        val capName = Utility.capitalize(name)
        name = capName
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString()
        confirmPassword = binding.etConfirmPassword.text.toString()
        removeErrors()

        if (name.trim().isEmpty()) {
            binding.tilName.error = getString(R.string.error_name)
            return false
        } else if (email.isNullOrEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_empty)
            return false
        } else if (!isValidEmail(email ?: "")) {
            binding.tilEmail.error = getString(R.string.error_email_invalid)
            return false
        } else if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_empty)
            binding.ivShowHideNormalEmail.isVisible=false
            return false
        } else if (!isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.error_password_invalid)
            binding.ivShowHideNormalEmail.isVisible=false
            return false
        } else if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_confirm_password_empty)
            binding.ivShowHidesConformEmail.isVisible=false
            return false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_confirm_password_unmatch)
            binding.ivShowHidesConformEmail.isVisible=false
            return false
        } else if (binding.cbTerms.isChecked==false) {
            binding.clParent.showStringSnackbarError(getString(R.string.error_tnc))
            return false
        }

        return true
    }

    private fun removeErrors() {
        binding.tilName.isErrorEnabled = false
        binding.tilEmail.isErrorEnabled = false
        binding.tilPassword.isErrorEnabled = false
        binding.tilConfirmPassword.isErrorEnabled = false
    }

    private fun setSpan() {
        //TERMS
        val spannableTerms =
            SpannableString(getString(R.string.i_accept_terms_amp_conditions_and_privacy_policy))
        val clickableSpanTerms: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                AboutUsTnCPrivacyPolicyActivity.open(requireActivity(), TERMS_AND_CONDITIONS)
            }
        }
        val clickablePolicy: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                AboutUsTnCPrivacyPolicyActivity.open(requireActivity(), PRIVACY_POLICY)
            }
        }
        if (Utility.getCurrentLocale(requireActivity()) == Constants.LANGUAGE_CODE_ENGLISH) {
            spannableTerms.setSpan(
                clickableSpanTerms,
                9,
                27,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableTerms.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.color_blue_light)),
                9,
                27,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannableTerms.setSpan(
                clickablePolicy,
                32,
                46,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableTerms.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.color_blue_light)),
                32,
                46,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (Utility.getCurrentLocale(requireActivity()) == Constants.LANGUAGE_CODE_SPANISH) {
            spannableTerms.setSpan(
                clickableSpanTerms,
                11,
                33,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableTerms.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.color_blue_light)),
                11,
                33,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannableTerms.setSpan(
                clickablePolicy,
                35,
                58,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableTerms.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.color_blue_light)),
                35,
                58,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

        }
        binding.cbTerms.text = spannableTerms
        binding.cbTerms.movementMethod = LinkMovementMethod.getInstance()


        //ALREADY HAVE ACCOUNT
        val spannable = SpannableString(getString(R.string.already_have_an_account_log_in))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                currActivity?.onBackPressed()
            }
        }
        if (Utility.getCurrentLocale(requireActivity()) == Constants.LANGUAGE_CODE_ENGLISH) {

            spannable.setSpan(
                clickableSpan,
                25,
                30,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.color_blue_light)),
                25,
                30,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (Utility.getCurrentLocale(requireActivity()) == Constants.LANGUAGE_CODE_SPANISH) {
            spannable.setSpan(
                clickableSpan,
                19,
                32,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.color_blue_light)),
                19,
                32,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

        }
        binding.tvAlreadyHaveAccount.text = spannable
        binding.tvAlreadyHaveAccount.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun createDynamicLink(email: String) {
        val link = "https://familypedia.page.link/verify-my-account/?email=$email"
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(link))
            .setDomainUriPrefix("https://familypedia.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.familypedia").setMinimumVersion(125)
                    .build()
            )
            .setIosParameters(
                DynamicLink.IosParameters.Builder("com.family.pedia").setAppStoreId("123456789")
                    .setMinimumVersion("1.0.1").build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener { shortDynamicLink ->
                val dynamicLink = shortDynamicLink.shortLink.toString()
                Log.v("jasdhghas", "" + dynamicLink);
                val signupRequest = SignupRequestNew(name, password,email,"","" ,dynamicLink)
                callback?.onSignupButtonClick(signupRequest, getString(R.string.sign_up))
            }.addOnFailureListener { exception ->
                requireActivity().toast(getString(R.string.failed_to_generate_link))
            }
    }
}