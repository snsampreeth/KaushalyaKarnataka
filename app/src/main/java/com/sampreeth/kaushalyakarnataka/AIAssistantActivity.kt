package com.sampreeth.kaushalyakarnataka

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AIAssistantActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var workerName = ""
    private var workerCategory = ""
    private var workerArea = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_assistant)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid ?: return

        // Load worker data first
        db.collection("workers").document(userId).get()
            .addOnSuccessListener { doc ->
                workerName = doc.getString("name") ?: ""
                workerCategory = doc.getString("category") ?: ""
                workerArea = doc.getString("area") ?: ""
            }

        setupBioGenerator(userId)
        setupPriceSuggester()
        setupReviewSummarizer(userId)
    }

    private fun setupBioGenerator(userId: String) {
        val btnGenerate = findViewById<Button>(R.id.btnGenerateBio)
        val tvResult = findViewById<TextView>(R.id.tvBioResult)
        val btnUseBio = findViewById<Button>(R.id.btnUseBio)

        btnGenerate.setOnClickListener {
            btnGenerate.isEnabled = false
            btnGenerate.text = "Generating..."
            tvResult.visibility = View.GONE
            btnUseBio.visibility = View.GONE

            val prompt = """
                Write a professional 3-line bio for a local service worker in Karnataka, India.
                Name: $workerName
                Category: $workerCategory
                Area: $workerArea
                Make it friendly, trustworthy and concise. Write only the bio, nothing else.
            """.trimIndent()

            lifecycleScope.launch {
                val result = ClaudeHelper.ask(prompt)
                runOnUiThread {
                    btnGenerate.isEnabled = true
                    btnGenerate.text = "Generate Bio"
                    result.onSuccess { bio ->
                        tvResult.text = bio
                        tvResult.visibility = View.VISIBLE
                        btnUseBio.visibility = View.VISIBLE
                    }.onFailure {
                        Toast.makeText(this@AIAssistantActivity,
                            "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnUseBio.setOnClickListener {
            val bio = tvResult.text.toString()
            db.collection("workers").document(userId)
                .update("bio", bio)
                .addOnSuccessListener {
                    Toast.makeText(this, "Bio saved to profile!", Toast.LENGTH_SHORT).show()
                    btnUseBio.visibility = View.GONE
                }
        }
    }

    private fun setupPriceSuggester() {
        val etService = findViewById<EditText>(R.id.etServiceQuery)
        val btnSuggest = findViewById<Button>(R.id.btnSuggestPrice)
        val tvResult = findViewById<TextView>(R.id.tvPriceResult)

        btnSuggest.setOnClickListener {
            val service = etService.text.toString().trim()
            if (service.isEmpty()) {
                etService.error = "Enter a service name"
                return@setOnClickListener
            }

            btnSuggest.isEnabled = false
            btnSuggest.text = "Suggesting..."
            tvResult.visibility = View.GONE

            val prompt = """
                What is a fair price range in Indian Rupees (₹) for a local $workerCategory 
                in Karnataka, India to charge for: $service
                Give a short answer with min and max price range and one reason. 
                Keep it under 3 lines.
            """.trimIndent()

            lifecycleScope.launch {
                val result = ClaudeHelper.ask(prompt)
                runOnUiThread {
                    btnSuggest.isEnabled = true
                    btnSuggest.text = "Suggest Price"
                    result.onSuccess { price ->
                        tvResult.text = price
                        tvResult.visibility = View.VISIBLE
                    }.onFailure {
                        Toast.makeText(this@AIAssistantActivity,
                            "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupReviewSummarizer(userId: String) {
        val btnSummarize = findViewById<Button>(R.id.btnSummarizeReviews)
        val tvResult = findViewById<TextView>(R.id.tvReviewSummary)

        btnSummarize.setOnClickListener {
            btnSummarize.isEnabled = false
            btnSummarize.text = "Summarizing..."
            tvResult.visibility = View.GONE

            db.collection("workers").document(userId)
                .collection("reviews").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        btnSummarize.isEnabled = true
                        btnSummarize.text = "Summarize Reviews"
                        Toast.makeText(this, "No reviews yet!", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val reviewTexts = snapshot.documents.joinToString("\n") { doc ->
                        "- ${doc.getString("text")} (${doc.getLong("stars")} stars)"
                    }

                    val prompt = """
                        Summarize these customer reviews for a local worker in Karnataka in 2-3 sentences.
                        Focus on what customers liked and any concerns.
                        Reviews:
                        $reviewTexts
                    """.trimIndent()

                    lifecycleScope.launch {
                        val result = ClaudeHelper.ask(prompt)
                        runOnUiThread {
                            btnSummarize.isEnabled = true
                            btnSummarize.text = "Summarize Reviews"
                            result.onSuccess { summary ->
                                tvResult.text = summary
                                tvResult.visibility = View.VISIBLE
                            }.onFailure {
                                Toast.makeText(this@AIAssistantActivity,
                                    "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }
}