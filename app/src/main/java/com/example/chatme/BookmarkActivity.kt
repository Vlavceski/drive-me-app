package com.example.chatme

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log.d
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatme.Utils.Posts
import com.example.chatme.data.Post
import com.example.chatme.holder.PostsHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_bookmark.btn_back_home_bookmark
import kotlinx.android.synthetic.main.activity_bookmark.rv_bookmark
import kotlinx.android.synthetic.main.activity_main.recyclerview
import kotlinx.android.synthetic.main.activity_splash.imageView
import kotlinx.android.synthetic.main.single_view_post2.view.bookmark
import kotlinx.android.synthetic.main.single_view_post2.view.date
import kotlinx.android.synthetic.main.single_view_post2.view.from
import kotlinx.android.synthetic.main.single_view_post2.view.postDesc
import kotlinx.android.synthetic.main.single_view_post2.view.profileImagePost
import kotlinx.android.synthetic.main.single_view_post2.view.profileUsernamePost
import kotlinx.android.synthetic.main.single_view_post2.view.timeAgo
import kotlinx.android.synthetic.main.single_view_post2.view.to
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.TimeZone

class BookmarkActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mBookmarkRef: DatabaseReference;
    private lateinit var mPostRef: DatabaseReference;

    //    private lateinit var adapter: AdapterBookmark
//    private lateinit var  firstChild :Any
//    private lateinit var childKey:String
//    private lateinit var childValue :Any
    private lateinit var childValue: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        btn_back_home_bookmark.setOnClickListener {
            val intent = Intent(this@BookmarkActivity, MainActivity::class.java)
            startActivity(intent)
        }
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.currentUser!!;
        mBookmarkRef = FirebaseDatabase.getInstance().reference.child("BookMark");
        mPostRef = FirebaseDatabase.getInstance().reference.child("Posts");

        rv_bookmark.layoutManager = LinearLayoutManager(this@BookmarkActivity)
        loadBookmarks()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadBookmarks() {
        val currentDate = System.currentTimeMillis()
        val query = mBookmarkRef.child(mUser.uid)

        val options = FirebaseRecyclerOptions.Builder<Posts>()
            .setQuery(query, Posts::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_bookmark.layoutManager = LinearLayoutManager(this)

        val adapter = object : FirebaseRecyclerAdapter<Posts, PostsHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsHolder {
                return PostsHolder(
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.single_view_post2, parent, false)
                )
            }

            override fun onBindViewHolder(
                holder: PostsHolder,
                position: Int,
                model: Posts
            ) {
                val postKey = getRef(position).key;
                val timeAg = calculateTimeAgo(model.dataPost)
                holder.customView.timeAgo.text = timeAg
                holder.customView.from.text = model.postFrom
                holder.customView.to.text = model.postTo
                holder.customView.postDesc.text = model.postDesc
                holder.customView.date.text = model.postDate
                Picasso.get().load(model.userProfileImageUrl)
                    .into(holder.customView.profileImagePost)
                holder.customView.profileUsernamePost.text = model.username
                holder.customView.bookmark.setImageDrawable(ContextCompat.getDrawable(this@BookmarkActivity, R.drawable.baseline_bookmark_added_24))
                holder.customView.bookmark.setOnClickListener {
                    mBookmarkRef.child(mUser.uid).child(postKey!!).removeValue()
                }
                holder.customView.profileImagePost.setOnClickListener {
                    if(mUser.uid.equals(model.uid_user)){
                        val intent = Intent(this@BookmarkActivity, ProfileActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        val intent= Intent(this@BookmarkActivity,ViewFriendActivity::class.java)
                        intent.putExtra("userKey",model.uid_user.toString())
                        startActivity(intent)
                    }
                }
                holder.customView.profileUsernamePost.setOnClickListener {
                    if(mUser.uid.equals(model.uid_user)){
                        val intent = Intent(this@BookmarkActivity, ProfileActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        val intent= Intent(this@BookmarkActivity,ViewFriendActivity::class.java)
                        intent.putExtra("userKey",model.uid_user.toString())
                        startActivity(intent)
                    }
                }
            }
        }
        adapter.notifyDataSetChanged()
        adapter.startListening()
        rv_bookmark.adapter = adapter
    }
    @SuppressLint("SimpleDateFormat")
    private fun calculateTimeAgo(dataPost: String?): String? {
        val sdf = SimpleDateFormat("dd-M-yyyy hh:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("GMT-10")
        try {
            val time = sdf.parse(dataPost!!)!!.time
            val now = System.currentTimeMillis()
            val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
            return ago.toString();
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null;
    }

}