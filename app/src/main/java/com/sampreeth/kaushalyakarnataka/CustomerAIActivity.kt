package com.sampreeth.kaushalyakarnataka

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CustomerAIActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_ai)

        setupFindWorker()
        setupPriceChecker()
        setupQuestions()
    }

    private fun setupFindWorker() {
        val etProblem = findViewById<EditText>(R.id.etProblem)
        val btnFind = findViewById<Button>(R.id.btnFindWorker)
        val tvResult = findViewById<TextView>(R.id.tvWorkerAdvice)

        btnFind.setOnClickListener {
            val problem = etProblem.text.toString().trim()
            if (problem.isEmpty()) {
                etProblem.error = "Describe your problem"
                return@setOnClickListener
            }

            btnFind.isEnabled = false
            btnFind.text = "Getting advice..."
            tvResult.visibility = View.GONE

            val prompt = """
                A customer in Karnataka, India has this problem: $problem
                Which type of worker should they hire? (Electrician/Plumber/Carpenter/Painter/Mason)
                Give a 2-3 line answer with the worker type and why.
                Keep it simple and practical.
            """.trimIndent()

            lifecycleScope.launch {
                val result = ClaudeHelper.ask(prompt)
                runOnUiThread {
                    btnFind.isEnabled = true
                    btnFind.text = "Get Advice"
                    result.onSuccess { advice ->
                        tvResult.text = advice
                        tvResult.visibility = View.VISIBLE
                    }.onFailure {
                        Toast.makeText(this@CustomerAIActivity,
                            "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupPriceChecker() {
        val etService = findViewById<EditText>(R.id.etServiceCheck)
        val btnCheck = findViewById<Button>(R.id.btnCheckPrice)
        val tvResult = findViewById<TextView>(R.id.tvPriceCheck)

        btnCheck.setOnClickListener {
            val service = etService.text.toString().trim()
            if (service.isEmpty()) {
                etService.error = "Enter service and price"
                return@setOnClickListener
            }

            btnCheck.isEnabled = false
            btnCheck.text = "Checking..."
            tvResult.visibility = View.GONE

            val prompt = """
                A customer in Karnataka, India was quoted this price: $service
                Is this price fair, too high, or too low for this service?
                Give a 2-3 line honest answer with the typical price range in rupees.
            """.trimIndent()

            lifecycleScope.launch {
                val result = ClaudeHelper.ask(prompt)
                runOnUiThread {
                    btnCheck.isEnabled = true
                    btnCheck.text = "Check Price"
                    result.onSuccess { check ->
                        tvResult.text = check
                        tvResult.visibility = View.VISIBLE
                    }.onFailure {
                        Toast.makeText(this@CustomerAIActivity,
                            "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupQuestions() {
        val etWorkerType = findViewById<EditText>(R.id.etWorkerType)
        val btnGet = findViewById<Button>(R.id.btnGetQuestions)
        val tvResult = findViewById<TextView>(R.id.tvQuestions)

        btnGet.setOnClickListener {
            val workerType = etWorkerType.text.toString().trim()
            if (workerType.isEmpty()) {
                etWorkerType.error = "Enter worker type"
                return@setOnClickListener
            }

            btnGet.isEnabled = false
            btnGet.text = "Getting questions..."
            tvResult.visibility = View.GONE

            val prompt = """
                Give 5 smart questions a customer should ask before hiring a $workerType 
                in Karnataka, India.
                Keep each question short and practical.
                Number them 1-5.
            """.trimIndent()

            lifecycleScope.launch {
                val result = ClaudeHelper.ask(prompt)
                runOnUiThread {
                    btnGet.isEnabled = true
                    btnGet.text = "Get Questions"
                    result.onSuccess { questions ->
                        tvResult.text = questions
                        tvResult.visibility = View.VISIBLE
                    }.onFailure {
                        Toast.makeText(this@CustomerAIActivity,
                            "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}