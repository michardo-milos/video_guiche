package com.example.videoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

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

    }
}