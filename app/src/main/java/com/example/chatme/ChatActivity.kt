package com.example.chatme

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.chatme.Utils.Chat
import com.example.chatme.holder.ChatHolder
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
import kotlinx.android.synthetic.main.activity_chat.recyclerview_chat
import kotlinx.android.synthetic.main.activity_chat.send_message
import kotlinx.android.synthetic.main.activity_chat.text_chat
import kotlinx.android.synthetic.main.chat_appbar.active
import kotlinx.android.synthetic.main.chat_appbar.btn_back_chat_users
import kotlinx.android.synthetic.main.chat_appbar.circleImageView4_chat
import kotlinx.android.synthetic.main.chat_appbar.username_chat
import kotlinx.android.synthetic.main.main_appbar.app_bar
import kotlinx.android.synthetic.main.singleview_sms.view.firstUserProfile
import kotlinx.android.synthetic.main.singleview_sms.view.firstUserText
import kotlinx.android.synthetic.main.singleview_sms.view.secondUserProfile
import kotlinx.android.synthetic.main.singleview_sms.view.secondUserText
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mRef: DatabaseReference;
    private lateinit var mSmsRef: DatabaseReference;
    private lateinit var OtherUsername:String;
    private lateinit var OtherProfileImage:String;
    private lateinit var OtherStatus:String;
    private lateinit var OtherUserId:Any;
    lateinit var ProfilePicture:String;
    var URL="https://fcm.googleapis.com/fcm/send";
    lateinit var requestQueue: RequestQueue;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(app_bar as Toolbar?)
        mAuth= FirebaseAuth.getInstance();
        mUser= mAuth.currentUser!!;
        mRef= FirebaseDatabase.getInstance().reference.child("Users");

        mSmsRef= FirebaseDatabase.getInstance().reference.child("Sms");
        recyclerview_chat.layoutManager= LinearLayoutManager(this@ChatActivity);

        requestQueue= Volley.newRequestQueue(this);

        OtherUserId= intent.getStringExtra("OtherUserID")!!;
        LoadOtherUser(OtherUserId as String);

        send_message.setOnClickListener {
            sendSMS()
        }
        loadSMS()
        loadMyProfile()
        btn_back_chat_users.setOnClickListener {
            val intent= Intent(this@ChatActivity,ChatUsersActivity::class.java);
            startActivity(intent)
        }
    }

    private fun loadMyProfile() {
        mRef.child(mUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    ProfilePicture=snapshot.child("profileImage").value.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadSMS() {
        val query = mSmsRef.child(mUser.uid).child(OtherUserId as String)
        val options = FirebaseRecyclerOptions.Builder<Chat>()
            .setQuery(query, Chat::class.java)
            .setLifecycleOwner(this)
            .build()

        recyclerview_chat.layoutManager = LinearLayoutManager(this)
        val adapter = object : FirebaseRecyclerAdapter<Chat, ChatHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
                return ChatHolder(
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.singleview_sms, parent, false)
                )

            }

            override fun onBindViewHolder(holder: ChatHolder, position: Int, model: Chat) {

                if (model.userID==mUser.uid) {

                    holder.customView.firstUserText.visibility = View.GONE
                    holder.customView.firstUserProfile.visibility = View.GONE
                    holder.customView.secondUserText.visibility = View.VISIBLE
                    holder.customView.secondUserProfile.visibility = View.VISIBLE

                    holder.customView.secondUserText.text = model.sms
                    Picasso.get().load(ProfilePicture).into(holder.customView.secondUserProfile)


                } else {
                    holder.customView.firstUserText.visibility = View.VISIBLE
                    holder.customView.firstUserProfile.visibility = View.VISIBLE
                    holder.customView.secondUserText.visibility = View.GONE
                    holder.customView.secondUserProfile.visibility = View.GONE

                    holder.customView.firstUserText.text = model.sms
                    Picasso.get().load(OtherProfileImage).into(holder.customView.firstUserProfile)
                }

            }

        }
        adapter.notifyDataSetChanged()
        adapter.startListening()
        recyclerview_chat.adapter=adapter;
    }

    private fun sendSMS() {
        var sms=text_chat.text.toString()
        if (sms.isEmpty()){
            text_chat.error="";
        }
        else{
            val hashMap = HashMap<String, Any>()
            hashMap["sms"] = sms;
            hashMap["status"] = "unseen";
            hashMap["userID"] = mUser.uid;
            mSmsRef.child(OtherUserId as String).child(mUser.uid).push().updateChildren(hashMap).addOnCompleteListener {
                if (it.isSuccessful){
                    mSmsRef.child(mUser.uid).child(OtherUserId as String).push().updateChildren(hashMap).addOnCompleteListener {
                        if(it.isSuccessful){
                            text_chat.setText(null);
                            sendNotification(sms);
                        }
                    }
                }
            }
        }
    }

    private fun sendNotification(sms: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("to", "/topics/$OtherUserId")

            val jsonObject1 = JSONObject()
            jsonObject1.put("title", "Message from $OtherUsername")
            jsonObject1.put("body", sms)

            jsonObject.put("notification", jsonObject1)

            val request = object : JsonObjectRequest(
                Request.Method.POST,
                URL,
                jsonObject,
                Response.Listener { response ->
                    // Handle the response
                },
                null
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    //Cloud Messaging key
                    headers["Authorization"] = "key=BO7lyn8j0PDI99tf7pJGM8Shvn3W1KMDbsTiuB_E1hU2gHBViSZbUcPX-19Vcxa7JBIfU3quvBMgN2hpuKYsxiw"
                    return headers
                }
            }

            requestQueue.add(request)
        }

        catch (e: Exception) {
            // Handle the exception appropriately
            e.printStackTrace()
        }
    }

    private fun LoadOtherUser(OtherUserID: String?) {
        mRef.child(OtherUserID!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    OtherUsername=snapshot.child("username").value.toString()
                    OtherProfileImage=snapshot.child("profileImage").value.toString()
                    OtherStatus=snapshot.child("status").value.toString()

                    Picasso.get().load(OtherProfileImage).into(circleImageView4_chat);
                    username_chat.setText(OtherUsername);
                    active.setText(OtherStatus);

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}