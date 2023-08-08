package com.example.chatme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.chatme.data.Friends
import com.example.chatme.data.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_friend.btnDecline
import kotlinx.android.synthetic.main.activity_view_friend.btnPerform
import kotlinx.android.synthetic.main.activity_view_friend.btn_back_friend
import kotlinx.android.synthetic.main.activity_view_friend.circleImageView3
import kotlinx.android.synthetic.main.activity_view_friend.city_friend
import kotlinx.android.synthetic.main.activity_view_friend.textView3

class ViewFriendActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mUserRef: DatabaseReference;
    private lateinit var mUserMyRef: DatabaseReference;
    private lateinit var mRequestRef: DatabaseReference;
    private lateinit var mFriendRef: DatabaseReference;
    lateinit var profileImageUrll:String
    lateinit var usernamee:String
    lateinit var cityy:String
    private lateinit var username_profile: String
    lateinit var image_username_profile: String
    var currentState="nothing_happen"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_friend)
        val userId=intent.getStringExtra("userKey");

        mUserMyRef = FirebaseDatabase.getInstance().reference.child("Users");
        mUserRef= FirebaseDatabase.getInstance().reference.child("Users").child(userId!!);
        mRequestRef= FirebaseDatabase.getInstance().reference.child("Requests")
        mFriendRef= FirebaseDatabase.getInstance().reference.child("Friends")
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.currentUser!!;
        btn_back_friend.setOnClickListener {
            val intent= Intent(this@ViewFriendActivity,FindFriendActivity::class.java)
            startActivity(intent)
        }
        currentState="nothing_happen"
        mUserMyRef.child(mUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    image_username_profile = snapshot.child("profileImage").value.toString()
                    username_profile = snapshot.child("username").value.toString();
                    Log.d("Current-username", username_profile)
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })


        loadUser()

        CheckUserExistance(userId)
        btnPerform.setOnClickListener {
            PeroformAction(userId)
        }
        btnDecline.setOnClickListener {
            Unfriend(userId)
        }
        Log.d("current---?", currentState)

    }

    private fun Unfriend(userId: String) {
        if(currentState.equals("friend")){
            mFriendRef.child(mUser.uid).child(userId).removeValue().addOnCompleteListener {
                if (it.isSuccessful){
                    mFriendRef.child(userId).child(mUser.uid).removeValue().addOnCompleteListener { it2->
                        if(it2.isSuccessful){
                            Toast.makeText(this@ViewFriendActivity,"You are unfriend", Toast.LENGTH_LONG).show();
                            currentState="nothing_happen"
                            btnPerform.text="Send friend request"
                            btnDecline.visibility= View.GONE
                        }
                    }
                }
            }
        }
        if(currentState.equals("he_sent_pending")){
            var request= Request(
                status = "decline"
            )

            mFriendRef.child(userId).child(mUser.uid).setValue(request).addOnCompleteListener {
                if (it.isSuccessful){
                    currentState="he_sent_decline"
                    btnPerform.visibility= View.GONE
                    btnDecline.visibility= View.GONE
                }
            }


        }
    }


    private fun CheckUserExistance(userId: String) {
        mFriendRef.child(mUser.uid).child(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    currentState="friend"
                    btnPerform.setText("Send SMS");
                    btnDecline.text="Unfriend"
                    btnDecline.visibility= View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        mFriendRef.child(userId).child(mUser.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    currentState="friend"
                    btnPerform.setText("Send SMS");
                    btnDecline.text="Unfriend"
                    btnDecline.visibility= View.VISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

        mRequestRef.child(mUser.uid).child(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if (snapshot.child("status").value.toString().equals("pending")){
                        currentState="I_send_pending"
                        btnPerform.setText("Cancel friend Request");
                        btnDecline.visibility= View.GONE;
                    }
                    if (snapshot.child("status").value.toString().equals("decline")){
                        currentState="I_send_decline"
                        btnPerform.setText("Cancel friend Request");
                        btnDecline.visibility= View.GONE;

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        mRequestRef.child(userId).child(mUser.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if (snapshot.child("status").value.toString().equals("pending")){
                        currentState="he_send_pending"
                        btnPerform.setText("Accept Friend");
                        btnDecline.setText("Decline Friend")
                        btnDecline.visibility= View.VISIBLE;
                    }
                }
            }


            override fun onCancelled(error: DatabaseError) {
            }

        })
        if(currentState.equals("nothing_happen")){
            currentState="nothing_happen";
            btnPerform.setText("Send friend request")
            btnDecline.visibility= View.GONE
        }
    }

    private fun PeroformAction(userId: String) {

        if (currentState.equals("nothing_happen")){
            val request = Request(
                status = "pending")
            mRequestRef.child(mUser.uid).child(userId).setValue(request).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@ViewFriendActivity,"You have send Friend Request", Toast.LENGTH_LONG).show()
                    btnDecline.visibility = View.GONE
                    currentState="I_sent_pending";
                    btnPerform.text = "Cancel Friend Request"
                }
                else
                {
                    Toast.makeText(this@ViewFriendActivity,"Error: "+it.exception, Toast.LENGTH_LONG).show()

                }
            }
        }
        if(currentState == "I_sent_pending" || currentState == "I_sent_decline"){
            mRequestRef.child(mUser.uid).child(userId).removeValue().addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this@ViewFriendActivity,"You have cancelled Friend Request",
                        Toast.LENGTH_LONG).show()
                    currentState="nothing_happen";
                    btnPerform.setText("Send Friend Request")
                    btnDecline.visibility = View.GONE

                }
                else
                {
                    Toast.makeText(this@ViewFriendActivity,"Error: "+it.exception, Toast.LENGTH_LONG).show()

                }
            }
        }
        if(currentState == "he_send_pending") {
            mRequestRef.child(userId).child(mUser.uid).removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    val friends = Friends(
                        status = "friend",
                        username = usernamee,
                        profileImageUrl = profileImageUrll
                    )

                    val myFriendRequest = Friends(
                        status = "friend",
                        username = username_profile,
                        profileImageUrl = image_username_profile
                    )

                    mFriendRef.child(mUser.uid).child(userId).setValue(friends)
                        .addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                mFriendRef.child(userId).child(mUser.uid).setValue(myFriendRequest)
                                    .addOnCompleteListener {
                                        Toast.makeText(
                                            this@ViewFriendActivity,
                                            "You Added Friend",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        currentState = "friend";
                                        btnPerform.text = "Send SMS";
                                        btnDecline.text = "Unfriend";
                                        btnDecline.visibility = View.VISIBLE

                                    }
                            }
                        }
                }
            }
        }
        if (currentState == "friend") {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("OtherUserID", userId)
            startActivity(intent);
        }
        Log.d("status", currentState)
    }


    private fun loadUser() {

        mUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    profileImageUrll=snapshot.child("profileImage").value.toString();
                    usernamee=snapshot.child("username").value.toString();
                    cityy=snapshot.child("city").value.toString();
                    Picasso.get().load(profileImageUrll).into(circleImageView3)
                    textView3.text=usernamee
                    city_friend.text=cityy

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}