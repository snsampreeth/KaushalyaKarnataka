package com.sampreeth.kaushalyakarnataka

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                FirebaseFirestore.getInstance()
                    .collection("workers")
                    .document(auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val role = doc.getString("role") ?: "worker"
                        if (role == "customer") {
                            startActivity(Intent(this, CustomerHomeActivity::class.java))
                        } else {
                            startActivity(Intent(this, HomeActivity::class.java))
                        }
                        finish()
                    }
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 2000)
    }
}