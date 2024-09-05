package com.familypedia.view.dashboard.friends.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ItemRequestBinding
import com.familypedia.network.RequestList
import com.familypedia.utils.Constants
import com.familypedia.utils.hideView
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.utils.loadImagesWithGlide
import com.familypedia.utils.listeners.FriendRequestsListener

class FriendRequestsAdapter(
    private val context: Context,
    private val dataList: List<RequestList>,
    private val characterId: String,
    private val from: String,
    private val listener: FriendRequestsListener
) : RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRequestBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        listener.onListSizeChanged(dataList.size)
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    inner class ViewHolder(private val binding: ItemRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: RequestList) {
            with(binding) {
                ivCharacterImage.loadImagesWithGlide(
                    Constants.IMAGE_URL + request.requester?.profile_pic,
                    true
                )
                tvCharacterName.text = request.requester?.name ?: ""

                if (from == Constants.FROM_POST_REQUEST) {
                    itemRequest.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                }
                if (from == Constants.FROM_FRIEND_LIST) {
                    btnAccept.hideView()
                    btnReject.hideView()
                }
                itemRequest.setSafeOnClickListener {
                    request.requester?.let { user ->
                        listener.onItemClick(user)
                    }
                }
                btnAccept.setSafeOnClickListener {
                    request.requester?.let {
                        listener.onRequestAccept(request.requester, request)
                    }
                }
                btnReject.setSafeOnClickListener {
                    request.requester?.let {
                        listener.onRequestReject(request.requester, request)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
