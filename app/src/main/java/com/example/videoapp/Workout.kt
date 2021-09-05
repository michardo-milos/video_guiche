package com.example.videoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog

class Workout : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        val workoutList = findViewById<ListView>(R.id.list)
        val exercises = arrayOf(
            Exercise("Jumping Jacks", 25),
            Exercise("Abdominal Crunches", 15),
            Exercise("Russian Twist", 20),
            Exercise("Heel Touch", 30),
            Exercise("Leg Raises", 10),
            Exercise("Crossover Crunch", 20),
        )
        workoutList.adapter = ExerciseAdapter(this, exercises)
        workoutList.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val builder = AlertDialog.Builder(this)
                val videoView = VideoView(this)
                videoView.setVideoPath("http://62.171.136.153/The_Burpee.mp4")
                builder.setView(videoView)
                builder.setNegativeButton(
                    "Cancel"
                ) { dialog, _ -> dialog.cancel() }
                builder.show()
                videoView.start()
            }
    }
}