package com.familypedia.walkthrough

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.familypedia.R
import com.familypedia.utils.hideView
import com.familypedia.utils.showView

class WalkThroughAdapter(private var context: Context, private var onboardingDataList: List<WalkThroughData>):
    PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return onboardingDataList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.walkthrought_adapter_item, null)

        val imageView: ImageView = view.findViewById(R.id.walk_img)
        val title: TextView = view.findViewById(R.id.tvheading1)
        val desc: TextView = view.findViewById(R.id.tvheading2)
        val ll1: LinearLayout = view.findViewById(R.id.llItem)

        val imageView2: ImageView = view.findViewById(R.id.walk_img2)
        val title2: TextView = view.findViewById(R.id.tvheading12)
        val desc2: TextView = view.findViewById(R.id.tvheading22)
        val ll2: LinearLayout = view.findViewById(R.id.llItem2)

        if (position == 1){
            imageView2.setImageResource(onboardingDataList[position].image)
            title2.text= onboardingDataList[position].title1.toString()
            desc2.text= onboardingDataList[position].title2.toString()
            ll1.hideView()
            ll2.showView()
        }else{
            imageView.setImageResource(onboardingDataList[position].image)
            title.text= onboardingDataList[position].title1.toString()
            desc.text= onboardingDataList[position].title2.toString()
            ll2.hideView()
            ll1.showView()
        }


        container.addView(view)
        return view
    }

}

