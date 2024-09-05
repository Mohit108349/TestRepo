package com.familypedia.view.dashboard.friends.adapter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ItemInvitedFriendsBinding
import com.familypedia.network.InvitationData
import com.familypedia.utils.Constants
import com.familypedia.utils.Utility.getTimeAgo
import com.familypedia.utils.loadImagesWithGlide
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.utils.setTextOnTextView
import com.familypedia.view.dashboard.profile.ViewProfileActivity

class InvitedFriendsAdapter(
    private val context: Context,
    private val dataList: List<InvitationData>
) : RecyclerView.Adapter<InvitedFriendsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvitedFriendsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    inner class ViewHolder(private val binding: ItemInvitedFriendsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: InvitationData) {
            with(binding) {
                ivUserImage.loadImagesWithGlide(
                    Constants.IMAGE_URL + data.profile_pic,
                    true
                )
                tvUserName.setTextOnTextView(data.name, "")
                tvDate.setTextOnTextView(getTimeAgo(data.created), "")
                root.setSafeOnClickListener {
                    val bundle = Bundle().apply {
                        putString(Constants.FROM, Constants.FROM_VIEW_USER)
                        putString(Constants.USER_ID, data._id)
                    }
                    ViewProfileActivity.open(context as Activity, bundle)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
