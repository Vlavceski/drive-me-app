package com.example.chatme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.btn_alreadyHaveAccount
import kotlinx.android.synthetic.main.activity_register.btn_register
import kotlinx.android.synthetic.main.activity_register.inputEmail
import kotlinx.android.synthetic.main.activity_register.inputPassword

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth=FirebaseAuth.getInstance();

        btn_register.setOnClickListener {
            register()

        }
        btn_alreadyHaveAccount.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }

    private fun register() {
        val email= inputEmail.text.toString();
        val password= inputPassword.text.toString();
        if(email.isEmpty()){
            inputEmail.error = "Invalid";
        }
        if(password.isEmpty()){
            inputPassword.error = "Invalid";
        }
        else{

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                if (it.isSuccessful){
                    val intent= Intent(this@RegisterActivity,SetupActivity::class.java)
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