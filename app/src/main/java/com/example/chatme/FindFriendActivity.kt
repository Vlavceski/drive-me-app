package com.example.chatme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatme.data.Users
import com.example.chatme.holder.UsersHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_find_friend.app_bar
import kotlinx.android.synthetic.main.activity_find_friend.btn_back_home
import kotlinx.android.synthetic.main.activity_find_friend.recyclerView_find_friend
import kotlinx.android.synthetic.main.single_view_find_friend.view.circleImageView2
import kotlinx.android.synthetic.main.single_view_find_friend.view.city_find_friend
import kotlinx.android.synthetic.main.single_view_find_friend.view.username_find_friend

class FindFriendActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mUserRef: DatabaseReference;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friend)
        setSupportActionBar(app_bar as Toolbar?)
        supportActionBar!!.title = "Find Friends";

        mUserRef= FirebaseDatabase.getInstance().reference.child("Users");
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.currentUser!!;
        recyclerView_find_friend.layoutManager = LinearLayoutManager(this@FindFriendActivity)
        btn_back_home.setOnClickListener {
            val intent= Intent(this@FindFriendActivity,MainActivity::class.java)
            startActivity(intent)
        }

        loadPeople("")
    }

    private fun loadPeople(s: String) {
        val query = mUserRef.orderByChild("username").startAt(s)
        val options = FirebaseRecyclerOptions.Builder<Users>()
            .setQuery(query, Users::class.java)
            .setLifecycleOwner(this)
            .build()
        recyclerView_find_friend.layoutManager = LinearLayoutManager(this@FindFriendActivity)

        val adapter = object : FirebaseRecyclerAdapter<Users, UsersHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersHolder {
                val view = LayoutInflater
                    .from(parent.context).
                    inflate(R.layout.single_view_find_friend, parent, false)
                return UsersHolder(view)
            }

            override fun onBindViewHolder(holder: UsersHolder, position: Int, model: Users) {
                if(mUser.uid != getRef(position).key.toString()){
                    holder.itemView.username_find_friend.text = model.username
                    holder.itemView.city_find_friend.text = model.city
                    // Load the profile image using Glide library
                    Glide.with(holder.itemView.context)
                        .load(model.profileImage)
                        .into(holder.itemView.circleImageView2)
                }
                else {
                    holder.itemView.visibility = View.GONE
                    holder.itemView.layoutParams = ViewGroup.LayoutParams(0, 0)
                }
                holder.itemView.setOnClickListener {
                    d("getReg find Friend",getRef(position).key.toString())
                    val intent= Intent(this@FindFriendActivity,ViewFriendActivity::class.java)
                    intent.putExtra("userKey",getRef(position).key.toString())
                    startActivity(intent)

                }

            }
        }
        adapter.startListening()
        recyclerView_find_friend.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu);

        val searchItem = menu!!.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Do something when the user submits the search query
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Do something when the user types into the search box
                loadPeople(newText)
                return false
            }
        })

        return true;
    }
}