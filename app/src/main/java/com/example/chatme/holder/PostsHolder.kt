package com.example.chatme.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatme.R
import com.example.chatme.Utils.Posts

class PostsHolder(val customView: View, var category: Posts? = null) :
    RecyclerView.ViewHolder(customView) {
    val postTo_: TextView = itemView.findViewById(R.id.to)
//    val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

    fun bind(category: Posts) {
    }
}