package com.familypedia.view.dashboard.friends

import android.R.attr.label
import android.R.attr.phoneNumber
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.familypedia.R
import com.familypedia.databinding.ActivityInviteYourFriendsBinding
import com.familypedia.utils.*
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks



class InviteYourFriendsActivity : AppCompatActivity(), FamilyPediaClickListener {

    private var mInvitationUrl = ""
    private lateinit var binding: ActivityInviteYourFriendsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInviteYourFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        setListeners()
        setViewData()
        createInvitationLink()
    }

    private fun setViewData() {
        binding?.toolbarInviteFriends?.tvToolbarTitle?.text = getString(R.string.invite_your_friends_)
    }

    private fun setListeners() {
        binding?.toolbarInviteFriends?.ivBack?.familyPediaClickListener(this)
        binding?.llViaWhatsApp?.familyPediaClickListener(this)
        binding?.llMessage?.familyPediaClickListener(this)
        binding?.llMore?.familyPediaClickListener(this)
        binding?.llCopyLink?.familyPediaClickListener(this)
        binding?.viewInvitedFriends?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.toolbarInviteFriends?.ivBack -> onBackPressed()
            binding?.llViaWhatsApp -> shareViaWhatsapp()
            binding?.llMessage -> shareViaMessage()
            binding?.llMore -> more()
            binding?.llCopyLink -> copyLink()
            binding?.viewInvitedFriends -> YourInvitedFriendsActivity.open(this)
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

    private fun shareViaMessage() {
        if (mInvitationUrl.isNotEmpty()) {
            try {
                val defaultSMSPackage = Telephony.Sms.getDefaultSmsPackage(this)
                if (defaultSMSPackage != null) {
                    val sendIntent = Intent(Intent.ACTION_VIEW)
                    sendIntent.setPackage(defaultSMSPackage)
                    sendIntent.putExtra("sms_body", mInvitationUrl)
                    sendIntent.type = "vnd.android-dir/mms-sms"
                    startActivity(sendIntent)
                }else{
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                    intent.putExtra("sms_body", mInvitationUrl)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                    intent.putExtra("sms_body", mInvitationUrl)
                    startActivity(intent)
                    Log.d("TAG", "shareViaMessage: ${e.localizedMessage}")
                } catch (e: Exception) {
                    toast(getString(R.string.sms_not_support))
                }
            }
        } else {
            toast(getString(R.string.wait_for_link))
        }
    }


    private fun more() {
        if (mInvitationUrl.isNotEmpty()) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, mInvitationUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        } else {
            toast(getString(R.string.wait_for_link))
        }
    }

    private fun copyLink() {
        if (mInvitationUrl.isNotEmpty()) {
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label.toString(), mInvitationUrl)
            clipboard.setPrimaryClip(clip)
            toast(getString(R.string.link_copied))
        } else {
            toast(getString(R.string.wait_for_link))
        }
    }


    private fun createInvitationLink() {
        val userId = Utility.getPreferencesString(this, Constants.USER_ID)
        val link = "https://familypedia.page.link/send-invitation-link/?id=$userId"
        Log.d("::==>",""+ link)
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
                mInvitationUrl =
                    getString(R.string.invitation_text) + shortDynamicLink.shortLink.toString()
            }.addOnFailureListener { exception ->
                toast(getString(R.string.failed_to_generate_link))
            }
    }

    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, InviteYourFriendsActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
}