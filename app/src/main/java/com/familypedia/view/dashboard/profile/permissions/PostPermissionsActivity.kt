package com.familypedia.view.dashboard.profile.permissions

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.familypedia.R
import com.familypedia.databinding.ActivityPostPermissionsBinding
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.NOTIFICATION
import com.familypedia.utils.FamilyPediaClickListener
import com.familypedia.utils.Utility
import com.familypedia.utils.familyPediaClickListener
import com.familypedia.view.dashboard.character.YourCharactersFragment
import com.familypedia.viewmodels.PermissionsViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PostPermissionsActivity : AppCompatActivity(), FamilyPediaClickListener {
    //private val titles = arrayOf("Post Request", "Given Request")
    private val titles = arrayListOf<String>()

    private var permissionsViewModel: PermissionsViewModel? = null
    private var from = ""
    private lateinit var binding: ActivityPostPermissionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        titles.add(getString(R.string.post_request))
        titles.add(getString(R.string.given_request))

        setupViews()
        initViewPager()
        setupListeners()
        initViewModel()
        responseModel()

    }

    private fun setupViews() {
        from = intent.extras?.getString(FROM) ?: ""
        if (from == NOTIFICATION)
            binding.toolbarPostPermissions.tvToolbarTitle.text = getString(R.string.notification_detail)
        else
            binding.toolbarPostPermissions.tvToolbarTitle.text = getString(R.string.permissions)
    }

    private fun setupListeners() {
        binding.toolbarPostPermissions.ivBack.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.toolbarPostPermissions.ivBack -> onBackPressed()
        }
    }


    private fun initViewPager() {
        binding.viewPager.adapter = ViewPagerFragmentAdapter(this)
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()
    }

    private class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return PostRequestFragment()
                1 -> return GivenRequestFragment()
            }
            return YourCharactersFragment()
        }

        override fun getItemCount(): Int {
            return 2
        }
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/
    private fun initViewModel() {
        permissionsViewModel = ViewModelProvider(this).get(PermissionsViewModel::class.java)
        permissionsViewModel?.init()
        //getPermissions()
    }

    private fun getPermissions() {
        permissionsViewModel?.getPermissionsList(this, 1)
    }

    private fun responseModel() {
        permissionsViewModel?.permisssionResponseResult?.observe(this, { response ->

        })
    }

    companion object {
        fun open(currActivity: Activity, bundle: Bundle?) {
            val intent = Intent(currActivity, PostPermissionsActivity::class.java)
            bundle?.let {
                intent.putExtras(bundle)
            }
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }
}