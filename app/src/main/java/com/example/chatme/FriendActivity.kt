package com.example.chatme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatme.Utils.Friends
import com.example.chatme.holder.FriendHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_find_friend.app_bar
import kotlinx.android.synthetic.main.activity_friend.recyclerView_Friends
import kotlinx.android.synthetic.main.single_view_friend.view.circleImageView2_my_friend
import kotlinx.android.synthetic.main.single_view_friend.view.city_my_friend
import kotlinx.android.synthetic.main.single_view_friend.view.username_my_friend

class FriendActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mRef: DatabaseReference;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)
        setSupportActionBar(app_bar as Toolbar?)
        supportActionBar?.title = "Friends";
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.currentUser!!;
        mRef= FirebaseDatabase.getInstance().reference.child("Friends");

        recyclerView_Friends.layoutManager= LinearLayoutManager(this@FriendActivity);
        LoadFriends("")

    }

    private fun LoadFriends(s: String) {
        val query = mRef.child(mUser.uid).orderByChild("username").startAt(s)

        val options = FirebaseRecyclerOptions.Builder<Friends>()
            .setQuery(query, Friends::class.java)
            .setLifecycleOwner(this)
            .build()


        val adapter = object : FirebaseRecyclerAdapter<Friends, FriendHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
                return FriendHolder(
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.single_view_friend, parent, false)
                )
            }

            override fun onBindViewHolder(holder: FriendHolder, position: Int, model: Friends) {
                holder.itemView.username_my_friend.text = model.username
                holder.itemView.city_my_friend.text = model.city

                Glide.with(holder.itemView.context)
                    .load(model.profileImageUrl)
                    .into(holder.itemView.circleImageView2_my_friend)
                holder.itemView.setOnClickListener {
                    val intent= Intent(this@FriendActivity,ChatActivity::class.java)
                    intent.putExtra("OtherUserID",getRef(position).key.toString())
                    startActivity(intent);
                }
            }

        }
        adapter.startListening()
        recyclerView_Friends.adapter = adapter
    }

}