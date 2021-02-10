package com.example.videoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.modules.core.PermissionListener
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetView
import java.net.MalformedURLException
import java.net.URL


class VideoActivity : AppCompatActivity(), JitsiMeetActivityInterface {

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                onBroadcastReceived(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        Log.i("VIDEO_BUTTON", "created")

        val room = intent.getStringExtra("ROOM_NAME")

        val serverURL: URL
        serverURL = try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            URL("https://videoapp.live")
            // URL("https://meet.jit.si")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException("Invalid server URL!")
        }
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverURL)
            // When using JaaS, set the obtained JWT here
            //.setToken("MyJWT")
            .setWelcomePageEnabled(true)
            .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)
        val layout = findViewById<FrameLayout>(R.id.frameLayout)
        val view = JitsiMeetView(this)

        val options = JitsiMeetConferenceOptions.Builder()
            .setRoom(room)
            .build()
//         Launch the new activity with the given options. The launch() method takes care
        // of creating the required Intent and passing the options.
        // JitsiMeetActivity.launch(this, options)
        // JitsiMeetActivity.launch(this, options)
        view.join(options)
        view.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1450, Gravity.CENTER_VERTICAL)

//        val params = ViewGroup.LayoutParams(70, 60)

        layout.addView(view)

    }

    fun onClickShowVideo (v: View?) {
        val videoView = findViewById<VideoView>(R.id.videoView)
        val videoViewHeight = videoView.height
        val uri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.test)
        videoView.setVideoURI(uri)
        videoView.start()
//        videoView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, videoViewHeight)
    }

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        TODO("Not yet implemented")
    }

    fun onBroadcastReceived (intent: Intent) {
        Log.i("CONFERENCE_TERMINATED", "onBroadcastReceived")
        if (intent.type == "CONFERENCE_TERMINATED") {
            val mainActivityItent = Intent(this, MainActivity::class.java).apply {}
            startActivity(mainActivityItent)
        }
    }
}