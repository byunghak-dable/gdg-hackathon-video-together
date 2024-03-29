package org.personal.videotogether

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import io.sentry.event.BreadcrumbBuilder
import io.sentry.event.UserBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.domianmodel.InviteYoutubeData
import org.personal.videotogether.util.SharedPreferenceHelper
import org.personal.videotogether.viewmodel.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val TAG by lazy { javaClass.name }

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private val userViewModel: UserViewModel by viewModels()
    private val friendViewModel: FriendViewModel by viewModels()
    private val youtubeViewModel: YoutubeViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val socketViewModel: SocketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSentry()
        socketViewModel.setStateEvent(SocketStateEvent.ConnectToTCPServer)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent!!.getStringExtra("request")) {
            "chat" -> {
                val chatRoomData = intent.getParcelableExtra<ChatRoomData>("chatRoomData")!!
                chatViewModel.setStateEvent(ChatStateEvent.OnNotification(chatRoomData))
            }

            "youtubeData" -> {
                val inviteYoutubeData = intent.getParcelableExtra<InviteYoutubeData>("inviteYoutubeData")!!
                youtubeViewModel.setStateEvent(YoutubeStateEvent.OnNotification(inviteYoutubeData))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sharedPreferenceHelper.setBoolean(this, getText(R.string.is_app_on).toString(), true)
        Log.i("TAG", "onCreate: ${sharedPreferenceHelper.getBoolean(this, getText(R.string.is_app_on).toString())}")
    }

    override fun onStop() {
        super.onStop()
        sharedPreferenceHelper.setBoolean(this, getText(R.string.is_app_on).toString(), false)
        Log.i("TAG", "onCreate: ${sharedPreferenceHelper.getBoolean(this, getText(R.string.is_app_on).toString())}")
    }

    private fun setSentry() {
        val sentryDSN = "https://94899e959a144d99abef9f1e3acca503@o438649.ingest.sentry.io/5403740"
        Sentry.init(sentryDSN, AndroidSentryClientFactory(this))
        Sentry.getContext().user = UserBuilder().setUsername("visitors").build()
        Sentry.getContext().recordBreadcrumb(BreadcrumbBuilder().setMessage("test").build())
    }
}