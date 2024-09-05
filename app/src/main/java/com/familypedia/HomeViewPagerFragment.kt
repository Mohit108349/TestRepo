package com.familypedia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity
import com.familypedia.view.dashboard.character.aboutCharacters.CharacterTimelineFragment
import com.familypedia.view.dashboard.character.aboutCharacters.PermittedToPost
import com.familypedia.view.dashboard.home.HomeFragment
import com.familypedia.viewmodels.CharacterViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
/*import kotlinx.android.synthetic.main.activity_about_character.*
import kotlinx.android.synthetic.main.fragment_home_view_pager.*
import kotlinx.android.synthetic.main.fragment_home_view_pager.tab_layout
import kotlinx.android.synthetic.main.fragment_home_view_pager.view_pager*/

class HomeViewPagerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      //  initViewPager()
    }

    /************************SETUP VIEW-PAGER TABS*************************************/
  /*  private fun initViewPager() {
        view_pager.adapter =
            ViewPagerFragmentAdapter(
                requireActivity()
            )
        TabLayoutMediator(
            tab_layout, view_pager
        ) { tab: TabLayout.Tab, position: Int ->
            //tab.text = titles[position]
        }.attach()
    }*/


    private class ViewPagerFragmentAdapter(
        fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            /*when (position) {
                0 -> return AboutCharacterFragment(bundleData, characterViewModel, permittedToPost)
                1 -> return CharacterTimelineFragment(characterId)
            }*/
            //return AboutCharacterFragment(bundleData, characterViewModel, permittedToPost)
            return HomeFragment()
        }

        override fun getItemCount(): Int {
            return 1
        }
    }

}