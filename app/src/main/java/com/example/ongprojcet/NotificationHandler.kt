package com.example.ongprojcet

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.ongprojcet.R

class NotificationHandler(private val context: Context) {
    lateinit var notificationChannel : NotificationChannel
    lateinit var builder : Notification.Builder
    private val channelId = "com.example.ongprojcet"
    private val description = "Test notification"
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
//    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    fun notify(msg : String, intent: Intent) {
        //val intent = Intent(context, LauncherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context,0, intent,PendingIntent.FLAG_CANCEL_CURRENT)

        val contentView = RemoteViews(context.packageName,R.layout.notification_layout)
        contentView.setTextViewText(R.id.tv_title,"요트")
        contentView.setTextViewText(R.id.tv_content,msg)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(context,channelId)
                .setContent(contentView)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setContentText(msg)
                .setStyle(Notification.BigTextStyle()
                    .bigText(msg))
        }else{
            builder = Notification.Builder(context)
                .setContent(contentView)
                .setContentText(msg)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setStyle(Notification.BigTextStyle()
                    .bigText(msg))
        }

        notificationManager.notify(1234,builder.build())
    }

}



