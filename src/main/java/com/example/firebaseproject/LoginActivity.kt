package com.example.firebaseproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private val editUserEmail by lazy { findViewById<EditText>(R.id.editTextSigninEmail) }
    private val editUserPassword by lazy { findViewById<EditText>(R.id.editTextSigninPW) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        findViewById<Button>(R.id.buttonSignin)?.setOnClickListener {
            doLogin()
        }

        // 회원가입 시작 버튼
        findViewById<Button>(R.id.buttonStartSignup)?.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun doLogin() {
        val email = editUserEmail.text.toString()
        if (email.isEmpty()) {
            Snackbar.make(editUserEmail, "아이디를 입력하세요!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val password = editUserPassword.text.toString()
        if (password.isEmpty()) {
            Snackbar.make(editUserPassword, "비밀번호를 입력하세요!", Snackbar.LENGTH_SHORT).show()
            return
        }

        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { // it: Task<AuthResult!>
                if (it.isSuccessful) {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                    )
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}