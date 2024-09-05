package com.familypedia.view.auth.phone_login

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.familypedia.R
import com.familypedia.databinding.FragmentSignUpWithPhoneBinding
import com.familypedia.network.SignupRequestNew
import com.familypedia.utils.*
import com.familypedia.view.auth.OnClickCallback
import com.familypedia.view.dashboard.profile.AboutUsTnCPrivacyPolicyActivity



class SignUpWithPhoneFragment : Fragment() , FamilyPediaClickListener,AuthDialogListener {
    private var name: String = "";
    private var phoneNumber: String = ""
    private var password: String = ""
    private var confirmPassword: String = ""
    private var type: String = "phone"
    private var countryCode: String =""
     private var currActivity: Activity? = null
    private var callback: OnClickCallback? = null
    private var _binding: FragmentSignUpWithPhoneBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        arguments?.let {

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
        _binding = FragmentSignUpWithPhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currActivity = requireActivity()
        initializeFlow()


    }

    private fun initializeFlow() {
        setSpan()
        clickListener()
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilPassword.isErrorEnabled = false
                binding.tilConfirmPassword.isErrorEnabled = false
                binding.ivShowHideConformPhn.isVisible=true
                binding.ivShowHideNormalPhn.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilPassword.isErrorEnabled = false
                binding.tilConfirmPassword.isErrorEnabled = false
                binding.ivShowHideConformPhn.isVisible=true
                binding.ivShowHideNormalPhn.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }
//    fun onViewClick(view: View) {
//        when (view) {
//            btnContinue -> {
//                if (validateFields()) {
//                    btnContinue.isClickable = false
//                    // createDynamicLink(email)
//                    /* val signupRequest = SignupRequest(name, email, password)
//                     callback?.onSignupButtonClick(signupRequest,getString(R.string.sign_up))*/
//                }
//            }
//        }
//    }

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
                AboutUsTnCPrivacyPolicyActivity.open(requireActivity(),
                    Constants.TERMS_AND_CONDITIONS)
            }
        }
        val clickablePolicy: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                AboutUsTnCPrivacyPolicyActivity.open(requireActivity(), Constants.PRIVACY_POLICY)
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


    private fun validateFields(): Boolean {
        name = binding.etName.text.toString()
        val capName = Utility.capitalize(name)
        name = capName
        countryCode = /*ccpCode.selectedCountryCode*/""
        phoneNumber = binding.etPhoneNumber.text.toString().trim()
        password = binding.etPassword.text.toString()
        confirmPassword = binding.etConfirmPassword.text.toString()
        removeErrors()

        if (name.trim().isEmpty()) {
            binding.tilName.error = getString(R.string.error_name)
            return false
        } else if (phoneNumber.isEmpty()) {
            true.changePhoneTVColor(1)
             return false
        } else if (!Utility.isValidPhone(phoneNumber)) {
             true.changePhoneTVColor(2)
             return false
        } else if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_empty)
            binding.ivShowHideNormalPhn.isVisible=false
            return false
        } else if (!Utility.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.error_password_invalid)
            binding.ivShowHideNormalPhn.isVisible=false
            return false
        } else if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_confirm_password_empty)
            binding.ivShowHideConformPhn.isVisible=false
            return false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_confirm_password_unmatch)
            binding.ivShowHideConformPhn.isVisible=false
            return false
        } else if (binding.cbTerms.isChecked) {
            binding. clParent.showStringSnackbarError(getString(R.string.error_tnc))
            return false
        }

        return true
    }

    private fun removeErrors() {
        binding.tilName.isErrorEnabled = false
        false.changePhoneTVColor(1)
        binding.tilPassword.isErrorEnabled = false
        binding.tilConfirmPassword.isErrorEnabled = false
    }

    private fun clickListener() {
        var isVisible = false
        binding.layoutSignup.btnContinue.setOnClickListener {
//            if (validateFields()) {
//                val signupRequest = SignupRequestNew(
//                    name = name,
//                    password = password,
//                    type = type,
//                    countryCode =countryCode,
//                    phoneNumber = phoneNumber,
//                    email = null)
//                callback?.onSignupButtonClick(signupRequest, getString(R.string.sign_up))
////                val direction =
////                    SignUpWithPhoneFragmentDirections.signUpWithPhoneFragmentToVerificationFragment(
////                        Constants.FROM_PHONE,phoneNumber,countryCode)
////                findNavController().navigate(direction)
//            }

            openAuthDialog(
                Constants.FROM_SIGNUP_CONFLICT,
                getString(R.string.phone_number_not_functional),
                getString(R.string.your_phone_number_not_working),
            )
        }

        binding.OptionEmail.setSafeOnClickListener {
            findNavController().navigate(R.id.action_signUpWithPhoneFragment_to_signupWithEmailFragment)
        }

        binding.ivShowHideNormalPhn.setOnClickListener {
            if (isVisible) {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowHideNormalPhn.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowHideNormalPhn.setBackgroundResource(R.drawable.password_invisible)
            }
        }
        binding.ivShowHideConformPhn.setOnClickListener {
            if (isVisible) {
                binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowHideConformPhn.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowHideConformPhn.setBackgroundResource(R.drawable.password_invisible)
            }
        }
    }

    private fun openAuthDialog(from: String, title: String, desc: String) {
        AuthDialog(
            requireContext(), from,
            this,
            title,
            desc
        ).show()

    }

    private fun Boolean.changePhoneTVColor(flag: Int) {
        if (this) {
            binding.tilPhoneNumber.setTextColor(resources.getColor(R.color.color_error))
            binding.llPhoneNumber.background = resources.getDrawable(R.drawable.bg_red_outline_textview)
            binding.tvErrorMsg.isVisible = true
            if (flag == 1) {
                binding.tvErrorMsg.text = getString(R.string.please_enter_your_phone_number)
            } else {
                binding.tvErrorMsg.text =getString(R.string.error_invalid_phone_number)
            }

        } else {
            binding.tilPhoneNumber.setTextColor(resources.getColor(R.color.text_color_pix))
            binding.tvErrorMsg.isVisible = false
            binding.llPhoneNumber.background = resources.getDrawable(R.drawable.bg_outline_textview)

        }

    }

    override fun onViewClick(view: View) {

    }

    override fun onAuthDialogResendLinkClick(from: String) {
    }

    override fun onAuthDialogDismiss(from: String) {
    }


}