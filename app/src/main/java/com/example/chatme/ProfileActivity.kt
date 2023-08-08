package com.example.chatme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.circleImageView
import kotlinx.android.synthetic.main.activity_profile.inputCity_profile_
import kotlinx.android.synthetic.main.activity_profile.inputCountry_profile
import kotlinx.android.synthetic.main.activity_profile.inputUsername_profile

class ProfileActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mUserRef: DatabaseReference;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.currentUser!!;
        mUserRef= FirebaseDatabase.getInstance().reference.child("Users");
        mUserRef.child(mUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val profileImageUrl=snapshot.child("profileImage").value.toString()
                    val city=snapshot.child("city").value.toString();
                    val country=snapshot.child("country").value.toString();
                    val username=snapshot.child("username").value.toString();

                    Picasso.get().load(profileImageUrl).into(circleImageView);
                    inputCity_profile_.setText(city);
                    inputCountry_profile.setText(country);
                    inputUsername_profile.setText(username);

                }
            }

            override fun onCancelled(error: DatabaseError) {


            }

        })

    }
}