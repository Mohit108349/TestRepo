package com.familypedia.view.dashboard.character

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.familypedia.R
import com.familypedia.databinding.FragmentCharactersBinding
import com.familypedia.utils.FamilyPediaClickListener
import com.familypedia.utils.familyPediaClickListener
import com.familypedia.view.dashboard.character.addCharacter.AddCharacterListingActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CharactersFragment : Fragment() ,FamilyPediaClickListener{
    private var param1: String? = null
    private var param2: String? = null
    private val titles = arrayListOf<String>()
//    private val titles = arrayOf(YOUR_BIOGRAPHIES, BIOGRAPHIES_WITH_PERMISSION)
private var _binding: FragmentCharactersBinding? = null
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
        _binding = FragmentCharactersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeControl()
    }
    private fun initializeControl(){
        titles.add(getString(R.string.your_biographies))
        titles.add(getString(R.string.biographies_with_permission))
        initViewPager()
        setListeners()
    }
    private fun setListeners(){
        binding.btnAddNewCharacter.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when(view){
            binding.btnAddNewCharacter->AddCharacterListingActivity.open(requireActivity())
        }
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
                0 -> return YourCharactersFragment()
                1 -> return CharactersWithPermissionFragment()
            }
            return YourCharactersFragment()
        }

        override fun getItemCount(): Int {
            return 2
        }
    }
}