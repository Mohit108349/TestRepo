package com.familypedia.tutorial

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.familypedia.R
import com.familypedia.databinding.ActivityTutorialsBinding
import com.familypedia.utils.setSafeOnClickListener
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator
import java.util.*

class TutorialActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    private val onBoardingData: MutableList<TutorialsData> = ArrayList()
    private var tutorialsAdapter: TutorialsAdapter? = null
    private var currentPage = 0
    private val NUM_PAGES = 7
    private val DELAY_MS: Long = 500
    private val PERIOD_MS: Long = 3000

    private lateinit var binding: ActivityTutorialsBinding

    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, TutorialActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupTabs()
        clickListener()
    }

    private fun setupTabs() {
        // Initialize onboarding data
        onBoardingData.add(TutorialsData(R.drawable.ic_image_one))
        onBoardingData.add(TutorialsData(R.drawable.ic_image_two))
        onBoardingData.add(TutorialsData(R.drawable.ic_image_three))
        onBoardingData.add(TutorialsData(R.drawable.ic_image_four))
        onBoardingData.add(TutorialsData(R.drawable.ic_image_five))
        onBoardingData.add(TutorialsData(R.drawable.ic_image_six))

        // Set up the adapter
        tutorialsAdapter = TutorialsAdapter(this, onBoardingData)
        binding.vpScreen.adapter = tutorialsAdapter

        // Set up the page change listener
        binding.vpScreen.addOnPageChangeListener(this)

        // Set up auto-scrolling of ViewPager
        val handler = Handler()
        val update = Runnable {
            if (currentPage == NUM_PAGES - 1) {
                currentPage = 0
            }
            binding.vpScreen.setCurrentItem(currentPage++, true)
        }
        // Uncomment to enable auto-scrolling
        // val timer = Timer()
        // timer.schedule(object : TimerTask() {
        //     override fun run() {
        //         handler.post(update)
        //     }
        // }, DELAY_MS, PERIOD_MS)

        // Set up the dots indicator
        binding.dotsIndicator.attachTo(binding.vpScreen)
    }

    private fun clickListener() {
        binding.ivBack.setSafeOnClickListener {
            finish()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        currentPage = position
    }

    override fun onPageScrollStateChanged(state: Int) {}
}
