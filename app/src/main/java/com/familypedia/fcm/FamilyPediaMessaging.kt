package com.familypedia.fcm

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.familypedia.FamilyPediaApplication
import com.familypedia.R
import com.familypedia.SplashActivity
import com.familypedia.network.CharacterData
import com.familypedia.network.User
import com.familypedia.utils.Constants
import com.familypedia.utils.Constants.CHARACTER_ID
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.NOTIFICATION
import com.familypedia.utils.Constants.NOTIFICATION_TYPE
import com.familypedia.utils.Constants.USER_ID
import com.familypedia.utils.Utility.savePreferencesString
import com.familypedia.view.dashboard.DashboardActivity
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity
import com.familypedia.view.dashboard.character.addCharacter.AddCharacterListingActivity
import com.familypedia.view.dashboard.character.post.AddNewPostActivity
import com.familypedia.view.notifications.NotificationsActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.json.JSONObject

private const val TAG = "FamilyPediaMessaging"

class FamilyPediaMessaging : FirebaseMessagingService() {
    val FCM_PARAM = "picture"
    private val CHANNEL_NAME = "FCM_CHANNEL"
    private val CHANNEL_DESC = "Firebase Cloud Messaging"
    private var numMessages = 0
    val NOTIFICATION_ID = 0
    private var notificationType = ""

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        savePreferencesString(FamilyPediaApplication.instance, Constants.FCM_TOKEN, p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        try {
            if (p0.data.isNotEmpty()) {
                Log.e(TAG, "Data Payload :: " + p0.data.toString())
                Log.e(TAG, "Notification Payload :: " + p0.notification.toString())
                Log.e(TAG, JSONObject(p0.data as Map<*, *>).toString())

                val dataKey = p0.data
                val notificationKey = p0.notification
                notificationType = dataKey["type"].toString()
                val title = notificationKey?.title ?: ""
                val message = notificationKey?.body ?: ""

                when (notificationType) {
                    "daysUpdate" -> {
                        NotificationsActivity.isNotification=true
                        val intent = Intent(this, NotificationsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        showSmallNotification(title, message, intent)

                    }

                    "biosBirthday" -> {
                        val intent = Intent(this, AboutCharacterActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        val userObj = p0.data["user"]
                        userObj?.run {
                            val userFromJSON = Gson().fromJson(this, User::class.java)
                            val userId = userFromJSON._id
                            val userName = userFromJSON.name
                            println("Retrieved User ID ::  $userId")
                            println("Retrieved User Name ::  $userName")
                            intent.putExtra(USER_ID, userId)
                            intent.putExtra(Constants.USER_NAME, userName)
                            intent.addNavigateToExtra("biosBirthday")

                        }

                        showSmallNotification(title, message, intent)

                    }
                    "accessingTime" -> {
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        showSmallNotification(title, message, intent)
                    }
                    "uploadMonth" -> {
                        val intent = Intent(this, AddNewPostActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        val userObj = p0.data["user"]
                        userObj?.run {
                            val userFromJSON = Gson().fromJson(this, User::class.java)
                            val userId = userFromJSON.postData?._id
                            val userName = userFromJSON.postData?.name
                            intent.putExtra(CHARACTER_ID, userId)
                            intent.putExtra(
                                Constants.CHARACTER_DATA,
                                CharacterData(
                                    null,
                                    null,
                                    userName,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                                )
                            )
                            intent.addNavigateToExtra("uploadMonth")

                        }
                        showSmallNotification(title, message, intent)
                    }
                    "afterRegistration" -> {
                        val intent = Intent(this, AddCharacterListingActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        showSmallNotification(title, message, intent)
                    }
                    Constants.NOTIFICATION_TYPE_NEW_POST_CREATED -> {
                        NotificationsActivity.isNotification=true
                        val intent = Intent(this, NotificationsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        showSmallNotification(title, message, intent)
                    }
                    Constants.NOTIFICATION_TYPE_CREATE_BIOGRAPHY, Constants.NOTIFICATION_TYPE_POST_EDITED, Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_RECEIVED,
                    Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_REJECTED, Constants.NOTIFICATION_TYPE_FRIEND_REQUEST_ACCEPTED, Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_RECEIVED, Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_ACCEPTED,
                    Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REQUEST_REJECTED, Constants.NOTIFICATION_TYPE_BIOGRAPHY_PERMISSION_REMOVED,
                    -> {
                        NotificationsActivity.isNotification=true
                        val intent = Intent(this, NotificationsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        showSmallNotification(title, message, intent)
                    }
                    else -> {
                        val intent = Intent(this, SplashActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        showSmallNotification(title, message, intent)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "error : ${e.localizedMessage} ")
        }
    }

    private fun sendNotification(
        notification: RemoteMessage.Notification?,
        remoteMessage: RemoteMessage,
    ) {
        try {
            println("JsonisHere ${remoteMessage.data}")
            val title = remoteMessage.data["title"] ?: ""
            var intent: Intent? = null
            val bundle = Bundle()
            bundle.putString(NOTIFICATION_TYPE, notificationType)
            bundle.putString(FROM, NOTIFICATION)
            intent = Intent(this, NotificationsActivity::class.java)
            intent.putExtras(bundle)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.getActivity(
                        this, 0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    PendingIntent.getActivity(
                        this, 0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
            val notificationBuilder =
                NotificationCompat.Builder(
                    this,
                    getString(R.string.default_notification_channel_id)
                )
                    .setContentTitle(notification?.title)
                    .setContentText(notification?.body)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.win))
                    .setContentIntent(pendingIntent)
                    .setContentInfo("FamilyPedia")
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(ContextCompat.getColor(this, R.color.color_blue))
                    .setLights(Color.RED, 1000, 300)
                    //.setNumber(++numMessages)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVibrate(LongArray(0))
                    .setAutoCancel(true) //auto remove notification when user taps it

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    getString(R.string.default_notification_channel_id),
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = CHANNEL_DESC
                channel.setShowBadge(true)
                channel.enableLights(true)
                channel.lightColor = Color.parseColor("#FF6600")
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(/*NOTIFICATION_ID*/++numMessages,
                notificationBuilder.build()
            )
        } catch (e: Exception) {

        }
        /*  }    private fun sendNotificationWithIntent(
        notification: RemoteMessage.Notification?,
        intent:Intent
    ) {
        try {

            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val notificationBuilder =
                NotificationCompat.Builder(
                    this,
                    getString(R.string.default_notification_channel_id)
                )
                    .setContentTitle(notification?.title)
                    .setContentText(notification?.body)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.win))
                    .setContentIntent(pendingIntent)
                    .setContentInfo("FamilyPedia")
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(ContextCompat.getColor(this, R.color.color_blue))
                    .setLights(Color.RED, 1000, 300)
                    //.setNumber(++numMessages)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVibrate(LongArray(0))
                    .setAutoCancel(true) //auto remove notification when user taps it

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    getString(R.string.default_notification_channel_id),
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = CHANNEL_DESC
                channel.setShowBadge(true)
                channel.enableLights(true)
                channel.lightColor = Color.parseColor("#FF6600")
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(*//*NOTIFICATION_ID*//*++numMessages,
                notificationBuilder.build()
            )
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }
*/


    }

    fun showSmallNotification(
        title: String?,
        message: String?,
        intent: Intent,
    ) {
        /*Setting Mutability FLAG TO FLAG_IMMUTABLE for Android 12 Above devices*/

        val CHANNEL_ID = "channel_1"
        val resultPendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        val mChannel: NotificationChannel
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mChannel = NotificationChannel(CHANNEL_ID, getString(R.string.app_name), importance)
            mChannel.description = message
            mChannel.enableLights(true)
            mChannel.lightColor = Color.GREEN
            mChannel.setShowBadge(true)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(mChannel)
        }
        val notification: Notification =
            mBuilder.setSmallIcon(R.mipmap.ic_launcher).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(applicationContext, R.color.white))
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setChannelId(CHANNEL_ID)
                .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

    }


    private fun Intent.addNavigateToExtra(type: String) {
        putExtra("navigateTo", type)
    }
}