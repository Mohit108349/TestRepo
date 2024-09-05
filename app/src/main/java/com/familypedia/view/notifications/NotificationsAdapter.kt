package com.familypedia.view.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ItemNotificationBinding
import com.familypedia.network.NotificationData
import com.familypedia.utils.*
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_RECEIVED
import com.familypedia.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_RECEIVED
import com.familypedia.utils.Utility.getNotificationType
import com.familypedia.utils.listeners.NotificationListener

class NotificationsAdapter(
    var context: Context,
    val dataList: List<NotificationData>,
    val listener: NotificationListener,
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationsAdapter.ViewHolder {
        // Inflate the layout using View Binding
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: NotificationsAdapter.ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NotificationData) {
            data.read_status?.let { readStatus ->
                if (readStatus) {
                    binding.itemNotification.setBackgroundResource(R.drawable.shape_rounded_corner)
                } else {
                    binding.itemNotification.setBackgroundResource(R.drawable.shape_round_unread)
                }
            }

            val notificationType = data.notification_type
            if (notificationType == NOTIFICATION_TYPE_FRIEND_REQUEST_RECEIVED || notificationType == NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_RECEIVED) {
                binding.btnAccept.showView()
                binding.btnReject.showView()
            } else {
                binding.btnAccept.hideView()
                binding.btnReject.hideView()
            }

            binding.ivUserImage.loadImagesWithGlide(
                IMAGE_URL + data.sender_user?.profile_pic,
                true
            )

            binding.btnAccept.setSafeOnClickListener {
                listener.onRequestAccept(data)
            }
            binding.btnReject.setSafeOnClickListener {
                listener.onRequestReject(data)
            }
            binding.itemNotification.setSafeOnClickListener {
                listener.onItemClick(data)
            }

            when(data.notification_type) {
                "biosBirthday", "accessingTime", "uploadMonth", "afterRegistration", "inactiveUser", "daysUpdate" -> {
                    binding.tvName.setTextOnTextView(data.title, "")
                    binding.tvNotificationType.setTextOnTextView(data.body, "")
                }
                else -> {
                    binding.tvName.setTextOnTextView(data.sender_user?.name, "")
                    binding.tvNotificationType.setTextOnTextView(
                        getNotificationType(data.notification_type ?: "", context), ""
                    )
                }
            }

            if (data.character != null) {
                binding.tvOn.showView()
                binding.tvOn.setTextOnTextView(
                    "${context.getString(R.string.on)} ${data.character.name}",
                    ""
                )
            } else {
                binding.tvOn.hideView()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
