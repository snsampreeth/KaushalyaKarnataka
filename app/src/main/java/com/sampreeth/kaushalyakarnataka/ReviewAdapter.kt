package com.sampreeth.kaushalyakarnataka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(
    private val reviews: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReviewerName: TextView = view.findViewById(R.id.tvReviewerName)
        val tvReviewText: TextView = view.findViewById(R.id.tvReviewText)
        val tvReviewStars: TextView = view.findViewById(R.id.tvReviewStars)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.tvReviewerName.text = review.reviewerName
        holder.tvReviewText.text = review.text
        holder.tvReviewStars.text = "⭐".repeat(review.stars)
    }

    override fun getItemCount() = reviews.size
}