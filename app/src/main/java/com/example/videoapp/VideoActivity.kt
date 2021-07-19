package com.example.videoapp

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.modules.core.PermissionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import io.socket.client.IO
import io.socket.client.Socket
import org.jitsi.meet.sdk.*
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Field
import java.net.MalformedURLException
import java.net.URL


class VideoActivity : AppCompatActivity(), JitsiMeetActivityInterface {

    private var mSocket: Socket? = null
    private var room: String = ""
    var mediaRecorder: MediaRecorder? = null
    var isTimerRunning: Boolean = false
    var isTimerSet: Boolean = false
    var timeWhenStopped: Long = 0
    var videoId: String = ""
    var videoPlayer : YouTubePlayer? = null


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

//        val action: String? = intent.action
//        val data: Uri? = intent.data

        room = intent.getStringExtra("ROOM_NAME").toString()

        connectToSocketServer()

        val serverURL: URL
        serverURL = try {
            URL("https://videoapp.live")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException("Invalid server URL!")
        }
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverURL)
            .setWelcomePageEnabled(true)
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
            mSocket?.on("video_shared") { args ->
                runOnUiThread {
                    if (args[0] != null)
                    showVideo(args[0] as String)
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
            mSocket?.on("timer_defined") { args ->
                runOnUiThread {
                    Log.i("TIMER_STARTED", args[0].toString())
                    if (args[0] != null)
                        setTimer(args[0] as Int)
                }
            }
            mSocket?.on("timer_started") { args ->
                runOnUiThread {
                    Log.i("TIMER_STARTED", "timer_started")
                    startTimer()
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

    fun showVideo (id: String) {
        val builder = AlertDialog.Builder(this)
        val videoView = VideoView(this)
//        val videoView = findViewById<VideoView>(R.id.videoView)
//        val uri: Uri = Uri.parse("android.resource://" + packageName + "/" + resources.getIdentifier(id, "raw", packageName))
//        videoView.setVideoURI(uri)
        videoView.setVideoPath("http://62.171.136.153/$id")
        builder.setView(videoView)
        builder.setNegativeButton(
                "Cancel"
        ) { dialog, which -> dialog.cancel() }
        builder.show()
        videoView.start()
    }

    fun onClickShowVideo () {
//        var arrayActions = ArrayList<String>()
        /*val fields: Array<Field> = R.raw::class.java.fields
        fields.forEach {field ->
            if (field.name.contains("video_")) {
                arrayActions.add(field.name)
                Log.d("RAW", String.format(
                        "name=\"%s\", id=0x%08x",
                        field.name, field.getInt(field))
                )
            }
        }

        var array = arrayActions.toTypedArray()*/
        val arrayActions = arrayOf("The_Burpee.mp4")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Link do video")

        builder.setItems(arrayActions,
                DialogInterface.OnClickListener { dialog, wich ->
                    val obj = JSONObject()
                    obj.put("room_id", room)
                    obj.put("video_name", "The_Burpee.mp4")
                    mSocket?.emit("share_video", obj)
                })

        builder.show()
    }

    fun setTimer (duration: Int) {
        val simpleChronometer = findViewById<Chronometer>(R.id.simpleChronometer)
        simpleChronometer.isCountDown = true
        simpleChronometer.base = SystemClock.elapsedRealtime() + (duration * 60000)
        timeWhenStopped =  simpleChronometer.base - SystemClock.elapsedRealtime()
        simpleChronometer.visibility = View.VISIBLE
        isTimerSet = true
    }

    fun startTimer () {
        val simpleChronometer = findViewById<Chronometer>(R.id.simpleChronometer)
//        simpleChronometer.isCountDown = true
        simpleChronometer.base = SystemClock.elapsedRealtime() + timeWhenStopped
//        simpleChronometer.visibility = View.VISIBLE
        simpleChronometer.start()
        isTimerRunning = true
    }

    fun stopTimer () {
        val simpleChronometer = findViewById<Chronometer>(R.id.simpleChronometer)
        timeWhenStopped = simpleChronometer.base - SystemClock.elapsedRealtime()
        simpleChronometer.stop()
        simpleChronometer.visibility = View.INVISIBLE
        isTimerSet = false
        isTimerRunning = false
    }

    fun onClickStopTimer () {
        val obj = JSONObject()
        obj.put("room_id", room)
        mSocket?.emit("stop_timer", obj)
    }

    fun onClickStartTimer () {
        val obj = JSONObject()
        obj.put("room_id", room)
        mSocket?.emit("start_timer", obj)
    }

    fun onClickSetTimer () {
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
            mSocket?.emit("set_timer", obj)
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

    fun onClickShareMeeting () {
        /*val builder = AlertDialog.Builder(this)
        builder.setTitle("Inserir número para partilhar (incluir indicativo sem o '+')")
        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) { dialog, which ->
            val url = "https://api.whatsapp.com/send?phone=${input.text}&text=Junte-se+%C3%A1+reuni%C3%A3o+com+o+seguinte+id%3A+${room}"
//            val url = "https://api.whatsapp.com/send?text=Junte-se+%C3%A1+reuni%C3%A3o+com+o+seguinte+id%3A+${room}"
            val i = Intent(Intent.ACTION_VIEW).apply {
                setData(Uri.parse(url))
            }
            startActivity(i)
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        builder.show()*/
        val url = "https://api.whatsapp.com/send?text=Junte-se%20ao%20treino%20com%20o%20seguinte%20id%3A%20${room}"
        val i = Intent(Intent.ACTION_VIEW).apply {
            setData(Uri.parse(url))
        }
        startActivity(i)
    }

    fun onClickFabButton (v: View) {
        Log.i("FAB", "FAB CLICKED")
        val builder = AlertDialog.Builder(this)
        var arrayActions = emptyArray<String>()
        if (!isTimerRunning) {
            if (isTimerSet) {
                arrayActions = arrayOf("Show Video", "Start Timer", "Share Meeting")
            } else {
                arrayActions = arrayOf("Show Video", "Set Timer", "Share Meeting")
            }
        } else {
            arrayActions = arrayOf("Show Video", "Stop Timer", "Share Meeting")
        }

        builder.setTitle("Acções")
                .setItems(arrayActions,
                        DialogInterface.OnClickListener { dialog, wich ->
                            when (wich) {
                                0 -> onClickShowVideo()
                                1 ->
                                    if (isTimerRunning) {
                                        onClickStopTimer()
                                    }  else {
                                        if (isTimerSet) {
                                            onClickStartTimer()
                                        } else {
                                            onClickSetTimer()
                                        }
                                    }
                                2 -> onClickShareMeeting()
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

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        JitsiMeetActivityDelegate.requestPermissions(this, p0, p1, p2)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}