package com.familypedia.view.dashboard.character.post.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.databinding.ItemCropImageBinding
import com.familypedia.utils.setSafeOnClickListener

class CropImageAdapter(private val imageUrlList: List<Uri>, val listen: (Uri) -> Unit) :
    RecyclerView.Adapter<CropImageAdapter.ViewPagerViewHolder>() {
    inner class ViewPagerViewHolder(private val binding: ItemCropImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(imageUrl: Uri) {
            binding.ivImage.setSafeOnClickListener {
                //DialogPhotoPreview(binding.root.context, imageUrl).show()
                listen(imageUrl)
            }
            binding.ivImage.setImageURI(imageUrl)
        }
    }

    override fun getItemCount(): Int = imageUrlList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val binding = ItemCropImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.setData(imageUrlList[position])
    }
}