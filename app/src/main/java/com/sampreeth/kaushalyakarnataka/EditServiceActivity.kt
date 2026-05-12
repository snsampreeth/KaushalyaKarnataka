package com.sampreeth.kaushalyakarnataka

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditServiceActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_service)

        db = FirebaseFirestore.getInstance()

        val serviceId = intent.getStringExtra("serviceId") ?: ""
        val workerId = intent.getStringExtra("workerId") ?: ""

        val etServiceName = findViewById<EditText>(R.id.etEditServiceName)
        val etPrice = findViewById<EditText>(R.id.etEditPrice)
        val rgPriceType = findViewById<RadioGroup>(R.id.rgEditPriceType)
        val btnUpdate = findViewById<Button>(R.id.btnUpdateService)
        val btnDelete = findViewById<Button>(R.id.btnDeleteService)

        // Pre-fill existing data
        etServiceName.setText(intent.getStringExtra("serviceName") ?: "")
        etPrice.setText(intent.getStringExtra("price") ?: "")
        val priceType = intent.getStringExtra("priceType") ?: "Fixed"
        if (priceType == "Fixed") {
            rgPriceType.check(R.id.rbEditFixed)
        } else {
            rgPriceType.check(R.id.rbEditStartingAt)
        }

        // Update service
        btnUpdate.setOnClickListener {
            val serviceName = etServiceName.text.toString().trim()
            val price = etPrice.text.toString().trim()
            val newPriceType = if (rgPriceType.checkedRadioButtonId == R.id.rbEditFixed)
                "Fixed" else "Starting At"

            if (serviceName.isEmpty()) {
                etServiceName.error = "Service name is required"
                return@setOnClickListener
            }

            if (price.isEmpty()) {
                etPrice.error = "Price is required"
                return@setOnClickListener
            }

            btnUpdate.isEnabled = false
            btnUpdate.text = "Updating..."

            db.collection("workers").document(workerId)
                .collection("serviceCards").document(serviceId)
                .update(
                    "serviceName", serviceName,
                    "price", price,
                    "priceType", newPriceType
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Service updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    btnUpdate.isEnabled = true
                    btnUpdate.text = "Update Service"
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Delete service
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Service")
                .setMessage("Are you sure you want to delete this service?")
                .setPositiveButton("Delete") { _, _ ->
                    db.collection("workers").document(workerId)
                        .collection("serviceCards").document(serviceId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Service deleted!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}