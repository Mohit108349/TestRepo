package com.familypedia.view.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.familypedia.R
import com.familypedia.databinding.FragmentAuthLoginTypeBinding
import com.familypedia.view.auth.phone_login.LoginWithPhoneFragment
import com.familypedia.view.dashboard.character.CharactersWithPermissionFragment
import com.familypedia.view.dashboard.character.YourCharactersFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AuthLoginTypeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private val titles = arrayListOf<String>()
    private var _binding: FragmentAuthLoginTypeBinding? = null
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
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAuthLoginTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFlow()
    }
    private fun initializeFlow() {
        titles.add(getString(R.string.email))
        titles.add(getString(R.string.phone_number))
        initViewPager()
    }

    private fun initViewPager() {
        binding.viewPager.adapter = ViewPagerFragmentAdapter(requireActivity())
        TabLayoutMediator(binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()
    }

    class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return LoginWithEmailFragment()
                1 -> return LoginWithPhoneFragment()
            }
            return LoginWithEmailFragment()
        }

        override fun getItemCount(): Int {
            return 2
        }
    }


}