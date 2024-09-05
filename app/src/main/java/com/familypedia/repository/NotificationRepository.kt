package com.familypedia.repository

import com.familypedia.network.*
import retrofit2.Response

object NotificationRepository {
    private var repository: NotificationRepository? = null
    private lateinit var api: FamilyPediaServices

    val instance: NotificationRepository
        get() {
            repository = NotificationRepository
            api = ServiceGenerator.createService(FamilyPediaServices::class.java)
            return repository!!
        }

    suspend fun getNotifications(page: Int): Response<NotificationResponseModel> {
        return api.getNotificationHistory(page)
    }

    suspend fun getInvitationList(page: Int): Response<InvitationListResponse> {
        return api.getInvitationList(page)
    }

    suspend fun getUnreadNotificationsCount():Response<NotificationCountResponseModel>{
        return api.getNotificationCount()
    }
    suspend fun readNotifications(request: ReadNotificationRequest):Response<SimpleResponseModel>{
        return api.readNotification(request)
    }

}
