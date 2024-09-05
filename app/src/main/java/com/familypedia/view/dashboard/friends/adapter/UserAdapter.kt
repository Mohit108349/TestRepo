package com.familypedia.view.dashboard.friends.adapter

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ItemUserBinding
import com.familypedia.network.User
import com.familypedia.utils.Constants
import com.familypedia.utils.Utility
import com.familypedia.utils.Utility.getTimeAgo
import com.familypedia.utils.hideView
import com.familypedia.utils.loadImagesWithGlide
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.utils.setTextOnTextView
import com.familypedia.view.dashboard.profile.ViewProfileActivity
import com.familypedia.utils.listeners.UserListener
import com.familypedia.utils.showView
import io.ak1.pix.helpers.hide

class UserAdapter(
    private val context: Activity,
    private val userList: List<User>,
    private val userListener: UserListener
) : RecyclerView.Adapter<UserAdapter.CharacterViewHolder>() {

    private var userId = Utility.getPreferencesString(context, Constants.USER_ID)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class CharacterViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            with(binding) {
                // Load user image
                ivUserImage.loadImagesWithGlide(
                    Constants.IMAGE_URL + user.profile_pic,
                    true
                )

                // Set user name
                tvUserName.setTextOnTextView(user.name, "")

                // Hide and show views based on user status
                val value = user.email ?: user.phone_number ?: ""
                if (value.contains("@")) {
                    val stringFull = user.email
                    val before = stringFull?.substringBefore("@")
                    val size = stringFull?.substringAfter("@")?.length ?: 0
                    val astresikToAdd = "*".repeat(size)
                    tvUserId.setTextOnTextView("$before$astresikToAdd", "")
                } else {
                    val number = user.phone_number.toString()
                    val numStar = number.length - 5
                    val modified = "${number.substring(0, numStar)}*****"
                    tvUserId.setTextOnTextView(modified, "")
                }

                // Set button states based on user status
                when (user.block_status) {
                    "Not" -> {
                        if (user.isFriend == 1) {
                            btnFirendAccept.hideView()
                            btnFirendReject.hideView()
                            btnFriendStatus.showView()
                            btnFriendStatus.setTextOnTextView(context.getString(R.string.friend), "")
                            btnFriendStatus.background =
                                ContextCompat.getDrawable(context, R.drawable.bg_shape_button_semi_rect_grey)
                        } else {
                            btnFirendAccept.hideView()
                            btnFirendReject.hideView()
                            btnFriendStatus.showView()
                            if (!user.isBlocked) {
                                btnFriendStatus.setTextOnTextView(
                                    context.getString(R.string.become_friend),
                                    ""
                                )
                                btnFriendStatus.setSafeOnClickListener {
                                    if (user.isPending != 1) {
                                        userListener.sendFriendRequest(user)
                                    }
                                }
                                btnFriendStatus.background =
                                    ContextCompat.getDrawable(context, R.drawable.bg_shape_button_semi_rect)
                            } else {
                                btnFriendStatus.hideView()
                            }
                        }

                        if (user.isPending == 1) {
                            btnFriendStatus.setTextOnTextView(context.getString(R.string.pending), "")
                            btnFriendStatus.background =
                                ContextCompat.getDrawable(context, R.drawable.bg_shape_button_semi_rect_grey)
                        }

                        if (user.isRecived == 1) {
                            btnFriendStatus.hide()
                            btnFirendAccept.showView()
                            btnFirendReject.showView()
                            btnFirendAccept.setTextOnTextView(context.getString(R.string.accept), "")
                            btnFirendAccept.background =
                                ContextCompat.getDrawable(context, R.drawable.bg_shape_button_semi_rect)
                            btnFirendReject.setTextOnTextView(context.getString(R.string.reject), "")
                            btnFirendReject.background =
                                ContextCompat.getDrawable(context, R.drawable.bg_shape_button_semi_rect_grey)

                            btnFirendAccept.setSafeOnClickListener {
                                userListener.acceptRequest(user)
                            }
                            btnFirendReject.setSafeOnClickListener {
                                userListener.rejectRequest(user)
                            }
                        }
                    }

                    "Blocked By Me" -> {
                        btnFriendStatus.hideView()
                    }
                }

                // Handle item click
                itemUser.setSafeOnClickListener {
                    val bundle = Bundle().apply {
                        putString(Constants.FROM, Constants.FROM_FRIEND_REQUEST)
                        putString(Constants.USER_ID, user._id)
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
