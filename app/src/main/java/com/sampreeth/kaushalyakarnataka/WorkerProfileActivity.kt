package com.sampreeth.kaushalyakarnataka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

class WorkerProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var workerId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_profile)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        workerId = intent.getStringExtra("workerId") ?: ""

        if (workerId.isEmpty()) {
            Toast.makeText(this, "Worker not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvCategory = findViewById<TextView>(R.id.tvProfileCategory)
        val tvArea = findViewById<TextView>(R.id.tvProfileArea)
        val tvRating = findViewById<TextView>(R.id.tvProfileRating)
        val tvBio = findViewById<TextView>(R.id.tvProfileBio)
        val tvInitials = findViewById<TextView>(R.id.tvProfileInitials)
        val tvThumbsCount = findViewById<TextView>(R.id.tvThumbsCount)
        val btnHireMe = findViewById<Button>(R.id.btnHireMe)
        val btnThumbsUp = findViewById<Button>(R.id.btnThumbsUp)
        val btnWriteReview = findViewById<Button>(R.id.btnWriteReview)
        val etReview = findViewById<EditText>(R.id.etReview)
        val btnSubmitReview = findViewById<Button>(R.id.btnSubmitReview)
        val rvServices = findViewById<RecyclerView>(R.id.rvServices)
        val rvReviews = findViewById<RecyclerView>(R.id.rvReviews)
        val rvPortfolio = findViewById<RecyclerView>(R.id.rvPortfolio)
        val tvViewAllPortfolio = findViewById<TextView>(R.id.tvViewAllPortfolio)
        val tvNoPortfolio = findViewById<TextView>(R.id.tvNoPortfolio)

        rvServices.layoutManager = LinearLayoutManager(this)
        rvReviews.layoutManager = LinearLayoutManager(this)
        // Ensure horizontal layout for portfolio preview
        rvPortfolio.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val currentUserId = auth.currentUser?.uid ?: ""
        val isOwner = currentUserId == workerId

        // Hide customer-only actions if viewing own profile
        if (isOwner) {
            btnHireMe.visibility = View.GONE
            btnThumbsUp.visibility = View.GONE
            btnWriteReview.visibility = View.GONE
            btnSubmitReview.visibility = View.GONE
            etReview.visibility = View.GONE
        }

        // View All Portfolio listener
        tvViewAllPortfolio.setOnClickListener {
            val intent = Intent(this, PortfolioActivity::class.java)
            intent.putExtra("workerId", workerId)
            startActivity(intent)
        }

        // Load worker data
        db.collection("workers").document(workerId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val category = doc.getString("category") ?: ""
                    val area = doc.getString("area") ?: ""
                    val rating = doc.getDouble("rating") ?: 0.0
                    val bio = doc.getString("bio") ?: ""
                    val thumbsUp = doc.getLong("thumbsUp") ?: 0

                    tvName.text = name
                    tvCategory.text = category.ifEmpty { "No category set" }
                    tvArea.text = area.ifEmpty { "No area set" }
                    tvRating.text = if (rating > 0) "⭐ $rating" else "No ratings yet"
                    tvBio.text = bio.ifEmpty { "No bio added yet" }
                    tvInitials.text = name.firstOrNull()?.toString() ?: "?"
                    tvThumbsCount.text = "👍 $thumbsUp people thumbed up this worker"

                    // Check if current user already thumbed up
                    val thumbsUpList = doc.get("thumbsUpUsers") as? List<*> ?: emptyList<String>()
                    if (thumbsUpList.contains(currentUserId)) {
                        btnThumbsUp.text = "👍 Thumbed Up!"
                        btnThumbsUp.isEnabled = false
                        btnThumbsUp.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#888888")
                            )
                    }
                }
            }

        // Load services
        db.collection("workers").document(workerId)
            .collection("serviceCards")
            .addSnapshotListener { snapshot, _ ->
                val services = snapshot?.documents?.map { doc ->
                    ServiceCard(
                        id = doc.id,
                        serviceName = doc.getString("serviceName") ?: "",
                        price = doc.getString("price") ?: "",
                        priceType = doc.getString("priceType") ?: "Fixed"
                    )
                } ?: emptyList()
                rvServices.adapter = ServiceCardAdapter(services, isOwner, workerId)
            }

        // Load portfolio (preview)
        db.collection("workers").document(workerId)
            .collection("portfolio")
            .limit(5)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                val portfolioList = snapshot?.documents?.map { doc ->
                    PortfolioItem(
                        id = doc.id,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        caption = doc.getString("caption") ?: ""
                    )
                } ?: emptyList()

                if (portfolioList.isEmpty()) {
                    tvNoPortfolio.visibility = View.VISIBLE
                    rvPortfolio.visibility = View.GONE
                } else {
                    tvNoPortfolio.visibility = View.GONE
                    rvPortfolio.visibility = View.VISIBLE
                    rvPortfolio.adapter = PortfolioAdapter(portfolioList.toMutableList(), workerId, isOwner)
                }
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

        // Hire Me button
        btnHireMe.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnHireMe.isEnabled = false
            btnHireMe.text = "Sending..."

            val notification = hashMapOf(
                "customerEmail" to (currentUser.email ?: "Anonymous"),
                "customerId" to currentUser.uid,
                "message" to "A customer wants to hire you! Contact them at: ${currentUser.email}",
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false
            )

            db.collection("workers").document(workerId)
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener {
                    btnHireMe.isEnabled = true
                    btnHireMe.text = "Hire Me!"
                    Toast.makeText(
                        this,
                        "Your request has been sent! The worker will contact you soon. 📞",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener {
                    btnHireMe.isEnabled = true
                    btnHireMe.text = "Hire Me!"
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Thumbs Up button
        btnThumbsUp.setOnClickListener {
            if (currentUserId.isEmpty()) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add thumbs up
            db.collection("workers").document(workerId)
                .update(
                    "thumbsUp", FieldValue.increment(1),
                    "thumbsUpUsers", FieldValue.arrayUnion(currentUserId)
                )
                .addOnSuccessListener {
                    btnThumbsUp.text = "👍 Thumbed Up!"
                    btnThumbsUp.isEnabled = false
                    btnThumbsUp.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#888888")
                        )
                    Toast.makeText(this, "Thanks for the thumbs up! 👍", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Write Review button
        btnWriteReview.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("workerId", workerId)
            startActivity(intent)
        }

        // Submit quick review
        btnSubmitReview.setOnClickListener {
            val reviewText = etReview.text.toString().trim()
            if (reviewText.isEmpty()) {
                etReview.error = "Please write a review"
                etReview.requestFocus()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            val review = hashMapOf(
                "reviewerName" to (currentUser?.email ?: "Anonymous"),
                "text" to reviewText,
                "stars" to 5
            )

            db.collection("workers").document(workerId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener {
                    etReview.text.clear()
                    Toast.makeText(this, "Review submitted! ✅", Toast.LENGTH_SHORT).show()
                    updateAverageRating()
                }
                .addOnFailureListener {
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
