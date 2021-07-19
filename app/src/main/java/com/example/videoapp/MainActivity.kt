package com.example.videoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videoapp.VideoActivity
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun enterVideoConference(roomName: String) {
        val intent = Intent(this, VideoActivity::class.java).apply {
            putExtra("ROOM_NAME", roomName)
        }
        startActivity(intent)
    }

    fun onButtonClick(v: View?) {
        val room = Random.nextInt(111111, 999999).toString()
        enterVideoConference(room)

    }

    fun onJoinButtonClick (v: View) {
        var roomName = ""
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Juntar a uma sala")
        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) { dialog, which ->
            roomName = input.text.toString()
            enterVideoConference(roomName)
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        builder.show()
    }

    fun viewWorkoutPlans (v: View) {
        val intent = Intent(this, WorkoutPlans::class.java)
        startActivity(intent)
    }
}