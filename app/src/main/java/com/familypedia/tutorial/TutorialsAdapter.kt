package com.familypedia.tutorial


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.familypedia.R


class TutorialsAdapter(private var context: Context, private var onboardingDataList: List<TutorialsData>):
    PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view==`object`
    }
    override fun getCount(): Int {
        return onboardingDataList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object`as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view= LayoutInflater.from(context).inflate(R.layout.tutorial_adapter_item,null)

        val imageView: ImageView = view.findViewById(R.id.walk_img)
        Glide.with(context)
            .load(onboardingDataList[position].image)
            .into(imageView)
        container.addView(view)
        return view
    }
}