package com.example.chatme

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.chatme.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_setup.app_bar
import kotlinx.android.synthetic.main.activity_setup.btnSave
import kotlinx.android.synthetic.main.activity_setup.inputCity
import kotlinx.android.synthetic.main.activity_setup.inputCountry
import kotlinx.android.synthetic.main.activity_setup.inputUsername
import kotlinx.android.synthetic.main.activity_setup.profile_image

class SetupActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mRef: DatabaseReference;
    private lateinit var StorageRef: StorageReference;
    private lateinit var  imageUri: Uri
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.currentUser!!;
        mRef= FirebaseDatabase.getInstance().reference.child("Users");
        StorageRef= FirebaseStorage.getInstance().reference.child("ProfileImages");

        setSupportActionBar(app_bar as Toolbar?)
        supportActionBar?.title = "Setup profile"

        profile_image.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)

        }
        btnSave.setOnClickListener {
            saveData()
        }
    }

    private fun saveData() {
        val username=inputUsername.text.toString();
        val city=inputCity.text.toString();
        val country=inputCountry.text.toString();
        if (username.isEmpty()){
            inputUsername.error="Invalid";
        }
        if (city.isEmpty()){
            inputCity.error="Invalid";
        }
        if (country.isEmpty()){
            inputCountry.error="Invalid";
        }
        else {

            StorageRef.child(mUser.uid).putFile(imageUri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    StorageRef.child(mUser.uid).downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Create user object with the user information and download URL of the image
                            val user = User(
                                username = username,
                                city = city,
                                country = country,
                                profileImage = task.result.toString(), // Save download URL of the image
                                status = "offline"
                            )
                            // Save user object to Firebase Realtime Database
                            mRef.child(mUser.uid).setValue(user).addOnSuccessListener {
                                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@SetupActivity,MainActivity::class.java))
                            }.addOnFailureListener {
                                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Failed to retrieve image download URL", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }



//            d("uri->>>>>", imageUri.toString())
//            StorageRef.child(mUser.uid).putFile(imageUri).addOnSuccessListener {
//                val downloadUrl = it.metadata?.reference?.downloadUrl.toString()
//                d("uri->>>>>", downloadUrl)
//
//                val user = User(
//                    username = username,
//                    city = city,
//                    country = country,
//                    profileImage = downloadUrl,
//                    status = "offline"
//                )
//
//                mRef.child(mUser.uid).push().setValue(user).addOnSuccessListener {
//                    Toast.makeText(this, "Complete", Toast.LENGTH_LONG).show()
//                }.addOnFailureListener {
//                    Toast.makeText(this, "Failure", Toast.LENGTH_LONG).show()
//                }
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//
//            }


//            StorageRef.child(mUser.uid).putFile(imageUri).addOnCompleteListener{ it ->
//                if (it.isSuccessful){
//                    StorageRef.child(mUser.uid).downloadUrl.addOnCompleteListener {task->
//
//                        val user = User(
//                            username =username,
//                            city =city,
//                            country =country,
//                            profileImage = imageUri.toString(),
//                            status = "offline"
//                        )
//
//                        mRef.child(mUser.uid).setValue(user).addOnSuccessListener {
//                            Toast.makeText(this,"Complete",Toast.LENGTH_LONG).show()
//                        }.addOnFailureListener {
//                            Toast.makeText(this,"Failure",Toast.LENGTH_LONG).show()
//
//                        }
//                        val intent = Intent(this, MainActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    }
//
//                }
//                else{
//                    Toast.makeText(this,"Failed", Toast.LENGTH_LONG).show()
//                }
//            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            Log.d("image-->", imageUri.toString())
            profile_image.setImageURI(imageUri);
        }
    }
}