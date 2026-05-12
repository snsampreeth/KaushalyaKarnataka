package com.sampreeth.kaushalyakarnataka

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val notificationList = mutableListOf<HireNotification>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val rvNotifications = findViewById<RecyclerView>(R.id.rvNotifications)

        adapter = NotificationAdapter(notificationList, userId)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter

        loadNotifications(userId)
    }

    private fun loadNotifications(userId: String) {
        db.collection("workers").document(userId)
            .collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                notificationList.clear()
                snapshot?.documents?.forEach { doc ->
                    notificationList.add(
                        HireNotification(
                            id = doc.id,
                            customerEmail = doc.getString("customerEmail") ?: "",
                            customerId = doc.getString("customerId") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
    }
}