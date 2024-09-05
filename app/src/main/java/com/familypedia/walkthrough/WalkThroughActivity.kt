package com.familypedia.walkthrough

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.familypedia.R
import com.familypedia.databinding.ActivityWalkThroughBinding
import com.familypedia.utils.Constants
import com.familypedia.utils.Utility
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle


class WalkThroughActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, WalkThroughActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }
    private val onBoardingData: MutableList<WalkThroughData> = ArrayList()
    var position = 0
    var walkThroughAdapter : WalkThroughAdapter? = null
    var currentPage = 0
    private lateinit var binding: ActivityWalkThroughBinding
    val NUM_PAGES= 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityWalkThroughBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()
        setOnclickListeners()
    }

    private fun setupTabs() {

        onBoardingData.add(
            WalkThroughData(
                R.mipmap.image_one1,
                getString(R.string.order_important_photos_of_your_life_and_that_of_your_relatives),
                getString(R.string.avoid_losing_your_most_beautiful_memories)

            )
        )

        onBoardingData.add(
            WalkThroughData(
                R.mipmap.image_two2,
                getString(R.string.easily_create_biographies_of_whoever_you_want),
                getString(R.string.photos_and_memories_are_ordered_chronologically)
            )
        )

        onBoardingData.add(
            WalkThroughData(
                R.mipmap.image_three3,
                getString(R.string.ask_your_friends_to_complete_the_bios_you_want),
                getString(R.string.their_memories_add_to_yours)
            )
        )


        walkThroughAdapter = WalkThroughAdapter(this, onBoardingData)
        binding.vpScreen?.adapter = walkThroughAdapter

        binding.vpScreen?.addOnPageChangeListener(this)


        binding.btnNext.setOnClickListener {
            currentPage =  binding.vpScreen?.currentItem!!
            binding.vpScreen?.setCurrentItem(++currentPage, true)


        }
        binding.dotsIndicator.apply {
            val dotsIndicator = findViewById<SpringDotsIndicator>(R.id.dots_indicator)
            dotsIndicator.attachTo( binding.vpScreen)
        }
//        dots_indicator.apply {
//
//            setSliderColor(
//                Color.parseColor("#4D20BEC8"),
//                resources.getColor(R.color.color_blue_dark)
//            )
//            setSliderWidth(
//                resources.getDimension(R.dimen.dp_7),
//                resources.getDimension(R.dimen.dp_7)
//            )
//            setSliderHeight(resources.getDimension(R.dimen.dp_7))
//            setSlideMode(IndicatorSlideMode.NORMAL)
//            setSliderGap(resources.getDimension(R.dimen.dp_5))
//            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
//            setupWithViewPager(vpScreen)
//        }
    }






    private fun setOnclickListeners(){
        Utility.savePreferencesBoolean(this, Constants.IS_WALKTHROUGH_SEEN, true)

        binding.btnSkip?.setOnClickListener {
            startActivity(Intent(this,AuthViewsHolderActivity::class.java))
            finishAffinity()
        }

        binding.btnLetsStart?.setSafeOnClickListener {
            startActivity(Intent(this,AuthViewsHolderActivity::class.java))
            finishAffinity()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        if (position == 0||position == 1){
            binding.btnLetsStart.isVisible=false
            binding.btnNext.isVisible = true
            binding.btnSkip.isVisible =true

        }else if (position == 2){
            binding.btnLetsStart.isVisible=true
            binding.btnNext.isVisible =false
            binding.btnSkip.isVisible = false

        }

    }


    override fun onPageScrollStateChanged(state: Int) {

    }
}