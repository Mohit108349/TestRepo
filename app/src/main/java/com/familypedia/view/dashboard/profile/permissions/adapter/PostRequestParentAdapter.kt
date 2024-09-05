package com.familypedia.view.dashboard.profile.permissions.adapter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.network.PermissionData
import com.familypedia.utils.Constants.FROM_GIVEN_REQUEST
import com.familypedia.utils.Constants.FROM_POST_REQUEST
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.listeners.ClickPosition
import com.familypedia.utils.listeners.PermissionsListener
import com.familypedia.utils.loadImagesWithGlide
import com.familypedia.utils.setTextOnTextView
import com.familypedia.view.dashboard.profile.permissions.PostRequestFragment
import kotlinx.android.extensions.LayoutContainer

import android.widget.Toast
import com.familypedia.databinding.ItemPostRequestBinding
import com.familypedia.utils.Constants
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity

class PostRequestParentAdapter(
    var context: Context,
    var permissionList: List<PermissionData>,
    val from: String,
    val permissionsListener: PermissionsListener
) :
    RecyclerView.Adapter<PostRequestParentAdapter.ViewHolder>() {
    var clickPosition: ClickPosition? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostRequestParentAdapter.ViewHolder {
        val binding = ItemPostRequestBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return permissionList.size
    }

    override fun onBindViewHolder(holder: PostRequestParentAdapter.ViewHolder, position: Int) {
        holder.bind(permissionList[position])
    }

    inner class ViewHolder(private val binding: ItemPostRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: PermissionData) {
            binding.itemPostRequest.setSafeOnClickListener {
                // Handle click
            }

            binding.llCharacter.setSafeOnClickListener {
                val bundle = Bundle().apply {
                    putString(Constants.USER_NAME, data.name)
                    putString(Constants.USER_ID, data._id)
                    putString(Constants.PROFILE_PIC, data.profile_pic)
                }
                AboutCharacterActivity.open(context as Activity, bundle)
            }

            binding.ivCharacterImage.loadImagesWithGlide(IMAGE_URL + data.profile_pic, true)
            binding.tvCharacterName.setTextOnTextView(data.name, "")

            val requestAdapter = if (from == FROM_GIVEN_REQUEST) {
                data.permittedUser?.let {
                    RequestsAdapter(context, it, data._id ?: "", from, permissionsListener)
                }
            } else {
                data.characterEditDeleterequestUser?.let {
                    RequestsAdapter(context, it, data._id ?: "", from, permissionsListener)
                }
            }
            binding.rvRequest.adapter = requestAdapter
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}