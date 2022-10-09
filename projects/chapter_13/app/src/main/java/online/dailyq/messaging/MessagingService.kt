package online.dailyq.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import online.dailyq.AuthManager
import online.dailyq.Notifier
import online.dailyq.api.ApiService

class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.let {
            when (it["type"]) {
                "follow" -> {
                    Notifier.showFollowNotification(this, it["username"]!!)
                }
                "answer" -> {
                    Notifier.showAnswerNotification(this, it["username"]!!)
                }
            }
        }
    }
}
