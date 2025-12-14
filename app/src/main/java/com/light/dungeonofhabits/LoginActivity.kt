package com.light.dungeonofhabits

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.models.LoginRequest
import com.light.dungeonofhabits.models.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.light.dungeonofhabits.models.Profile
import com.light.dungeonofhabits.models.User
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usernameET: TextInputEditText
    private lateinit var passwordET: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton
    private lateinit var forgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        usernameET = findViewById(R.id.usernameEditText)
        passwordET = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginbtn)
        registerButton = findViewById(R.id.registerbtn)
        forgotPassword = findViewById(R.id.textView)

        loginButton.setOnClickListener {
            val username = usernameET.text.toString()
            val password = passwordET.text.toString()
            loginUser(username, password)
        }
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgorActivity::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        val request = LoginRequest(username, password)

        ApiClient.apiService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val token = loginResponse?.token

                    if (token != null) {
                        // Save token to SharedPreferences
                        sharedPreferences.edit(commit = true) { putString("jwt_token", token) }
                        ApiClient.init { token }
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed: token missing", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "").optString("message", "Login failed")
                    } catch (e: Exception) {
                        "Login failed"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
