package com.example.videoapp

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class WorkoutPlans : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_plans)

        val workoutList = findViewById<ListView>(R.id.workoutList)
        val workouts = arrayOf("Treino de Abdominais", "Treino de Braços", "Treino de Pernas", "Treino de Ombros")
        workoutList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, workouts)

        workoutList.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, Workout::class.java)
            startActivity(intent)
        })

    }
}