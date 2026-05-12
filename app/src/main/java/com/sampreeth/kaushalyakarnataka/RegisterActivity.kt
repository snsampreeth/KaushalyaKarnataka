package com.sampreeth.kaushalyakarnataka

import android.content.Intent
import android.graphics.Color
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedRole = "worker"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnRoleWorker = findViewById<Button>(R.id.btnRoleWorker)
        val btnRoleCustomer = findViewById<Button>(R.id.btnRoleCustomer)

        btnRoleWorker.setOnClickListener {
            selectedRole = "worker"
            btnRoleWorker.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1D9E75"))
            btnRoleWorker.setTextColor(Color.WHITE)
            btnRoleCustomer.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            btnRoleCustomer.setTextColor(Color.parseColor("#1D9E75"))
        }

        btnRoleCustomer.setOnClickListener {
            selectedRole = "customer"
            btnRoleCustomer.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1D9E75"))
            btnRoleCustomer.setTextColor(Color.WHITE)
            btnRoleWorker.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            btnRoleWorker.setTextColor(Color.parseColor("#1D9E75"))
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name is required"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (name.length < 3) {
                etName.error = "Name must be at least 3 characters"
                etName.requestFocus()
                return@setOnClickListener
            }

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

            btnRegister.isEnabled = false
            btnRegister.text = "Creating account..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: return@addOnSuccessListener
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "role" to selectedRole,
                        "category" to "",
                        "area" to "",
                        "bio" to "",
                        "rating" to 0.0
                    )
                    db.collection("workers").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            navigateByRole()
                        }
                }
                .addOnFailureListener {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Create Account"
                    when {
                        it.message?.contains("email address is already") == true ->
                            etEmail.error = "This email is already registered"
                        else ->
                            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun navigateByRole() {
        if (selectedRole == "worker") {
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            startActivity(Intent(this, CustomerHomeActivity::class.java))
        }
        finish()
    }
}