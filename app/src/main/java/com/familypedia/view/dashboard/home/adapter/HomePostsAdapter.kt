package com.familypedia.view.dashboard.home.adapter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.familypedia.R
import com.familypedia.databinding.ItemHomePostsBinding
import com.familypedia.databinding.LayoutViewMoreBinding
import com.familypedia.network.PostData
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_HOME
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.Constants.MULTIPLE_DAY_EVENT
import com.familypedia.utils.listeners.PostsListener
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity
import com.familypedia.view.dashboard.profile.ViewProfileActivity
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle

class HomePostsAdapter(
    private val context: Context,
    private val listener: PostsListener,
    private val dataList: List<PostData>,
    private val from: String,
    private val rvParent: RecyclerView? = null
) : RecyclerView.Adapter<HomePostsAdapter.ViewHolder>() {

    private var tempDataList: List<PostData>? = null
    private var searchText: String? = ""
    private var desciption = ""
    private var characterName = ""
    private var startingDate = ""
    private var endingDate = ""
    private var location = ""
    private var userId = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomePostsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postData = dataList[position]
        holder.bind(postData)
    }

    inner class ViewHolder(private val binding: ItemHomePostsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val readMoreOption = ReadMoreOption.Builder(context)
            .textLength(140)
            .moreLabel(context.getString(R.string.read_more))
            .lessLabel(" " + context.getString(R.string.read_less))
            .moreLabelColor(ContextCompat.getColor(context, R.color.color_blue))
            .lessLabelColor(ContextCompat.getColor(context, R.color.color_blue))
            .labelUnderLine(true)
            .expandAnimation(false)
            .build()

        fun bind(postData: PostData) {
            userId = Utility.getPreferencesString(context, Constants.USER_ID)

            if (from == FROM_HOME) {
                binding.ivThreedot2.hideView()
                binding.ivThreedot.showView()
                binding.llProfile.showView()
            } else {
                binding.llProfile.hideView()
                binding.ivThreedot2.showView()
            }

            binding.tvPostBy.setTextOnTextView(postData.postedBy?.name, "")
            binding.tvPostBy.setSafeOnClickListener {
                val bundle = Bundle().apply {
                    putString(FROM, Constants.FROM_VIEW_USER)
                    putString(Constants.USER_ID, postData.postedBy?._id)
                }
                ViewProfileActivity.open(context as Activity, bundle)
            }

            val images = arrayListOf<String>().apply {
                postData.attachPhotos?.forEach { add(IMAGE_URL + it) }
            }

            if (postData.attachPhotos != null && postData.attachPhotos?.let { it.isNotEmpty() } == true) {
                binding.viewPagerScrollHost.showView()
                binding.indicatorView.showView()
                binding.postsViewPager.adapter = ImageViewPagerAdapter(true, images) { img, currPosition ->
                    DialogPhotoPreview(context, img, images, currPosition).show()
                }
                binding.postsViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                binding.postsViewPager.currentItem = 0

                binding.indicatorView.apply {
                    setSliderColor(
                        ContextCompat.getColor(context, R.color.color_grey),
                        ContextCompat.getColor(context, R.color.color_blue_light)
                    )
                    setSliderWidth(resources.getDimension(R.dimen.dp_10))
                    setSliderHeight(resources.getDimension(R.dimen.dp_5))
                    setSlideMode(IndicatorSlideMode.WORM)
                    setIndicatorStyle(IndicatorStyle.CIRCLE)
                    setupWithViewPager(binding.postsViewPager)
                }
            } else {
                binding.rvPhotos2.hideView()
                binding.viewPagerScrollHost.hideView()
                binding.indicatorView.hideView()
            }

            binding.ivEditCharacter.setSafeOnClickListener {
                listener.onEditClick(postData)
            }
            binding.ivThreedot.setSafeOnClickListener {
                Utility.showPopup(binding.ivThreedot, context, listener, postData, userId)
            }
            binding.ivThreedot2.setSafeOnClickListener {
                Utility.showPopup(binding.ivThreedot2, context, listener, postData, userId)
            }
            binding.ivDeleteCharacter?.setSafeOnClickListener {
                listener.onDeleteClick(postData)
            }
            binding.ivEditCharacter2.setSafeOnClickListener {
                listener.onEditClick(postData)
            }
            binding.ivDeleteCharacter2?.setSafeOnClickListener {
                listener.onDeleteClick(postData)
            }
            binding.llProfile.setSafeOnClickListener {
                val bundle = Bundle().apply {
                    putString(Constants.USER_NAME, postData.character?.name)
                    putString(Constants.USER_ID, postData.character?._id)
                    putString(Constants.PROFILE_PIC, postData.character?.profile_pic)
                    putSerializable(Constants.PERMITTED_USERS, postData.character)
                }
                AboutCharacterActivity.open(context as Activity, bundle)
            }

            binding.ivCharacterImage.loadImagesWithGlide(
                IMAGE_URL + postData.character?.profile_pic,
                false
            )
            if (postData.eventType != MULTIPLE_DAY_EVENT) {
                binding.tvTo.hideView()
                binding.tvToDate.hideView()
                binding.tvFrom.text = ""
            } else {
                binding.tvTo.showView()
                binding.tvToDate.showView()
                binding.tvFrom.text = context.getString(R.string.from)
            }

            desciption = postData.description ?: ""
            characterName = postData.character?.name ?: ""
            startingDate = postData.startingDate ?: ""
            endingDate = postData.endingDate ?: ""
            location = postData.location ?: ""

            val space = if (postData.eventType != MULTIPLE_DAY_EVENT) "" else "  "

            binding.tvCharacterName.setTextOnTextView(characterName, "")
            binding.tvFromDate.setTextOnTextView(space + startingDate, "")
            binding.tvToDate.setTextOnTextView(endingDate, "")
            binding.tvLocation.setTextOnTextView(location, "")

            readMoreOption.addReadMoreTo(binding.tvDescription, desciption)
        }
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getItemId(position: Int): Long = position.toLong()

    fun setFilter(dataList: List<PostData>, searchText: String) {
        tempDataList = ArrayList(dataList)
        this.searchText = searchText.toLowerCase()
        notifyDataSetChanged()
    }
}
