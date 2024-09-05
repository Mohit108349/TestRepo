package com.familypedia.view.dashboard.home.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.familypedia.R
import com.familypedia.databinding.ImageItemBinding
import com.familypedia.utils.Constants
import com.familypedia.utils.glide.GlideImageLoader
import com.familypedia.utils.setSafeOnClickListener
class ImageViewPagerAdapter(
    val fromHomePostAdapter: Boolean = false,
    private val imageUrlList: List<String>,
    val listen: (String, Int) -> Unit
) :
    RecyclerView.Adapter<ImageViewPagerAdapter.ViewPagerViewHolder>() {
    inner class ViewPagerViewHolder(private val binding: ImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(imageUrl: String, position: Int) {
            if (fromHomePostAdapter) binding.ivImage.disableListeners()
            binding.ivImage.setSafeOnClickListener {
                //DialogPhotoPreview(binding.root.context, imageUrl).show()
                listen(imageUrl, position)
            }
            val options: RequestOptions = RequestOptions()
                .placeholder(R.drawable.ic_item_placeholder_home)
                .error(R.drawable.ic_item_placeholder_home)
                .priority(Priority.HIGH)
            GlideImageLoader(
                binding.ivImage,
                binding.imageProgressBar,
            ).load(
                if (imageUrl.startsWith("http")) imageUrl else Constants.IMAGE_URL + imageUrl,
                options
            )
        }
    }

    override fun getItemCount(): Int = imageUrlList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val binding = ImageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.setData(imageUrlList[position], position)
    }
}
