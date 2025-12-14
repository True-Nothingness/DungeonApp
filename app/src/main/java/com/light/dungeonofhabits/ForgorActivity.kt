package com.light.dungeonofhabits

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.models.ForgorRequest
import com.light.dungeonofhabits.models.GenericResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgorActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var forgorBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        emailEditText = findViewById(R.id.emailEditText)
        forgorBtn = findViewById(R.id.forgorbtn)
        forgorBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            forgotPassword(email)
        }
    }
    private fun forgotPassword(email: String) {
        val request = ForgorRequest(email)
        ApiClient.apiService.forgotPassword(request).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgorActivity, "If the email exists, a reset link has been sent!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to send email"
                    Toast.makeText(this@ForgorActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@ForgorActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}