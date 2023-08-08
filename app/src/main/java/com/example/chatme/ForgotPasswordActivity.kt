package com.example.chatme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.btn_reset_password
import kotlinx.android.synthetic.main.activity_forgot_password.inputPasswordReset

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        mAuth=FirebaseAuth.getInstance();

        btn_reset_password.setOnClickListener {
            val email=inputPasswordReset.text.toString();
            if (email.isEmpty()){
                Toast.makeText(this@ForgotPasswordActivity,"Please Enter you email", Toast.LENGTH_LONG).show()
            }
            else
            {
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(this@ForgotPasswordActivity,"Successful", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@ForgotPasswordActivity,LoginActivity::class.java))
                    }
                    else
                    {
                        Toast.makeText(this@ForgotPasswordActivity,"Something wrong!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}