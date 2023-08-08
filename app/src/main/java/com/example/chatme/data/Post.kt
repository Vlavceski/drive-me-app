package com.example.chatme.data

data class Post(
    val username:String,
    val userProfileImageUrl: String,
    val dataPost: String,
    val postFrom: String,
    val postTo: String,
    val postDate: String,
    val postDesc: String,
    val uid_user:String
)
