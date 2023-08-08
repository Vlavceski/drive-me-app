package com.example.chatme

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log.d
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatme.Utils.Posts
import com.example.chatme.adapter.AdapterBookmark
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
import kotlinx.android.synthetic.main.activity_bookmark.btn_back_home_bookmark
import kotlinx.android.synthetic.main.activity_bookmark.rv_bookmark
import kotlinx.android.synthetic.main.activity_main.recyclerview
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.TimeZone

class BookmarkActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mBookmarkRef: DatabaseReference;
    private lateinit var mPostRef: DatabaseReference;
    private lateinit var adapter: AdapterBookmark
//    private lateinit var  firstChild :Any
//    private lateinit var childKey:String
//    private lateinit var childValue :Any
private lateinit var childValue :Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        btn_back_home_bookmark.setOnClickListener {
            val intent = Intent(this@BookmarkActivity, MainActivity::class.java)
            startActivity(intent)
        }
        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.currentUser!!;
        mBookmarkRef= FirebaseDatabase.getInstance().reference.child("BookMark");

        rv_bookmark.layoutManager = LinearLayoutManager(this@BookmarkActivity)
        loadBookmarks()

    }

    private fun loadBookmarks() {

    }




}