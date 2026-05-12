package com.sampreeth.kaushalyakarnataka

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val etName = findViewById<EditText>(R.id.etEditName)
        val etArea = findViewById<EditText>(R.id.etEditArea)
        val etBio = findViewById<EditText>(R.id.etEditBio)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)

        // Category options
        val categories = listOf("Electrician", "Plumber", "Carpenter", "Painter", "Mason", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Load existing data
        val userId = auth.currentUser?.uid ?: return
        db.collection("workers").document(userId).get()
            .addOnSuccessListener { doc ->
                etName.setText(doc.getString("name") ?: "")
                etArea.setText(doc.getString("area") ?: "")
                etBio.setText(doc.getString("bio") ?: "")
                val category = doc.getString("category") ?: ""
                val categoryIndex = categories.indexOf(category)
                if (categoryIndex >= 0) spinnerCategory.setSelection(categoryIndex)
            }

        // Save profile
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val area = etArea.text.toString().trim()
            val bio = etBio.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()

            if (name.isEmpty()) {
                etName.error = "Name is required"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (area.isEmpty()) {
                etArea.error = "Area is required"
                etArea.requestFocus()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "Saving..."

            db.collection("workers").document(userId)
                .update(
                    "name", name,
                    "category", category,
                    "area", area,
                    "bio", bio
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    btnSave.isEnabled = true
                    btnSave.text = "Save Profile"
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}