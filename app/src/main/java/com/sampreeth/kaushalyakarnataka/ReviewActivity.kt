package com.sampreeth.kaushalyakarnataka

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var workerId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        workerId = intent.getStringExtra("workerId") ?: ""

        val tvWorkerName = findViewById<TextView>(R.id.tvWorkerNameReview)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etReviewText = findViewById<EditText>(R.id.etReviewText)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitReview)
        val rvReviews = findViewById<RecyclerView>(R.id.rvAllReviews)

        rvReviews.layoutManager = LinearLayoutManager(this)

        // Load worker name
        db.collection("workers").document(workerId).get()
            .addOnSuccessListener { doc ->
                tvWorkerName.text = doc.getString("name") ?: ""
            }

        // Load reviews
        db.collection("workers").document(workerId)
            .collection("reviews")
            .addSnapshotListener { snapshot, _ ->
                val reviews = snapshot?.documents?.map { doc ->
                    Review(
                        reviewerName = doc.getString("reviewerName") ?: "",
                        text = doc.getString("text") ?: "",
                        stars = doc.getLong("stars")?.toInt() ?: 0
                    )
                } ?: emptyList()
                rvReviews.adapter = ReviewAdapter(reviews)
            }

        // Submit review
        btnSubmit.setOnClickListener {
            val reviewText = etReviewText.text.toString().trim()
            val stars = ratingBar.rating.toInt()

            if (reviewText.isEmpty()) {
                etReviewText.error = "Please write a review"
                etReviewText.requestFocus()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Submitting..."

            val currentUser = auth.currentUser
            val review = hashMapOf(
                "reviewerName" to (currentUser?.email ?: "Anonymous"),
                "text" to reviewText,
                "stars" to stars
            )

            db.collection("workers").document(workerId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener {
                    etReviewText.text.clear()
                    ratingBar.rating = 5f
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Review"
                    Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
                    updateAverageRating()
                }
                .addOnFailureListener {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Review"
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateAverageRating() {
        db.collection("workers").document(workerId)
            .collection("reviews")
            .get()
            .addOnSuccessListener { snapshot ->
                val reviews = snapshot.documents
                if (reviews.isNotEmpty()) {
                    val avg = reviews.mapNotNull {
                        it.getLong("stars")?.toInt()
                    }.average()
                    val rounded = Math.round(avg * 10.0) / 10.0
                    db.collection("workers").document(workerId)
                        .update("rating", rounded)
                }
            }
    }
}