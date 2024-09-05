package com.familypedia.view.auth.phone_login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.familypedia.R
import com.familypedia.databinding.FragmentVerificationBinding
import com.familypedia.network.AuthResponseModel
import com.familypedia.network.ResendOtpRequest
import com.familypedia.network.User
import com.familypedia.network.VerifyOTPRequest
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_EMAIL
import com.familypedia.utils.Constants.FROM_PHONE
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.auth.OnClickCallback
import com.familypedia.view.auth.ResetPasswordActivity
import com.familypedia.view.dashboard.profile.EditProfileActivity
import com.familypedia.viewmodels.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Array.getInt
import java.text.DecimalFormat
import java.text.NumberFormat

class VerificationFragment : Fragment(),SuccessFulListener {

    private var param1: String? = null
    private var param2: String? = null
    private var phoneNumber: String = ""
    private var countryCode: String =""
    private var email: String? = null
    private var otp:String?=""
    var valueToSet : String?=null
    var otpFromProfile : String?=null
    var from : String?=null
    private var comeFrom :Int? = null
    private var cc :String? = null
    private lateinit var changeTitle : TextView
    lateinit var changeDoc : TextView
    private var callback: OnClickCallback? = null
    private  var count: CountDownTimer? = null
    private var profileViewModel: ProfileViewModel? = null
    private var _binding: FragmentVerificationBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as OnClickCallback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeTitle =requireView().findViewById(R.id.tvVerificationTitle)
        changeDoc =requireView().findViewById(R.id.tvVerifyDesc)
        arguments?.let {
             comeFrom = it.getInt("comeFrom")
             valueToSet = it.getString("id","")
            otpFromProfile = it.getString(Constants.OTP,"")
            from = it.getString(Constants.FROM,"")

            if (comeFrom == FROM_PHONE){
                 cc = it.getString("cc","")
                val fullMobileNumber = cc+valueToSet
                changeTitle.text =getString(R.string.phone_number_verification)
                val textToSet =  getString(R.string.enter_four_digit_code_to_verify_your_phone_number_sent_to_).replace("[X]",fullMobileNumber)
                Utility.setBolderView(changeDoc,textToSet,fullMobileNumber)
            }
            else{
                changeTitle.text =getString(R.string.email_verification)
                val textToSet =  getString(R.string.enter_four_digit_code_to_verify_your_email_sent_to_).replace("[X]",valueToSet?:"")
                Utility.setBolderView(changeDoc,textToSet,valueToSet?:"")
            }
        }
        binding.Continue.btnContinue.text = getString(R.string.submit)
        showSuccessDialog()
        clickListener()
        startTimer()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,backcallback)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel?.init()
        verifyAuthTypeObserver()
    }

    private fun verifyAuthTypeObserver(){
        profileViewModel?.verifyAuthTypeResult?.observe(requireActivity()
        ) {
            val response = it
            if (response != null){
                otpFromProfile = response.data?.user?.otp
            }
        }
    }

    private fun showSuccessDialog() {

        binding.Continue.btnContinue.setOnClickListener() {
            PhoneNumberVerified(
                requireContext(), "",
                object : SuccessFulListener {
                    override fun onUpdateSuccessfully(from: String) {
                        count?.cancel()
                    }

                    override fun onDismiss(from: String) {

                    }
                },
            ).show()
        }
    }
    private val backcallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            activity?.finish()
        }
    }

    private fun startTimer() {
         count = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Used for formatting digit to be in 2 digits only
                val f: NumberFormat = DecimalFormat("00")
                val min = (millisUntilFinished / 60000) % 60
                val sec = (millisUntilFinished / 1000) % 60
                val time =  f.format(min) + ":" + f.format(sec)

                binding.ResendOtp?.text = "$time sec"
            }
            // When the task is over it will print 00:00:00 there
            override fun onFinish() {
                binding.tvResendHead?.hideView()
                binding.ResendOtp.text = getString(R.string.resend_code)
                binding.ResendOtp.setOnClickListener {
                    Toast.makeText(requireContext(), "Otp Will Send Again ", Toast.LENGTH_SHORT).show()
                    if (otpFromProfile?.isEmpty() == true && comeFrom== FROM_PHONE){
                        val resendOTPSignUP = ResendOtpRequest(null,cc?.removePrefix("+"),valueToSet)
                        callback?.onResendOtpButtonClick(resendOTPSignUP,Constants.FROM_RESEND_OTP)
                    }else if (otpFromProfile?.isEmpty() == true && comeFrom== FROM_EMAIL){
                        val resendOTPSignUP = ResendOtpRequest(valueToSet,null,null)
                        callback?.onResendOtpButtonClick(resendOTPSignUP,Constants.FROM_RESEND_OTP)
                    }else if (otpFromProfile?.isNotEmpty() == true && comeFrom== FROM_PHONE){
                        val resendOTPSignUP = ResendOtpRequest(null,cc?.removePrefix("+"),valueToSet)
                        profileViewModel?.verifyAUthType(resendOTPSignUP,requireActivity())

                    }else if(otpFromProfile?.isNotEmpty() == true && comeFrom== FROM_EMAIL){
                        val resendOTPSignUP = ResendOtpRequest(valueToSet,null,null)
                       // callback?.onResendOtpButtonClick(resendOTPSignUP,Constants.FROM_RESEND_OTP)
                        profileViewModel?.verifyAUthType(resendOTPSignUP,requireActivity())
                    }
                }
            }
        }.start()
    }

    private fun clickListener() {
        binding.Continue.btnContinue.setOnClickListener {
           // count?.cancel()
            otp = binding.otpView?.text.toString()
            if (comeFrom== FROM_PHONE) {
                val verifyOtpPhn = VerifyOTPRequest(phoneNumber = valueToSet,
                countryCode=cc?.removePrefix("+"), otp = otp, email = null)
                if (otpFromProfile?.isEmpty() == true) {
                    callback?.onVerifyButtonClick(verifyOtpPhn, FROM_PHONE)
                }else{
                    if (otp == otpFromProfile){
                        Toast.makeText(requireActivity(), "OTP IS VERIFIED", Toast.LENGTH_SHORT).show()
                        if (from != null && from== Constants.FROM_FORGOT_PASS){
                            accountVerifiedDialog(verifyOtpPhn)
                        }else{
                            val intent = Intent(requireActivity(),EditProfileActivity::class.java)
                            requireActivity().setResult(Activity.RESULT_OK,intent)
                            requireActivity().finish()
                        }

                    }else{
                        Toast.makeText(requireActivity(), "Invalid otp", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                val verifyOTPEmail =VerifyOTPRequest(phoneNumber = null,
                    countryCode=null, otp = otp, email = valueToSet)
                if (otpFromProfile?.isEmpty() == true) {
                    callback?.onVerifyButtonClick(verifyOTPEmail, FROM_EMAIL)
                }else{
                    if (otp == otpFromProfile){
                        Toast.makeText(requireActivity(), "OTP IS VERIFIED", Toast.LENGTH_SHORT).show()
                        if (from != null && from== Constants.FROM_FORGOT_PASS){
                            accountVerifiedDialog(verifyOTPEmail)
                        }else{
                            val intent = Intent(requireActivity(),EditProfileActivity::class.java)
                            requireActivity().setResult(Activity.RESULT_OK,intent)
                            requireActivity().finish()
                        }
                    }else{
                        Toast.makeText(requireActivity(), "Invalid otp", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun accountVerifiedDialog(verifyOTPRequest: VerifyOTPRequest) {
        var verifcationDialog : SuccessfulDialog? = null
        if (comeFrom == FROM_PHONE){

            verifcationDialog = SuccessfulDialog(
                requireActivity(), "",
                this,
                getString(R.string.phone_number_verified),
                getString(R.string.phone_number_has_been_verified)
            )
        }else{

            verifcationDialog = SuccessfulDialog(
                requireActivity(), "",
                this,
                getString(R.string.email_verification),
                "Email has been verified"
            )
        }

        verifcationDialog.show()
        lifecycleScope.launch {
            delay(3000)
            verifcationDialog.dismiss()
            navigateResetPassword(verifyOTPRequest)
        }
    }
    private fun navigateResetPassword(verifyOTPRequest: VerifyOTPRequest) {
        val bundle = Bundle()
        if (comeFrom == FROM_PHONE){
            bundle.putInt(Constants.FROM, comeFrom?:0)
            bundle.putString(Constants.OTP, verifyOTPRequest.otp)
            bundle.putString(Constants.COUNTRY_CODE, verifyOTPRequest.countryCode)
            bundle.putString(Constants.PHONE_NUMBER, verifyOTPRequest.phoneNumber)
        }else{
            bundle.putInt(Constants.FROM, comeFrom?:0)
            bundle.putString(Constants.OTP, verifyOTPRequest.otp)
            bundle.putString(Constants.EMAIL, verifyOTPRequest.email)
        }

        ResetPasswordActivity.open(requireActivity(), bundle)
        activity?.finishAffinity()
    }

    override fun onUpdateSuccessfully(from: String) {

    }

    override fun onDismiss(from: String) {

    }

    override fun onDestroy() {
        count?.cancel()
        super.onDestroy()
    }

}
