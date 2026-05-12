package com.sampreeth.kaushalyakarnataka

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.util.*

class PortfolioActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private val portfolioList = mutableListOf<PortfolioItem>()
    private lateinit var adapter: PortfolioAdapter

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            val ivSelectedImage = findViewById<ImageView>(R.id.ivSelectedImage)
            Glide.with(this).load(it).centerCrop().into(ivSelectedImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val workerIdFromIntent = intent.getStringExtra("workerId")
        val currentUserId = auth.currentUser?.uid ?: ""
        val targetWorkerId = if (!workerIdFromIntent.isNullOrEmpty()) workerIdFromIntent else currentUserId
        
        if (targetWorkerId.isEmpty()) {
            finish()
            return
        }

        val isOwner = targetWorkerId == currentUserId

        val tvTitle = findViewById<TextView>(R.id.tvPortfolioTitle)
        val addPhotoCard = findViewById<View>(R.id.cvAddPhoto)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnUpload = findViewById<Button>(R.id.btnUploadPhoto)
        val etCaption = findViewById<EditText>(R.id.etCaption)
        val rvPortfolio = findViewById<RecyclerView>(R.id.rvPortfolio)

        if (isOwner) {
            tvTitle.text = "My Portfolio"
            addPhotoCard.visibility = View.VISIBLE
        } else {
            tvTitle.text = "Worker Portfolio"
            addPhotoCard.visibility = View.GONE
        }

        adapter = PortfolioAdapter(portfolioList, targetWorkerId, isOwner)
        rvPortfolio.layoutManager = LinearLayoutManager(this)
        rvPortfolio.adapter = adapter

        btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnUpload.setOnClickListener {
            val caption = etCaption.text.toString().trim()
            val imageUri = selectedImageUri

            if (imageUri == null) {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnUpload.isEnabled = false
            btnUpload.text = "Processing..."

            // Convert image to Base64 String (FREE Workaround)
            val base64Image = uriToBase64(imageUri)
            if (base64Image != null) {
                savePortfolioToFirestore(targetWorkerId, base64Image, caption)
            } else {
                btnUpload.isEnabled = true
                btnUpload.text = "Add to Portfolio"
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        }

        loadPortfolio(targetWorkerId)
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            // Resize image to keep it under 1MB Firestore limit
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true)
            
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    private fun savePortfolioToFirestore(workerId: String, base64String: String, caption: String) {
        val btnUpload = findViewById<Button>(R.id.btnUploadPhoto)
        val etCaption = findViewById<EditText>(R.id.etCaption)

        val portfolioItem = hashMapOf(
            "imageUrl" to base64String, // This is now the actual image data
            "caption" to caption,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("workers").document(workerId)
            .collection("portfolio")
            .add(portfolioItem)
            .addOnSuccessListener {
                btnUpload.isEnabled = true
                btnUpload.text = "Add to Portfolio"
                etCaption.text.clear()
                selectedImageUri = null
                findViewById<ImageView>(R.id.ivSelectedImage).setImageResource(android.R.drawable.ic_menu_gallery)
                Toast.makeText(this, "Added to portfolio! ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                btnUpload.isEnabled = true
                btnUpload.text = "Add to Portfolio"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPortfolio(userId: String) {
        db.collection("workers").document(userId)
            .collection("portfolio")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                portfolioList.clear()
                val docs = snapshot?.documents ?: emptyList()
                docs.forEach { doc ->
                    portfolioList.add(
                        PortfolioItem(
                            id = doc.id,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            caption = doc.getString("caption") ?: ""
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
    }
}
