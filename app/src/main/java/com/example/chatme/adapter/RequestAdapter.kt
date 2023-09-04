package com.example.chatme.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatme.ChatActivity
import com.example.chatme.R
import com.example.chatme.ViewFriendActivity
import com.example.chatme.data.RequestWithUsername
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.single_view_friend.view.circleImageView2_my_friend

class RequestAdapter(private val requestList: List<RequestWithUsername>) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_view_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requestList[position]
        holder.usernameTextView.text = request.username
        holder.cityTextView.text = request.city
        // You can set other views in your item layout as needed.
        Glide.with(holder.itemView.context)
            .load(request.image)
            .into(holder.imageTextView)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewFriendActivity::class.java)
            intent.putExtra("userKey", request.requestId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.username_my_friend)
        val cityTextView: TextView = itemView.findViewById(R.id.city_my_friend)
        val imageTextView: CircleImageView = itemView.findViewById(R.id.circleImageView2_my_friend)
        // Define other views here as needed.
    }
}