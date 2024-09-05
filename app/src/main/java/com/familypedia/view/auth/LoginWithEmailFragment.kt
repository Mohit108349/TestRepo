package com.familypedia.view.auth

import android.content.Context
import android.os.Bundle
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.familypedia.R
import com.familypedia.databinding.FragmentLoginBinding
import com.familypedia.network.LoginRequest
import com.familypedia.network.ResendOtpRequest
import com.familypedia.utils.Constants
import com.familypedia.utils.Constants.FROM_EMAIL
import com.familypedia.utils.Constants.LANGUAGE_CODE_ENGLISH
import com.familypedia.utils.Constants.LANGUAGE_CODE_SPANISH
import com.familypedia.utils.FamilyPediaClickListener
import com.familypedia.utils.Utility
import com.familypedia.utils.Utility.isValidEmail
import com.familypedia.utils.Utility.isValidPassword
import com.familypedia.utils.familyPediaClickListener



private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LoginWithEmailFragment : Fragment(), FamilyPediaClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var callback: OnClickCallback? = null
    private var email: String = ""
    private var password: String = ""
    lateinit var signupwithEmailmail :TextView
    lateinit var signUpWithPhone :TextView
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
        // Inflate the layout using View Binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)


        // Inflate the layout for this fragment

        val roor = inflater.inflate(R.layout.fragment_login, container, false)
        signupwithEmailmail = roor.findViewById(R.id.signupwithEmailmail)
        signupwithEmailmail.setOnClickListener{
            findNavController().navigate(R.id.action_authLoginTypeFragment_to_signupWithEmailFragment)
        }
        signUpWithPhone = roor.findViewById(R.id.SignupWithPhone)
        signUpWithPhone.setOnClickListener{
            findNavController().navigate(R.id.action_authLoginTypeFragment_to_signUpWithPhoneFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFlow()


    }

    private fun initializeFlow() {
        binding.layoutLogin.btnContinue.text = getString(R.string.login)
        setSpan()
        setClickListeners()
        forgetPassword()
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilPassword.isErrorEnabled = false
                binding.ivShowHide.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }



    private fun setClickListeners() {
        var isVisible = false

        binding.ivShowHide.setOnClickListener {
            if (isVisible) {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowHide.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowHide.setBackgroundResource(R.drawable.password_invisible)
            }
        }
        binding.tvForgotPassword.familyPediaClickListener(this)
        binding.layoutLogin.btnContinue.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.tvForgotPassword -> callback?.onForgotPasswordClick()
            binding.layoutLogin.btnContinue -> onLoginButtonClick()
        }
    }

    private fun setSpan() {
        val spannable = SpannableString(getString(R.string.don_t_have_an_account_sign_up))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                //ds.color = context.resources.getColor(R.color.orangeButton)
            }

            override fun onClick(widget: View) {
                //SignupActivity.open(requireActivity())
                callback?.onSignupClick();

            }
        }

        if (Utility.getCurrentLocale(requireActivity()) == LANGUAGE_CODE_ENGLISH) {
            spannable.setSpan(
                clickableSpan,
                23,
                30,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.color_blue_light)),
                23,
                30,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (Utility.getCurrentLocale(requireActivity()) == LANGUAGE_CODE_SPANISH) {
            spannable.setSpan(
                clickableSpan,
                19,
                29,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.color_blue_light)),
                19,
                29,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
//        tvDontHaveAccount.text = spannable
//        tvDontHaveAccount.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun onLoginButtonClick() {
        if (validateFields()) {
            binding.layoutLogin.btnContinue.isClickable = false
            val loginRequest = LoginRequest(email, password,null,null)
            callback?.onLoginButtonClick(loginRequest, binding.layoutLogin.btnContinue, getString(R.string.login))
        }
    }

    private fun forgetPassword(){
        binding.tvForgotPassword.setOnClickListener {
            email = binding.etEmail.text.toString().trim()
            if (email.isNotBlank() && isValidEmail(email)) {
                val resendOtp = ResendOtpRequest(email, null, null)
                callback?.onResendOtpButtonClick(resendOtp, Constants.FROM_FORGOT_PASSWORD)
            } else if (!isValidEmail(email)) {
                binding.tilEmail.error = getString(R.string.error_email_invalid)

            }
            else
            {
                binding.tilEmail.error = getString(R.string.error_email_empty)

            }


        }
    }

    private fun validateFields(): Boolean {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString()
        removeErrors()
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_empty)
            return false
        } else if (!isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.error_email_invalid)
            return false
        } else if (password.isEmpty()) {
            binding. tilPassword.error = getString(R.string.error_password_empty)
            binding.ivShowHide.isVisible=false
            return false
        } else if (!isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.error_password_invalid)
            binding.ivShowHide.isVisible=false
            return false
        }
        return true

    }

    private fun removeErrors() {
        binding.tilEmail.isErrorEnabled = false
        binding.tilPassword.isErrorEnabled = false
    }
}