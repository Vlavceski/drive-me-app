package com.example.chatme

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatme.Utils.Posts
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
import kotlinx.android.synthetic.main.dialog_post.datePicker_text
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

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    DatePickerDialog.OnDateSetListener {
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
    private var datePost_: String = ""
    private lateinit var dialogView: View

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
            dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_post, null)

            val textDate = dialogView.findViewById<TextView>(R.id.datePicker_text)
            textDate.text = "-/-/-";


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

                    val Messages = arrayOf(
                        "Successful. Wait approval from admin to show post.",
                    )

                    if (postFrom == postTo) {
                        Toast.makeText(
                            this@MainActivity,
                            "Destinations is same!",
                            Toast.LENGTH_LONG
                        ).show()

                    } else if (postFrom.isEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed. Input FROM is empty",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (postTo.isEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed. Input TO is empty",
                            Toast.LENGTH_LONG
                        ).show()

                    } else if (::datePost_.equals("")) {
                        Toast.makeText(this@MainActivity, "Please choose date!!", Toast.LENGTH_LONG)
                            .show()

                    } else {
                        progressBar.setCancelable(false)
                        progressBar.setMessage("Adding Post")
                        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER)

                        progressBar.show()

                        // Add a fun message before the post is added
                        val randomMessage = Messages.random()
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
                                uid_user = mUser.uid,
                                bookmarked = false,
                                allowed = false
                            )

                            mPostRef.child(mUser.uid + formattedDate).setValue(post)
                                .addOnSuccessListener {
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
                val splitDate =
                    formattedDate.split("-")  // Split the formattedDate string into parts
                val day = splitDate[0].toInt()
                val month = splitDate[1].toInt()
                val year = splitDate[2].toInt()
                DatePickerDialog(this, this, year, month, day).show()

            }
        }
    }


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        datePost_ =
            dayOfMonth.toString().plus("-").plus(month.toString()).plus("-").plus(year.toString())
        val textDate = dialogView.findViewById<TextView>(R.id.datePicker_text)

        textDate.text = datePost_
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

                if (model.allowed == true) {
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
//                val reference = mPostRef.child(postKey!!)
                    var value: Any?
                    val reference = mBookMarkRef.child(mUser.uid).child(postKey!!)



                    reference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            value =
                                dataSnapshot.child("bookmarked").getValue(Boolean::class.java)
                                    ?: false

                            if (value == true) {
                                holder.customView.bookmark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@MainActivity,
                                        R.drawable.baseline_bookmark_added_24
                                    )
                                )
                            } else {
                                holder.customView.bookmark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@MainActivity,
                                        R.drawable.baseline_bookmark_border_24
                                    )
                                )
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                    holder.customView.profileImagePost.setOnClickListener {
                        if (mUser.uid.equals(model.uid_user)) {
                            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@MainActivity, ViewFriendActivity::class.java)
                            intent.putExtra("userKey", model.uid_user.toString())
                            startActivity(intent)
                        }
                    }
                    holder.customView.profileUsernamePost.setOnClickListener {
                        if (mUser.uid.equals(model.uid_user)) {
                            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@MainActivity, ViewFriendActivity::class.java)
                            intent.putExtra("userKey", model.uid_user.toString())
                            startActivity(intent)
                        }
                    }
                    holder.customView.bookmark.setOnClickListener {
//                    val reference1 = mPostRef.child(postKey!!).child("bookmarked")
                        val reference1 =
                            mBookMarkRef.child(mUser.uid).child(postKey).child("bookmarked")
                        var value1: Boolean
                        reference1.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                value1 = dataSnapshot.getValue(Boolean::class.java) ?: false
                                if (value1) {
                                    mBookMarkRef.child(mUser.uid).child(postKey).removeValue()
//                                reference1.setValue(false)
                                    holder.customView.bookmark.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@MainActivity,
                                            R.drawable.baseline_bookmark_added_24
                                        )
                                    )
                                    notifyDataSetChanged();

                                }
                                if (!value1) {
                                    val post = Post(
                                        username = username_profile,
                                        userProfileImageUrl = image_username_profile,
                                        dataPost = model.dataPost.toString(),
                                        postFrom = model.postFrom.toString(),
                                        postTo = model.postTo.toString(),
                                        postDate = model.postDate.toString(),
                                        postDesc = model.postDesc.toString(),
                                        uid_user = mUser.uid,
                                        bookmarked = true,
                                        allowed = true
                                    )
                                    mBookMarkRef.child(mUser.uid).child(postKey).setValue(post)
//                                reference1.setValue(true)
                                    holder.customView.bookmark.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@MainActivity,
                                            R.drawable.baseline_bookmark_border_24
                                        )
                                    )
                                    notifyDataSetChanged();
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // An error occurred while retrieving the data
                                // Handle the error here
                            }
                        })
                    }

                } else {
                    holder.itemView.visibility = View.GONE
                    val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
                    params.height = 0
                    params.width = 0
                    holder.itemView.layoutParams = params
                }

            }
        }
        adapter.notifyDataSetChanged()

        recyclerview.adapter = adapter
        adapter.startListening()
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

            R.id.requests -> {
                val intent = Intent(this@MainActivity, RequestsActivity::class.java)
                startActivity(intent)
            }

            R.id.bookmark -> {
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