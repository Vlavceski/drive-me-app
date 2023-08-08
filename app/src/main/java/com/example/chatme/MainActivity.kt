package com.example.chatme

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatme.Utils.Posts
import com.example.chatme.data.Bookmark
import com.example.chatme.data.Post
import com.example.chatme.holder.PostsHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.dialog
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navView
import kotlinx.android.synthetic.main.activity_main.recyclerview
import kotlinx.android.synthetic.main.activity_setup.app_bar
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
import java.util.Date
import java.util.TimeZone

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,DatePickerDialog.OnDateSetListener {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var mUser: FirebaseUser;
    private lateinit var mUserRef: DatabaseReference;
    private lateinit var mPostRef: DatabaseReference;
    private lateinit var mLikeRef: DatabaseReference;
    private lateinit var mBookMarkRef: DatabaseReference;
    private lateinit var profileImageHeader: CircleImageView
    private lateinit var usernameHeader: TextView
    private lateinit var postImageRef: StorageReference;
    private lateinit var username_profile: String
    private lateinit var image_username_profile: String
    private lateinit var datePost_: String


    private lateinit var imageUri: Uri
    private val PICK_IMAGE_REQUEST = 1

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(app_bar as Toolbar?)
        supportActionBar?.title = "DriveMe"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.currentUser!!;
        mUserRef = FirebaseDatabase.getInstance().reference.child("Users");
        mPostRef = FirebaseDatabase.getInstance().reference.child("Posts");
        mLikeRef = FirebaseDatabase.getInstance().reference.child("Like");
        mBookMarkRef = FirebaseDatabase.getInstance().reference.child("BookMark");

        FirebaseMessaging.getInstance().subscribeToTopic(mAuth.uid!!);

        postImageRef = FirebaseStorage.getInstance().reference.child("PostImages");

        val view = navView.inflateHeaderView(R.layout.drawer_header)
        usernameHeader = view.findViewById(R.id.username_header)
        profileImageHeader = view.findViewById(R.id.profileImage_)
        recyclerview.layoutManager = LinearLayoutManager(this@MainActivity)

        navView.setNavigationItemSelectedListener(this)

        mUserRef.child(mUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    image_username_profile = snapshot.child("profileImage").value.toString()
                    username_profile = snapshot.child("username").value.toString();
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        LoadPost();
        showDialog();
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun showDialog() {
        dialog.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_post, null)

            val textDate = dialogView.findViewById<TextView>(R.id.datePicker_text)

            builder.setView(dialogView)
                .setTitle("AddPost")
                .setPositiveButton("OK") { _, _ ->

                    val inputFrom_ = dialogView.findViewById<TextView>(R.id.inputFrom)
                    val inputTo_ = dialogView.findViewById<TextView>(R.id.inputTo)
                    val inputDesc_ = dialogView.findViewById<TextView>(R.id.inputDesc)

                    val postDesc = inputDesc_.text.toString()
                    val postFrom = inputFrom_.text.toString()
                    val postTo = inputTo_.text.toString()
                    val progressBar = ProgressDialog(this@MainActivity)


                    val inputFrom_dialog = dialogView.findViewById<TextView>(R.id.inputFrom)
                    val inputTo_dialog = dialogView.findViewById<TextView>(R.id.inputTo)
                    val inputDesc_dialog = dialogView.findViewById<TextView>(R.id.inputDesc)


                    val funMessages = arrayOf(
                        "Hang on, let me get my popcorn!",
                        "This is going to be good!",
                        "Let's see what you've got!",
                        "Time to entertain us!",
                        "Bring on the fun!"
                    )

                    if (postFrom == postTo) {
                        inputFrom_dialog.error = "Please write different position!"
                        inputTo_dialog.error = "Please write different position!"
                    }
                    if (postFrom.isEmpty()) {
                        inputFrom_dialog.error = "Please write something!"
                    }
                    if (postTo.isEmpty()) {
                        inputTo_dialog.error = "Please write something!"
                    }
                    if (::datePost_.isInitialized) {
                        textDate.error = "Please choose date!!"
                    }
                    if (postDesc.isEmpty()) {
                        inputDesc_dialog.error = "Please write something!"
                    } else {
                        progressBar.setCancelable(false)
                        progressBar.setMessage("Adding Post")
                        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER)

                        progressBar.show()

                        // Add a fun message before the post is added
                        val randomMessage = funMessages.random()
                        Toast.makeText(this@MainActivity, randomMessage, Toast.LENGTH_SHORT).show()

                        val currentDate = Date()
                        val dateFormat = SimpleDateFormat("dd-M-yyyy hh:mm:ss")
                        val formattedDate = dateFormat.format(currentDate)


                        postImageRef.child(mUser.uid + formattedDate).downloadUrl.addOnCompleteListener {

                            val post = Post(
                                username = username_profile,
                                userProfileImageUrl = image_username_profile,
                                dataPost = formattedDate,
                                postFrom = postFrom,
                                postTo = postTo,
                                postDate = datePost_,
                                postDesc = postDesc,
                                uid_user=mUser.uid
                            )

                            mPostRef.child(mUser.uid + formattedDate).setValue(post).addOnSuccessListener {
                                progressBar.dismiss()

                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)

            val dialog = builder.create()
            dialog.show()
            val button = dialogView.findViewById<Button>(R.id.datepicker)
            button.setOnClickListener {
                val currentDate = Date()
                val dateFormat = SimpleDateFormat("dd-M-yyyy")
                val formattedDate = dateFormat.format(currentDate)
                val splitDate = formattedDate.split("-")  // Split the formattedDate string into parts
                val day = splitDate[0].toInt()
                val month=splitDate[1].toInt()
                val year = splitDate[2].toInt()
                DatePickerDialog(this,this,year,month,day).show()
//                textDate.setText(datePost_)
            }

        }
    }


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        datePost_=dayOfMonth.toString().plus("-").plus(month.toString()).plus("-").plus(year.toString())

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun LoadPost() {
        val currentDate = System.currentTimeMillis()
        val query = mPostRef.orderByChild("postDate").startAt(currentDate.toDouble())

        val options = FirebaseRecyclerOptions.Builder<Posts>()
            .setQuery(query, Posts::class.java)
            .setLifecycleOwner(this)
            .build()

        recyclerview.layoutManager = LinearLayoutManager(this)

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
                mBookMarkRef.child(mUser.uid).child(postKey!!).addListenerForSingleValueEvent(object :ValueEventListener{

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            holder.customView.bookmark.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.baseline_bookmark_border_24))
                            notifyDataSetChanged();
                        } else {
                          holder.customView.bookmark.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.baseline_bookmark_added_24))
                            notifyDataSetChanged();
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        d("Error -- > bookMark",error.toString())
                    }

                })
                holder.customView.bookmark.setOnClickListener {
                    mBookMarkRef.child(mUser.uid).child(postKey!!).addListenerForSingleValueEvent(object :ValueEventListener{

                        @SuppressLint("NotifyDataSetChanged")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            d("data snapshot", snapshot.toString())
                            if (snapshot.exists()) {
                                mBookMarkRef.child(mUser.uid).child(postKey).removeValue();
                                holder.customView.bookmark.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.baseline_bookmark_border_24))
                                notifyDataSetChanged();
                            } else {
                                val bookmark_=Bookmark(
                                    status = "bookmarked",
                                    bookmark_key=postKey,
                                    uid_user=mUser.uid
                                )
                                mBookMarkRef.child(mUser.uid).child(postKey).setValue(bookmark_);
                                holder.customView.bookmark.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.baseline_bookmark_added_24))
                                notifyDataSetChanged();
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                           d("Error -- > bookMark",error.toString())
                        }

                    })
                }



            }

        }
        adapter.notifyDataSetChanged()
        adapter.startListening()
        recyclerview.adapter = adapter
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


    override fun onStart() {
        super.onStart()
        if (mUser == null) {
            SendUserToLoginActivity()
        } else {
            mUserRef.child(mUser.uid).addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val profileImageUrlV = dataSnapshot.child("profileImage").value.toString();
                        val usernameV = dataSnapshot.child("username").value.toString();

                        usernameHeader.text = usernameV;
                        Picasso.get()
                            .load(profileImageUrlV)
                            .resize(150, 150)
                            .into(profileImageHeader)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Sorry! Something going wrong!!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {

            imageUri = data.data!!

//            addImagePost.setImageURI(imageUri);
        }
    }

    private fun SendUserToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(intent)
            }

            R.id.profile -> {
                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                startActivity(intent)
            }

            R.id.friend -> {
                val intent = Intent(this@MainActivity, FriendActivity::class.java)
                startActivity(intent)
            }

            R.id.chat -> {
                val intent = Intent(this@MainActivity, ChatUsersActivity::class.java)
                startActivity(intent)

            }

            R.id.findFriend -> {
                val intent = Intent(this@MainActivity, FindFriendActivity::class.java)
                startActivity(intent)
            }
            R.id.bookmark-> {
                val intent = Intent(this@MainActivity, BookmarkActivity::class.java)
                startActivity(intent)
            }
            R.id.logout -> {
                mAuth.signOut()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            else -> true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }

        return true
    }
}