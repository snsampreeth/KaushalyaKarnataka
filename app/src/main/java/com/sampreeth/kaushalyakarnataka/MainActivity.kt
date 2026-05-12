package com.sampreeth.kaushalyakarnataka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            checkRoleAndNavigate(auth.currentUser!!.uid)
            return
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter a valid email address"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Logging in..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    checkRoleAndNavigate(auth.currentUser!!.uid)
                }
                .addOnFailureListener {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                    when {
                        it.message?.contains("no user record") == true ->
                            etEmail.error = "No account found with this email"
                        it.message?.contains("password is invalid") == true ->
                            etPassword.error = "Wrong password"
                        else ->
                            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkRoleAndNavigate(userId: String) {
        db.collection("workers").document(userId).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "worker"
                if (role == "customer") {
                    startActivity(Intent(this, CustomerHomeActivity::class.java))
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                finish()
            }
    }
}