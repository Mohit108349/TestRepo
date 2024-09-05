package com.familypedia.utils.listeners

import com.familypedia.network.User

interface UserListener {
    fun sendFriendRequest(user: User)
    fun reportUser(user_id: String)
    fun blockUser(user_id: String)
    fun unBlockUser(user_id: String)
    fun acceptRequest(user: User)
    fun rejectRequest(user: User)
}