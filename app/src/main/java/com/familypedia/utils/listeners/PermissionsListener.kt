package com.familypedia.utils.listeners

import com.familypedia.network.AskCharacterPermissionRequest
import com.familypedia.network.User

interface PermissionsListener {
    fun onRequestAccept(data: AskCharacterPermissionRequest)
    fun onRequestReject(data:AskCharacterPermissionRequest)
    fun onDelete(data:AskCharacterPermissionRequest)
    fun onItemClick(user: User)
}