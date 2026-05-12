package com.sampreeth.kaushalyakarnataka

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomerHomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var workerList = mutableListOf<Worker>()
    private var filteredList = mutableListOf<Worker>()
    private lateinit var adapter: WorkerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_home)

        db = FirebaseFirestore.getInstance()

        val rvWorkers = findViewById<RecyclerView>(R.id.rvWorkers)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        adapter = WorkerAdapter(filteredList) { worker ->
            val intent = Intent(this, WorkerProfileActivity::class.java)
            intent.putExtra("workerId", worker.id)
            startActivity(intent)
        }

        rvWorkers.layoutManager = LinearLayoutManager(this)
        rvWorkers.adapter = adapter

        // Logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Category filters
        findViewById<Button>(R.id.btnAll).setOnClickListener { filterByCategory("") }
        findViewById<Button>(R.id.btnElectrician).setOnClickListener { filterByCategory("Electrician") }
        findViewById<Button>(R.id.btnPlumber).setOnClickListener { filterByCategory("Plumber") }
        findViewById<Button>(R.id.btnCarpenter).setOnClickListener { filterByCategory("Carpenter") }

        // Search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterByName(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Bottom Navigation
        findViewById<BottomNavigationView>(R.id.bottomNavigation).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_ai -> {
                    startActivity(Intent(this, CustomerAIActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, CustomerProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        loadWorkers()
    }

    private fun loadWorkers() {
        // Only load workers with role = "worker"
        db.collection("workers")
            .whereEqualTo("role", "worker")
            .addSnapshotListener { snapshot, _ ->
                workerList.clear()
                snapshot?.documents?.forEach { doc ->
                    workerList.add(
                        Worker(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "",
                            area = doc.getString("area") ?: "",
                            rating = doc.getDouble("rating") ?: 0.0,
                            profileImageUrl = doc.getString("profileImageUrl") ?: ""
                        )
                    )
                }
                filteredList.clear()
                filteredList.addAll(workerList)
                adapter.notifyDataSetChanged()
                updateEmptyState()
            }
    }

    private fun filterByCategory(category: String) {
        filteredList.clear()
        if (category.isEmpty()) filteredList.addAll(workerList)
        else filteredList.addAll(workerList.filter { it.category == category })
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun filterByName(query: String) {
        filteredList.clear()
        filteredList.addAll(workerList.filter {
            it.name.contains(query, ignoreCase = true)
        })
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val emptyState = findViewById<LinearLayout>(R.id.emptyState)
        val rvWorkers = findViewById<RecyclerView>(R.id.rvWorkers)
        if (filteredList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rvWorkers.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rvWorkers.visibility = View.VISIBLE
        }
    }
}
