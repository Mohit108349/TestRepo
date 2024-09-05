package com.familypedia.view.dashboard.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.familypedia.R
import com.familypedia.databinding.FragmentFriendsBinding
import com.familypedia.utils.FamilyPediaClickListener
import com.familypedia.utils.familyPediaClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FriendsFragment : Fragment(), FamilyPediaClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private val titles = arrayListOf<String>()
    private var _binding: FragmentFriendsBinding? = null
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
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }

    private fun initializeControl() {
        titles.add(requireActivity().getString(R.string.your_friends))
        titles.add(requireActivity().getString(R.string.friend_request))

        listeners()
        initViewPager()
    }

    private fun listeners() {
        binding?.etSearch?.familyPediaClickListener(this)
        binding?.btnShare?.familyPediaClickListener(this)
    }


    override fun onViewClick(view: View) {
        when (view) {
            binding?.etSearch -> SearchFriendsActivity.open(requireActivity())
            binding?.btnShare -> InviteYourFriendsActivity.open(requireActivity())
        }
    }

    private fun initViewPager() {
        binding?.viewPager?.adapter = ViewPagerFragmentAdapter(requireActivity())
        TabLayoutMediator(
            binding?.tabLayout!!, binding?.viewPager!!
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()
    }

    private class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return YourFriendsFragment()
                1 -> return FriendRequestFragment()
            }
            return YourFriendsFragment()
        }

        override fun getItemCount(): Int {
            return 2
        }
    }
}