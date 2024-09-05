package com.familypedia.view.auth.phone_login

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.familypedia.R
import com.familypedia.databinding.FragmentLoginWithPhoneBinding
import com.familypedia.network.LoginRequest
import com.familypedia.network.ResendOtpRequest
import com.familypedia.utils.Constants
import com.familypedia.utils.FamilyPediaClickListener
import com.familypedia.utils.Utility
import com.familypedia.utils.familyPediaClickListener
import com.familypedia.view.auth.OnClickCallback
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LoginWithPhoneFragment : Fragment(), FamilyPediaClickListener {

    private var param1: String? = null
    private var param2: String? = null
    private var phoneNumber: String = ""
    private var password: String = ""
    private var countryCode: String =""
    private var callback: OnClickCallback? = null
    lateinit var signupwithEmailmail: TextView
    lateinit var signUpWithPhone: TextView
    private var _binding: FragmentLoginWithPhoneBinding? = null
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
        // Initialize View Binding
        _binding = FragmentLoginWithPhoneBinding.inflate(inflater, container, false)


        binding.layoutsignupwithEmailmail.signupwithEmailmail.setOnClickListener {
            findNavController().navigate(R.id.action_authLoginTypeFragment_to_signupWithEmailFragment)
        }
        binding.layoutSignupWithPhone.SignupWithPhone.setOnClickListener {
            findNavController().navigate(R.id.action_authLoginTypeFragment_to_signUpWithPhoneFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFlow()
        val localeCountryISO = getCurrentLocale(requireContext())
        println("Got :: $localeCountryISO")
    }

    fun getCurrentLocale(context: Context): Locale? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    private fun initializeFlow() {
        binding.layoutLogin.btnContinue.text = getString(R.string.login)
//        btnContinue.setOnClickListener {
//            findNavController().navigate(R.id.action_authLoginTypeFragment_to_verificationFragment)
        //  }
        clickListener()
        setClickListeners()
        onLoginButtonClick()
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
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                false.changePhoneTVColor(1)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }
/*    private fun onLoginButtonClick() {
        if (validateFields()) {
            btnContinue.setOnClickListener {
                findNavController().navigate(R.id.action_authLoginTypeFragment_to_verificationFragment)
            }
//            btnContinue.isClickable = false
//            val loginRequest = LoginRequest(phoneNumber, password)
          //  callback?.onLoginButtonClick(loginRequest, layoutLogin, getString(R.string.login))
        }
    }*/

    private fun clickListener() {
        binding.layoutLogin.btnContinue.setOnClickListener {
            if (validateFields()) {
                val loginRequest = LoginRequest(null, password,countryCode,phoneNumber)
                callback?.onLoginButtonClick(loginRequest,   binding.layoutLogin.btnContinue, getString(R.string.login))

            }
        }
    }
    private fun onLoginButtonClick() {
//        if (validateFields()) {
//            btnContinue.isClickable = false
//
//        }
    }

    private fun forgetPassword(){
        binding.tvForgotPassword.setOnClickListener {
            phoneNumber = binding.etPhoneNumber.text.toString().trim()
            countryCode = /*mCcpLogin.selectedCountryCode*/""
            if (phoneNumber.isNotBlank() && Utility.isValidPhone(phoneNumber)) {
                val resendOtp = ResendOtpRequest(null, countryCode, phoneNumber)
                callback?.onResendOtpButtonClick(resendOtp, Constants.FROM_FORGOT_PASSWORD)
            } else if (!Utility.isValidPhone(phoneNumber)) {
                true.changePhoneTVColor(1)
                binding.etPhoneNumber.error = getString(R.string.error_invalid_phone_number)

            }
            else
            {
                true.changePhoneTVColor(2)
                binding.etPhoneNumber.error = getString(R.string.please_enter_your_phone_number)

            }


        }
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

    private fun validateFields(): Boolean {
        phoneNumber = binding.etPhoneNumber.text.toString().trim()
        password = binding.etPassword.text.toString()
        countryCode = /*mCcpLogin.selectedCountryCode*/""
        removeErrors()
        if (phoneNumber.isEmpty()) {
            true.changePhoneTVColor(1)
            return false
        } else if (!Utility.isValidPhone(phoneNumber)) {
            true.changePhoneTVColor(2)
            return false
        } else if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_empty)
            binding.ivShowHide.isVisible = false
            return false
        } else if (!Utility.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.error_password_invalid)
            binding.ivShowHide.isVisible = false
            return false
        }
        return true

    }

    private fun removeErrors() {
        false.changePhoneTVColor(1)
        binding.tilPassword.isErrorEnabled = false
    }

    private fun onClickListener(): View? {
        /*btnContinue.setOnClickListener(this)*/
        return view


    }
/*     fun onSendOtp() {
        Navigation.findNavController(fragment_container)
            .navigate(R.id.action_authLoginTypeFragment_to_verificationFragment)
    }*/

    /*override fun onClick(view: View?) {
        if (view != null) {
            onSendOtp()

        }
         }*/

    private fun Boolean.changePhoneTVColor(flag: Int) {
        if (this) {
            binding.tilPhoneNumber.setTextColor(resources.getColor(R.color.color_error))
            binding.llPhoneNumber.background = resources.getDrawable(R.drawable.bg_red_outline_textview)
            binding.tvErrorMsg.isVisible = true
            if (flag == 1) {
                binding.tvErrorMsg.text = getString(R.string.please_enter_your_phone_number)
            } else {
                binding.tvErrorMsg.text = getString(R.string.error_invalid_phone_number)
            }

        } else {
            binding.tilPhoneNumber.setTextColor(resources.getColor(R.color.text_color_pix))
            binding. tvErrorMsg.isVisible = false
            binding.llPhoneNumber.background = resources.getDrawable(R.drawable.bg_outline_textview)

        }

    }

    override fun onViewClick(view: View) {
        when (view) {
            binding. tvForgotPassword -> callback?.onForgotPasswordClick()
            binding.layoutLogin.btnContinue -> clickListener()
        }
    }

   /* override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }*/

}



