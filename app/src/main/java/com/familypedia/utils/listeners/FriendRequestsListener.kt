package com.familypedia.utils.listeners

import com.familypedia.network.AskCharacterPermissionRequest
import com.familypedia.network.NotificationData
import com.familypedia.network.RequestList
import com.familypedia.network.User

interface FriendRequestsListener {
    fun onRequestAccept(user:User,requestData: RequestList)
    fun onRequestReject(user:User,requestData: RequestList)
    fun onItemClick(user: User)
    fun onListSizeChanged(size:Int)
}
interface NotificationListener{
    fun onRequestAccept(data:NotificationData)
    fun onRequestReject(data:NotificationData)
    fun onItemClick(data:NotificationData)
}