package com.sampreeth.kaushalyakarnataka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val notifications: MutableList<HireNotification>,
    private val workerId: String
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCustomerEmail: TextView = view.findViewById(R.id.tvCustomerEmail)
        val tvMessage: TextView = view.findViewById(R.id.tvNotificationMessage)
        val tvTime: TextView = view.findViewById(R.id.tvNotificationTime)
        val btnMarkRead: Button = view.findViewById(R.id.btnMarkRead)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.tvCustomerEmail.text = "From: ${notification.customerEmail}"
        holder.tvMessage.text = notification.message

        val date = Date(notification.timestamp)
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.tvTime.text = format.format(date)

        if (notification.isRead) {
            holder.btnMarkRead.text = "✅ Read"
            holder.btnMarkRead.isEnabled = false
            holder.itemView.alpha = 0.6f
        } else {
            holder.btnMarkRead.text = "Mark as Read"
            holder.btnMarkRead.isEnabled = true
            holder.itemView.alpha = 1.0f
        }

        holder.btnMarkRead.setOnClickListener {
            db.collection("workers").document(workerId)
                .collection("notifications").document(notification.id)
                .update("isRead", true)
                .addOnSuccessListener {
                    val pos = notifications.indexOf(notification)
                    if (pos != -1) {
                        notifications[pos] = notification.copy(isRead = true)
                        notifyItemChanged(pos)
                    }
                    Toast.makeText(
                        holder.itemView.context,
                        "Marked as read!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun getItemCount() = notifications.size
}