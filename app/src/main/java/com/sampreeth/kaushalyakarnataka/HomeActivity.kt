package com.sampreeth.kaushalyakarnataka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Load current worker's own profile card
        db.collection("workers").document(currentUserId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val category = doc.getString("category") ?: "No category"
                    val area = doc.getString("area") ?: "No area"
                    val rating = doc.getDouble("rating") ?: 0.0

                    findViewById<TextView>(R.id.tvMyName).text = name
                    findViewById<TextView>(R.id.tvMyCategory).text = category
                    findViewById<TextView>(R.id.tvMyArea).text = area
                    findViewById<TextView>(R.id.tvMyRating).text =
                        if (rating > 0) "⭐ $rating" else "No rating"
                    findViewById<TextView>(R.id.tvMyInitials).text =
                        name.firstOrNull()?.toString() ?: "?"
                }
            }

        // Click on own profile card - Fixed ClassCastException by using View
        findViewById<View>(R.id.cvMyProfile).setOnClickListener {
            val intent = Intent(this, WorkerProfileActivity::class.java)
            intent.putExtra("workerId", currentUserId)
            startActivity(intent)
        }

        // Logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Edit Profile
        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Add Service
        findViewById<Button>(R.id.btnAddService).setOnClickListener {
            startActivity(Intent(this, AddServiceActivity::class.java))
        }

        // Bottom Navigation
        findViewById<BottomNavigationView>(R.id.bottomNavigation).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_ai -> {
                    startActivity(Intent(this, AIAssistantActivity::class.java))
                    true
                }
                R.id.nav_requests -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_portfolio -> {
                    startActivity(Intent(this, PortfolioActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
