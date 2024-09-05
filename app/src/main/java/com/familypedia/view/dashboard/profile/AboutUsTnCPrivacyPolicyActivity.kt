package com.familypedia.view.dashboard.profile

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.familypedia.R
import com.familypedia.databinding.ActivityAboutUsTnCprivacyPolicyBinding
import com.familypedia.utils.*
import com.familypedia.utils.Constants.ABOUT_US
import com.familypedia.utils.Constants.FAQ
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.PRIVACY_POLICY
import com.familypedia.utils.Constants.TERMS_AND_CONDITIONS
import com.familypedia.utils.Constants.URL_ABOUT_US
import com.familypedia.utils.Constants.URL_FAQ
import com.familypedia.utils.Constants.URL_PRIVACY_POLICY
import com.familypedia.utils.Constants.URL_TERMS_CONDITION


class AboutUsTnCPrivacyPolicyActivity : AppCompatActivity(), FamilyPediaClickListener {
    private var from = ""
    private var toolbarTitle = ""
    private var url = ""
    private lateinit var binding: ActivityAboutUsTnCprivacyPolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsTnCprivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utility.setLocale(this)
        initializeControl()
    }

    private fun initializeControl() {
        from = intent.getStringExtra(FROM) ?: ""
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        when (from) {
            TERMS_AND_CONDITIONS -> {
                toolbarTitle = getString(R.string.terms_and_conditions)
                url = URL_TERMS_CONDITION
            }
            ABOUT_US -> {
                toolbarTitle = getString(R.string.about_us)
                url = URL_ABOUT_US
            }
            PRIVACY_POLICY -> {
                toolbarTitle = getString(R.string.privacy_policy)
                url = URL_PRIVACY_POLICY
            }
            FAQ -> {
                toolbarTitle = getString(R.string.faq)
                url = URL_FAQ
            }
            else -> ""
        }
        binding.toolbarAboutUs.tvToolbarTitle.text = toolbarTitle
        binding.webView.getSettings().setJavaScriptEnabled(true); // enable javascript
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.hideView()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                binding.progressBar.hideView()
                toast(getString(R.string.something_went_wrong))
            }
        }
        binding.webView?.loadUrl(url)
    }


    private fun setupListeners() {
        binding.toolbarAboutUs?.ivBack?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.toolbarAboutUs?.ivBack -> onBackPressed()
        }
    }

    companion object {
        fun open(currActivity: Activity, from: String) {
            val intent = Intent(currActivity, AboutUsTnCPrivacyPolicyActivity::class.java)
            intent.putExtra(FROM, from)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
}