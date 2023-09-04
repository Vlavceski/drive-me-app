package com.example.chatme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatme.adapter.RequestAdapter
import com.example.chatme.data.RequestWithUsername
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_find_friend.app_bar
import kotlinx.android.synthetic.main.activity_requests.back2Home
import kotlinx.android.synthetic.main.activity_requests.emptyRequest
import kotlinx.android.synthetic.main.activity_requests.recyclerView_Request
import kotlinx.android.synthetic.main.activity_view_friend.btnDecline
import kotlinx.android.synthetic.main.activity_view_friend.btnPerform

class RequestsActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mRef: DatabaseReference;
    private lateinit var mUserRef: DatabaseReference;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_requests)
        setSupportActionBar(app_bar as Toolbar?)
        supportActionBar?.title = "Requests";
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.currentUser!!;
        mRef = FirebaseDatabase.getInstance().reference.child("Requests");
        mUserRef = FirebaseDatabase.getInstance().reference.child("Users")

        recyclerView_Request.layoutManager = LinearLayoutManager(this@RequestsActivity);
        val userId = mUser.uid;
        LoadRequests(userId)


        back2Home.setOnClickListener {
            val intent = Intent(this@RequestsActivity, MainActivity::class.java)
            startActivity(intent);
        }
    }
    private fun LoadRequests(userId: String?) {
        val requestList: MutableList<RequestWithUsername> = ArrayList()

        mRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val requestId = childSnapshot.key
                    if (requestId != null) {
                        mRef.child(requestId!!).child(mUser.uid).addValueEventListener(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists() && snapshot.child("status").value.toString() == "pending") {
                                    mUserRef.child(requestId).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(usernameSnapshot: DataSnapshot) {
                                            if (usernameSnapshot.exists()) {
                                                val username = usernameSnapshot.child("username").value.toString()
                                                val image = usernameSnapshot.child("profileImage").value.toString()
                                                val city = usernameSnapshot.child("city").value.toString()
                                                requestList.add(RequestWithUsername(requestId, username,city,image))
                                                // Notify the RecyclerView adapter that data has changed.
                                                updateRecyclerView(requestList)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }
                                    })
                                }
                                else{

                                    emptyRequest.text="No Requests";
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle onCancelled event if needed.
                            }
                        })
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun updateRecyclerView(requestList: List<RequestWithUsername>) {
        val adapter = RequestAdapter(requestList)
        recyclerView_Request.adapter = adapter
    }

}