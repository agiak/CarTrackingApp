package com.agcoding.cartrackingapp.cloud.push

import com.agcoding.cartrackingapp.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Receives pushes from the server.
 *
 * The backend sends **data-only** messages, and that is deliberate. A message
 * carrying a `notification` block is drawn by the system while the app is in
 * the background and this class is never called — which would mean no entry in
 * the app's own notification history, and no say over how it looks. Data-only
 * costs one thing in exchange: the message is dropped if the app has been
 * force-stopped, which is the right trade for an app whose notifications are
 * informational.
 */
@AndroidEntryPoint
class CaribouMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var registrar: PushTokenRegistrar

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        registrar.onTokenRefreshed(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // `notification` is read as a fallback so a message sent by hand from
        // the Firebase console still shows something while the app is open.
        val title = message.data[KEY_TITLE] ?: message.notification?.title
        val body = message.data[KEY_BODY] ?: message.notification?.body.orEmpty()

        if (title.isNullOrBlank()) {
            Timber.w("Push with no title ignored: %s", message.data)
            return
        }
        notificationHelper.showPushNotification(title, body)
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}
