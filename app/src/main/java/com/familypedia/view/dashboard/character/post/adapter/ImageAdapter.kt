package com.familypedia.view.dashboard.character.post.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.familypedia.R
import com.familypedia.databinding.ItemUploadAttachmentBinding
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.listeners.ItemClickListener
import com.familypedia.utils.showView

class ImageAdapter(
    private val context: Context,
    private var list: ArrayList<String>?,
    private val onItemClick: ItemClickListener
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemUploadAttachmentBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list?.get(position) ?: "")
    }

    inner class ViewHolder(private val binding: ItemUploadAttachmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: String) {
            if (image.startsWith("profile_pictures")) {
                Glide.with(context).load(IMAGE_URL + image).into(binding.ivPic)
                binding.ivCancel.showView()
            } else {
                binding.ivCancel.showView()
                Glide.with(context).load(image).into(binding.ivPic)
            }

            binding.ivCancel.setOnClickListener {
                onItemClick.onItemClick(image, adapterPosition)
            }
        }
    }

    fun setItemList(list: ArrayList<String>?) {
        this.list = list
        notifyDataSetChanged()
    }
}
