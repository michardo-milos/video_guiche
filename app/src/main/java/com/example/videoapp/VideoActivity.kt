package com.example.videoapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaDrmResetException
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videoapp.MainActivity
import com.example.videoapp.R
import com.facebook.react.modules.core.PermissionListener
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.transports.Polling
import io.socket.engineio.client.transports.PollingXHR
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetView
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL


class VideoActivity : AppCompatActivity(), JitsiMeetActivityInterface {

    private var mSocket: Socket? = null
    private var room: String = ""
    var mediaRecorder: MediaRecorder? = null
    var isTimerRunning: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        room = intent.getStringExtra("ROOM_NAME").toString()

        connectToSocketServer()

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
            .setWelcomePageEnabled(true)
//            .setFeatureFlag("meeting-name.enabled", false)
            .setFeatureFlag("pip.enabled", false)
            .setFeatureFlag("close-captions.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)
        val layout = findViewById<FrameLayout>(R.id.frameLayout)
        val view = JitsiMeetView(this)

        val options = JitsiMeetConferenceOptions.Builder()
            .setRoom(room)
            .build()
        view.join(options)
        view.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1450, Gravity.CENTER_VERTICAL)

        layout.addView(view)

//        Log.i("SOCKET_INFO", mSocket?.connected().toString())

    }


    fun connectToSocketServer () {
        try {
            val options = IO.Options()
            options.reconnection = true
//            options.forceNew = true
//            options.transports = arrayOf(Polling.NAME, PollingXHR.NAME, io.socket.engineio.client.transports.WebSocket.NAME)
            mSocket = IO.socket("http://62.171.136.153:5000", options)
            Log.i("SOCKET_INFO", mSocket.toString())
            mSocket?.connect()
            mSocket?.on(Socket.EVENT_CONNECT) {
                runOnUiThread {
                    Log.i("SOCKET_INFO", "EVENT_CONNECT")
                    val obj = JSONObject()
                    obj.put("room_id", room)
                    mSocket?.emit("join_room", obj)
                }
            }
            mSocket?.on("video_shared") {
                runOnUiThread {
                    showVideo()
                }
            }
            mSocket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                if (args[0] != null) {
                    Log.i("SOCKET_INFO", "EVENT_CONNECT_ERROR")
                    for (index: Int in args.indices) {
                        Log.i("SOCKET_INFO", args[index].toString())
                    }
                }
            }
            mSocket?.on("timer_started") { args ->
                runOnUiThread {
                    Log.i("TIMER_STARTED", args[0].toString())
                    if (args[0] != null) {
                        startTimer(args[0].toString().toInt())
                    }
                }
            }
            mSocket?.on("timer_stoped") { args ->
                runOnUiThread {
                    stopTimer()
                }
            }
            Log.i("SOCKET_INFO", mSocket?.connected().toString())
        } catch (e: Exception) {
            Log.i("SOCKET_INFO", e.toString())
            mSocket = null
        }
    }

    fun showVideo () {
        val videoView = findViewById<VideoView>(R.id.videoView)
        val simpleChronometer = findViewById<Chronometer>(R.id.simpleChronometer)
        val videoViewHeight = videoView.height
        val uri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.test)
        videoView.setVideoURI(uri)
        videoView.visibility = View.VISIBLE
        simpleChronometer.visibility = View.INVISIBLE
        videoView.start()
    }

    fun onClickShowVideo () {
//        showVideo()
        val obj = JSONObject()
        obj.put("room_id", room)
        mSocket?.emit("share_video", obj)
//        videoView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, videoViewHeight)
    }
    fun startTimer (duration: Int) {
        val videoView = findViewById<VideoView>(R.id.videoView)
        val simpleChronometer = findViewById<Chronometer>(R.id.simpleChronometer)
        simpleChronometer.isCountDown = true
        simpleChronometer.base = SystemClock.elapsedRealtime() + (duration * 60000)
        simpleChronometer.visibility = View.VISIBLE
        videoView.visibility = View.INVISIBLE
        simpleChronometer.start()
        isTimerRunning = true
    }

    fun stopTimer () {
        val simpleChronometer = findViewById<Chronometer>(R.id.simpleChronometer)
        simpleChronometer.stop()
        simpleChronometer.visibility = View.INVISIBLE
        isTimerRunning = false
    }


    fun onClickStopTimer () {
        val obj = JSONObject()
        obj.put("room_id", room)
        mSocket?.emit("stop_timer", obj)
    }

    fun onClickStartTimer () {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tempo do timer")
        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton(
                "OK"
        ) { dialog, which ->
            val obj = JSONObject()
            obj.put("room_id", room)
            obj.put("duration", input.text.toString().toInt())
            mSocket?.emit("start_timer", obj)
//            startTimer(input.text.toString().toInt())
        }
        builder.setNegativeButton(
                "Cancel"
        ) { dialog, which -> dialog.cancel() }

        builder.show()
//        videoView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, videoViewHeight)
    }

    fun onClickRecord () {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder?.prepare()
        mediaRecorder?.start()
    }

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        TODO("Not yet implemented")
    }

    fun onClickFabButton (v: View) {
        Log.i("FAB", "FAB CLICKED")
        val builder = AlertDialog.Builder(this)
        var arrayActions = emptyArray<String>()
        if (!isTimerRunning) {
            arrayActions = arrayOf("Show Video", "Start Timer", "Record")
        } else {
            arrayActions = arrayOf("Show Video", "Stop Timer", "Record")
        }

        builder.setTitle("Acções")
                .setItems(arrayActions,
                        DialogInterface.OnClickListener { dialog, wich ->
                            when (wich) {
                                0 -> onClickShowVideo()
                                1 -> if (isTimerRunning) onClickStopTimer() else onClickStartTimer()
                                2 -> onClickRecord()
                            }
                        })
        builder.show()
    }

    fun onBroadcastReceived (intent: Intent) {
        Log.i("CONFERENCE_TERMINATED", "onBroadcastReceived")
        if (intent.type == "CONFERENCE_TERMINATED") {
            val mainActivityItent = Intent(this, MainActivity::class.java).apply {}
            startActivity(mainActivityItent)
        }
    }
}