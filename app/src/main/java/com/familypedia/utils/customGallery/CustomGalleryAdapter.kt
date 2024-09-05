package com.familypedia.utils.customGallery

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.familypedia.R
import com.familypedia.databinding.GalleryItemBinding
import com.familypedia.utils.showStringSnackbar




class CustomGalleryAdapter (private var context: Activity, private var llParent: LinearLayout, private var pickerController: PickerController, private var saveDir:String,private var maxSelection:Int) : RecyclerView.Adapter<CustomGalleryAdapter.ViewHolder>() {
    private var mInflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var imageCountCallBack:ImageCountCallBack
    private var imageList = ArrayList<ImageModal>()
    // private var selectedList = ArrayList<String>()
    private var imageCount = 0
    private var totalSize = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val  view = mInflater.inflate(R.layout.gallery_item, parent, false)
        val params = view.layoutParams
        params.width = llParent.width / 3
        // params.height = llParent.height / 5
        params.height = params.width
        view.layoutParams = params
        imageCountCallBack=context as ImageCountCallBack
        val mBinding: GalleryItemBinding = DataBindingUtil.bind(view)!!
       // return MyViewHolder(mBinding)
        return ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modal = imageList[position]

        if (modal.isSelected) {
            holder.mBinding.llFrame.background = ContextCompat.getDrawable(context, R.drawable.gallery_border_blue)
            holder.mBinding.llSelection.visibility = View.VISIBLE
            holder.mBinding.tvSelectCount.text=modal.countSelect.toString()
            } else {
            holder.mBinding.llFrame.background = ContextCompat.getDrawable(context, R.drawable.transparent_rect_shape)
            holder.mBinding.llSelection.visibility = View.GONE
            }
            if(modal.type){
                holder.mBinding.flCamera.visibility= View.VISIBLE
                holder.mBinding.flItem.visibility= View.GONE
            }else{
                holder.mBinding.flCamera.visibility= View.GONE
                holder.mBinding.flItem.visibility= View.VISIBLE
            }
            if(!TextUtils.isEmpty(modal.imageUrl)) {
                Glide.with(context).load(Uri.parse(modal.imageUrl))
                    .centerCrop()
                    .placeholder(R.mipmap.gallery)
                    .error(R.mipmap.gallery)
                    .override(750,750)
                    .into(holder.mBinding.ivImage)

            }
        holder.mBinding.flItem.setOnClickListener {
               if ((imageCount+totalSize) < maxSelection) {
                    holder.mBinding.llFrame.background = ContextCompat.getDrawable(context, R.drawable.gallery_border_blue)
                    holder.mBinding.llSelection.visibility = View.VISIBLE
                    if (modal.isSelected) {
                        val tempCount =  imageList[position].countSelect
                        imageList[position].countSelect=0
                        refreshData(tempCount)
                        imageCount -= 1
                    } else {
                        imageCount += 1
                    }
                    modal.countSelect=imageCount
                    modal.isSelected=!modal.isSelected
                    notifyDataSetChanged()
                }
               else {
                    if (modal.isSelected) {
                        val tempCount =  imageList[position].countSelect
                        modal.isSelected=!modal.isSelected
                        imageCount -= 1
                        modal.countSelect=imageCount
                        refreshData(tempCount)
                    }else {
                        val text: String = java.lang.String.format(
                            context.getString(R.string.select_5_images),
                            maxSelection
                        )

                        holder.mBinding.flCamera.showStringSnackbar(text)
                    }
                }
                imageCountCallBack.imageCount(imageCount)
            }
        holder.mBinding.flCamera.setOnClickListener {
                pickerController.takePicture(context, saveDir)
            }
    }
    inner class ViewHolder(mBinding: GalleryItemBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        val mBinding: GalleryItemBinding = mBinding
    }

   private fun refreshData(tempCount: Int) {
        for (j in imageList.indices){
            if(imageList[j].isSelected&&imageList[j].countSelect>=tempCount){
                imageList[j].countSelect-=1
            }
        }

        notifyDataSetChanged()
        imageCountCallBack.imageCount(imageCount)
    }


    fun setData(result: ArrayList<ImageModal>) {
        imageList = result
    }
    interface ImageCountCallBack{
        fun imageCount(count:Int)
    }

    fun updateAdapter(unselectedList: java.util.ArrayList<Int>) {
        for (i in unselectedList.indices){
            imageCount -= 1
            val tempCount =  imageList[unselectedList[i]].countSelect
            imageList[unselectedList[i]].countSelect=0
            refreshData(tempCount)
            imageList[unselectedList[i]].countSelect=imageCount
            imageList[unselectedList[i]].isSelected=!imageList[unselectedList[i]].isSelected
        }
        notifyDataSetChanged()
    }

    fun updateImageCount(selectCount: Int, listLise: Int) {
        imageCount=selectCount
        totalSize=listLise
    }
}