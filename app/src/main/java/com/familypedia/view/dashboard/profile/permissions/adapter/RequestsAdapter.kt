package com.familypedia.view.dashboard.profile.permissions.adapter

import android.content.Context
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.familypedia.R
import com.familypedia.databinding.ItemRequestBinding
import com.familypedia.databinding.ItemCharacterBinding
import com.familypedia.network.AskCharacterPermissionRequest
import com.familypedia.network.User
import com.familypedia.utils.Constants.FROM_FRIEND_LIST
import com.familypedia.utils.Constants.FROM_FRIEND_REQUEST
import com.familypedia.utils.Constants.FROM_POST_REQUEST
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.hideView
import com.familypedia.utils.listeners.PermissionsListener
import com.familypedia.utils.loadImagesWithGlide
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.utils.setTextOnTextView

class RequestsAdapter(
    private val context: Context,
    private val dataList: List<User>,
    private val characterId: String,
    private val from: String,
    private val permissionsListener: PermissionsListener
) : RecyclerView.Adapter<RequestsAdapter.ViewHolder>() {

    private var mLastClickTime: Long = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = when (from) {
            FROM_POST_REQUEST, FROM_FRIEND_REQUEST, FROM_FRIEND_LIST ->
                ItemRequestBinding.inflate(LayoutInflater.from(context), parent, false)
            else ->
                ItemCharacterBinding.inflate(LayoutInflater.from(context), parent, false)
        }
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    inner class ViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            val requestDetail = AskCharacterPermissionRequest(user._id ?: "", characterId)

            // For ItemRequestBinding
            if (binding is ItemRequestBinding) {
                binding.ivCharacterImage.loadImagesWithGlide(IMAGE_URL + user.profile_pic, true)
                binding.tvCharacterName.setTextOnTextView(user.name, "")

                if (from == FROM_POST_REQUEST) {
                    binding.itemRequest.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }

                if (from == FROM_FRIEND_LIST) {
                    binding.btnAccept.hideView()
                    binding.btnReject.hideView()
                }

                binding.itemRequest.setSafeOnClickListener {
                    permissionsListener.onItemClick(user)
                }
                binding.btnAccept.setSafeOnClickListener {
                    permissionsListener.onRequestAccept(requestDetail)
                }
                binding.btnReject.setSafeOnClickListener {
                    permissionsListener.onRequestReject(requestDetail)
                }
            }

            // For ItemCharacterBinding
            if (binding is ItemCharacterBinding) {
                binding.ivEditCharacter.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_delete))
                binding.itemCharacter.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                binding.ivEditCharacter.setSafeOnClickListener {
                    avoidDoubleClick()
                    permissionsListener.onDelete(requestDetail)
                }
                binding.itemCharacter.setSafeOnClickListener {
                    avoidDoubleClick()
                    permissionsListener.onItemClick(user)
                }
            }
        }
    }

    private fun avoidDoubleClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 3000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }

    override fun getItemViewType(position: Int): Int = position
}
