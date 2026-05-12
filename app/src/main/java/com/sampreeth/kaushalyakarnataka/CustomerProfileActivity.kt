package com.sampreeth.kaushalyakarnataka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomerProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_profile)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return

        val tvInitials = findViewById<TextView>(R.id.tvCustomerInitials)
        val tvName = findViewById<TextView>(R.id.tvCustomerName)
        val tvEmail = findViewById<TextView>(R.id.tvCustomerEmail)
        val tvNameInfo = findViewById<TextView>(R.id.tvCustomerNameInfo)
        val tvEmailInfo = findViewById<TextView>(R.id.tvCustomerEmailInfo)
        val btnLogout = findViewById<Button>(R.id.btnCustomerLogout)

        db.collection("workers").document(userId).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""
                val email = doc.getString("email") ?: ""

                tvInitials.text = name.firstOrNull()?.toString() ?: "?"
                tvName.text = name
                tvEmail.text = email
                tvNameInfo.text = name
                tvEmailInfo.text = email
            }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}