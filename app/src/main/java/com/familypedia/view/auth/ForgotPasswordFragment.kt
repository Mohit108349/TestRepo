package com.familypedia.view.auth

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.familypedia.R
import com.familypedia.databinding.FragmentForgotPasswordBinding
import com.familypedia.utils.FamilyPediaClickListener
import com.familypedia.utils.Utility.isValidEmail
import com.familypedia.utils.familyPediaClickListener


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ForgotPasswordFragment : Fragment(), FamilyPediaClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var callback: OnClickCallback? = null
    private var email = ""
    private var _binding: FragmentForgotPasswordBinding? = null
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
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }

    private fun initializeControl() {
       // setViews()
        //setSpan()
       // setListeners()
    }

    private fun setViews() {
        binding.layoutForgot.btnContinue.text = getString(R.string.submit)
    }

    private fun setListeners() {
        binding.layoutForgot.btnContinue.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.layoutForgot.btnContinue -> {
                if (validateFields()) {
                    binding.layoutForgot.btnContinue.isClickable=false
                    //createDynamicLink(email, "")
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        email = binding.etEmail.text.toString().trim()
        binding.tilEmail.isErrorEnabled = false
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_empty)
            return false
        } else if (!isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.error_email_invalid)
            return false
        }
        return true
    }


    private fun setSpan() {
        val spannable = SpannableString(getString(R.string.password_reset_link_not_received_resend))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                //ds.color = context.resources.getColor(R.color.orangeButton)
            }

            override fun onClick(widget: View) {
   //             if (validateFields())
                   // createDynamicLink(email, "resend")
   //             widget.invalidate()
            }
        }
        spannable.setSpan(
            clickableSpan,
            34,
            40,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.color_blue_light)),
            34,
            40,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding. tvLinkNotReceived.text = spannable
        binding.tvLinkNotReceived.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun createDynamicLink(email: String, type: String) {
//        val link = "https://familypedia.page.link/reset-my-password/?email=$email"
//        FirebaseDynamicLinks.getInstance().createDynamicLink()
//            .setLink(Uri.parse(link))
//            .setDomainUriPrefix("https://familypedia.page.link")
//            .setAndroidParameters(
//                DynamicLink.AndroidParameters.Builder("com.familypedia").setMinimumVersion(125)
//                    .build()
//            )
//            .setIosParameters(
//                DynamicLink.IosParameters.Builder("com.family.pedia").setAppStoreId("123456789")
//                    .setMinimumVersion("1.0.1").build()
//            )
//            .buildShortDynamicLink()
//            .addOnSuccessListener { shortDynamicLink ->
//                val mInvitationUrl = shortDynamicLink.shortLink.toString()
//                if (type == "resend") {
//                 //   callback?.onResendButtonClick(ForgotPasswordRequest(mInvitationUrl, email))
//                } else {
//                    callback?.onSendPasswordResetButtonClick(
//                 //       ForgotPasswordRequest(mInvitationUrl, email),
//                        getString(R.string.send_password_reset_link)
//                    )
//                    //tvLinkNotReceived.showView()
//                }
//
//            }.addOnFailureListener { exception->
//                requireActivity().toast(getString(R.string.failed_to_generate_link))
//            }
    }
}