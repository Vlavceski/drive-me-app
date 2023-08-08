package com.example.chatme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.btn_alreadyHaveAccount
import kotlinx.android.synthetic.main.activity_login.btn_login
import kotlinx.android.synthetic.main.activity_login.btn_signup
import kotlinx.android.synthetic.main.activity_login.inputEmail_l
import kotlinx.android.synthetic.main.activity_login.inputPassword_l

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth=FirebaseAuth.getInstance();

        btn_login.setOnClickListener {
            login()

        }
        btn_signup.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

        btn_alreadyHaveAccount.setOnClickListener {
            startActivity(Intent(this@LoginActivity,ForgotPasswordActivity::class.java))
        }

    }

    private fun login() {
        val email= inputEmail_l.text.toString();
        val password= inputPassword_l.text.toString();
        if(email.isEmpty()){
            inputEmail_l.error = "Invalid";
        }
        if(password.isEmpty()){
            inputPassword_l.error = "Invalid";
        }
        else{
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                if (it.isSuccessful){
                    val intent= Intent(this@LoginActivity,SetupActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(this,"Failed", Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}