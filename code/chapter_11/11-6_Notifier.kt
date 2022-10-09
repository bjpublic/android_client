package online.dailyq

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object Notifier {
    const val ANSWER_CHANNEL_ID = "answer"
    const val FOLLOW_CHANNEL_ID = "follow"
    var NOTI_ID = 1000

    fun init(context: Context) {
        createAnswerChannel(context)
        createFollowChannel(context)
    }

    fun createAnswerChannel(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        val channel = NotificationChannel(
            ANSWER_CHANNEL_ID,
            context.getString(R.string.noti_answer_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)
    }

    fun createFollowChannel(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        val channel = NotificationChannel(
            FOLLOW_CHANNEL_ID,
            context.getString(R.string.noti_follow_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)
    }

    fun showAnswerNotification(context: Context, username: String) {
        val contentText = context.getString(R.string.noti_answer_msg, username)

        val builder = NotificationCompat.Builder(context, ANSWER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dailyq)
            .setContentTitle(context.getString(R.string.noti_answer_channel))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val nm = NotificationManagerCompat.from(context)
        nm.notify(contentText.hashCode(), builder.build())
    }

    fun showFollowNotification(context: Context, username: String) {
        val contentText = context.getString(R.string.noti_answer_msg, username)

        val builder = NotificationCompat.Builder(context, FOLLOW_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dailyq)
            .setContentTitle(context.getString(R.string.noti_follow_channel))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val nm = NotificationManagerCompat.from(context)
        nm.notify(contentText.hashCode(), builder.build())
    }
}
