package com.familypedia.utils.listeners

import com.familypedia.network.PostData

interface PostsListener {
    fun onEditClick(postData:PostData)
    fun onDeleteClick(postData:PostData)
    fun onReportClick(postData:PostData)
}