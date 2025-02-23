package com.familypedia.utils.imagepicker.adapter

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.EfImagepickerItemImageBinding
import com.familypedia.utils.imagepicker.features.imageloader.ImageLoader
import com.familypedia.utils.imagepicker.features.imageloader.ImageType
import com.familypedia.utils.imagepicker.helper.ImagePickerUtils
import com.familypedia.utils.imagepicker.helper.diff.SimpleDiffUtilCallBack
import com.familypedia.utils.imagepicker.listeners.OnImageClickListener
import com.familypedia.utils.imagepicker.listeners.OnImageSelectedListener
import com.familypedia.utils.imagepicker.model.Image

class ImagePickerAdapter(
    private val context: Context,
    private val imageLoader: ImageLoader,
    selectedImages: List<Image>,
    private val itemClickListener: OnImageClickListener
) : RecyclerView.Adapter<ImagePickerAdapter.ImageViewHolder>() {

    private val listDiffer by lazy {
        AsyncListDiffer<Image>(this, SimpleDiffUtilCallBack())
    }

    val selectedImages: MutableList<Image> = mutableListOf()

    private var imageSelectedListener: OnImageSelectedListener? = null
    private val videoDurationHolder = HashMap<Long, String?>()

    init {
        if (selectedImages.isNotEmpty()) {
            this.selectedImages.addAll(selectedImages)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = EfImagepickerItemImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ImageViewHolder, position: Int) {
        val image = getItem(position) ?: return

        val isSelected = isSelected(image)
        imageLoader.loadImage(image, viewHolder.binding.imageView, ImageType.GALLERY)

        var showFileTypeIndicator = false
        var fileTypeLabel: String? = ""

        if (ImagePickerUtils.isGifFormat(image)) {
            fileTypeLabel = context.resources.getString(R.string.ef_gif)
            showFileTypeIndicator = true
        }

        if (ImagePickerUtils.isVideoFormat(image)) {
            if (!videoDurationHolder.containsKey(image.id)) {
                val uri =
                    Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), "" + image.id)
                videoDurationHolder[image.id] = ImagePickerUtils.getVideoDurationLabel(
                    context, uri
                )
            }

            fileTypeLabel = videoDurationHolder[image.id]
            showFileTypeIndicator = true
        }

        viewHolder.binding.apply {
            efItemFileTypeIndicator.text = fileTypeLabel
            efItemFileTypeIndicator.visibility = if (showFileTypeIndicator) View.VISIBLE else View.GONE
            viewAlpha.alpha = if (isSelected) 0.5f else 0f
            root.setOnClickListener {
                val shouldSelect = itemClickListener(isSelected)

                if (isSelected) {
                    removeSelectedImage(image, position)
                } else if (shouldSelect) {
                    addSelected(image, position)
                }
            }
            root.foreground = if (isSelected) ContextCompat.getDrawable(
                context,
                R.drawable.ef_ic_done_white
            ) else null
        }
    }

    private fun isSelected(image: Image): Boolean {
        return selectedImages.any { it.path == image.path }
    }

    override fun getItemCount() = listDiffer.currentList.size

    fun setData(images: List<Image>) {
        listDiffer.submitList(images)
    }

    private fun addSelected(image: Image, position: Int) {
        mutateSelection {
            selectedImages.add(image)
            notifyItemChanged(position)
        }
    }

    private fun removeSelectedImage(image: Image, position: Int) {
        mutateSelection {
            selectedImages.remove(image)
            notifyItemChanged(position)
        }
    }

    fun removeAllSelectedSingleClick() {
        mutateSelection {
            selectedImages.clear()
            notifyDataSetChanged()
        }
    }

    private fun mutateSelection(runnable: Runnable) {
        runnable.run()
        imageSelectedListener?.invoke(selectedImages)
    }

    fun setImageSelectedListener(imageSelectedListener: OnImageSelectedListener?) {
        this.imageSelectedListener = imageSelectedListener
    }

    private fun getItem(position: Int) = listDiffer.currentList.getOrNull(position)

    class ImageViewHolder(val binding: EfImagepickerItemImageBinding) : RecyclerView.ViewHolder(binding.root)
}
