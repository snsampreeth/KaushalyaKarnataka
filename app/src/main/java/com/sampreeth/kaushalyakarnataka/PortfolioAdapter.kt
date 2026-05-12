package com.sampreeth.kaushalyakarnataka

import android.app.AlertDialog
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class PortfolioAdapter(
    private val items: MutableList<PortfolioItem>,
    private val workerId: String,
    private val isOwner: Boolean
) : RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class PortfolioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivPortfolioImage)
        val tvCaption: TextView = view.findViewById(R.id.tvPortfolioCaption)
        val btnDelete: Button = view.findViewById(R.id.btnDeletePortfolio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_portfolio, parent, false)
        return PortfolioViewHolder(view)
    }

    override fun onBindViewHolder(holder: PortfolioViewHolder, position: Int) {
        val item = items[position]
        holder.tvCaption.text = item.caption.ifEmpty { "No caption" }

        // Clear previous image
        holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)

        if (item.imageUrl.isNotEmpty()) {
            if (item.imageUrl.startsWith("http")) {
                // Load as URL (Firebase Storage)
                Glide.with(holder.itemView.context)
                    .load(item.imageUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivImage)
            } else {
                // Load as Base64 String (Workaround for free plan)
                try {
                    val imageBytes = Base64.decode(item.imageUrl, Base64.DEFAULT)
                    Glide.with(holder.itemView.context)
                        .asBitmap()
                        .load(imageBytes)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.ivImage)
                } catch (e: Exception) {
                    holder.ivImage.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            }
        }

        // Handle delete button visibility
        holder.btnDelete.visibility = if (isOwner) View.VISIBLE else View.GONE

        if (isOwner) {
            holder.btnDelete.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Delete Photo")
                    .setMessage("Are you sure you want to delete this photo?")
                    .setPositiveButton("Delete") { _, _ ->
                        db.collection("workers").document(workerId)
                            .collection("portfolio").document(item.id)
                            .delete()
                            .addOnSuccessListener {
                                val pos = holder.bindingAdapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    items.removeAt(pos)
                                    notifyItemRemoved(pos)
                                }
                                Toast.makeText(holder.itemView.context, "Photo deleted!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(holder.itemView.context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    override fun getItemCount() = items.size
}
