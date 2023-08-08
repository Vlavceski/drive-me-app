package com.example.chatme.Utils

import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatme.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    @SuppressLint("MissingPermission")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title
        val body = message.notification?.body

        val builder = NotificationCompat.Builder(applicationContext, "CHAT")
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setSmallIcon(R.drawable.ic_logo)

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(123, builder.build())
    }
}