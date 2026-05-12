package com.sampreeth.kaushalyakarnataka

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddServiceActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val etServiceName = findViewById<EditText>(R.id.etServiceName)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val rgPriceType = findViewById<RadioGroup>(R.id.rgPriceType)
        val btnAddService = findViewById<Button>(R.id.btnAddService)

        btnAddService.setOnClickListener {
            val serviceName = etServiceName.text.toString().trim()
            val price = etPrice.text.toString().trim()
            val priceType = if (rgPriceType.checkedRadioButtonId == R.id.rbFixed)
                "Fixed" else "Starting At"

            if (serviceName.isEmpty()) {
                etServiceName.error = "Service name is required"
                etServiceName.requestFocus()
                return@setOnClickListener
            }

            if (price.isEmpty()) {
                etPrice.error = "Price is required"
                etPrice.requestFocus()
                return@setOnClickListener
            }

            btnAddService.isEnabled = false
            btnAddService.text = "Adding..."

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val service = hashMapOf(
                "serviceName" to serviceName,
                "price" to price,
                "priceType" to priceType
            )

            db.collection("workers").document(userId)
                .collection("serviceCards")
                .add(service)
                .addOnSuccessListener {
                    Toast.makeText(this, "Service added!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    btnAddService.isEnabled = true
                    btnAddService.text = "Add Service"
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}