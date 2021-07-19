package com.example.videoapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ExerciseAdapter(private val context: Context,
                      private val dataSource: Array<Exercise>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.list_with_text_side_to_side, parent, false)

        // Get title element
        val titleTextView = rowView.findViewById(R.id.title) as TextView

        // Get subtitle element
        val subtitleTextView = rowView.findViewById(R.id.subText) as TextView

        Log.i("Position", position.toString())
        val exercise = getItem(position) as Exercise

        titleTextView.text = exercise.title
        subtitleTextView.text = "x " + exercise.repetitions.toString()

        return rowView
    }
}