package com.familypedia.view.dashboard.character.aboutCharacters.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.databinding.ItemPhotoBinding
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.DialogPhotoPreview
import com.familypedia.utils.loadPlaceholderImagesWithGlide
import com.familypedia.utils.setSafeOnClickListener

class PhotosAdapter(
    private val context: Context,
    private val photosList: List<String>
) : RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        holder.bind(photosList[position])
    }

    override fun getItemCount(): Int {
        return photosList.size
    }

    inner class PhotosViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: String) {
            binding.ivPhoto.loadPlaceholderImagesWithGlide(
                IMAGE_URL + photo
            )

            binding.ivPhoto.setSafeOnClickListener {
                DialogPhotoPreview(context, photo, photosList).show()
            }
        }
    }
}
