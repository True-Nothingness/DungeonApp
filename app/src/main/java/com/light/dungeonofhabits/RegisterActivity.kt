package com.light.dungeonofhabits

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.models.RegisterRequest
import com.light.dungeonofhabits.models.GenericResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var usernameET: TextInputEditText
    private lateinit var emailET: TextInputEditText
    private lateinit var passwordET: TextInputEditText
    private lateinit var repeatET: TextInputEditText
    private lateinit var usernameTL: TextInputLayout
    private lateinit var emailTL: TextInputLayout
    private lateinit var passwordTL: TextInputLayout
    private lateinit var repeatTL: TextInputLayout
    private lateinit var registerButton: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        usernameET = findViewById(R.id.usernameEditText)
        emailET = findViewById(R.id.emailEditText)
        passwordET = findViewById(R.id.passwordEditText)
        repeatET = findViewById(R.id.repeatEditText)
        usernameTL = findViewById(R.id.textInputLayout3)
        emailTL = findViewById(R.id.emailTextInputLayout)
        passwordTL = findViewById(R.id.textInputLayout4)
        repeatTL = findViewById(R.id.repeatInputLayout)
        registerButton = findViewById(R.id.registerbtn)
        clearErrorOnInput(usernameET, usernameTL)
        clearErrorOnInput(emailET, emailTL)
        clearErrorOnInput(passwordET, passwordTL)
        clearErrorOnInput(repeatET, repeatTL)
        registerButton.setOnClickListener{
            val username = usernameET.text.toString()
            val email = emailET.text.toString()
            val password = passwordET.text.toString()
            val repeat = repeatET.text.toString()
            if (!isValidUsername(username)) {
                usernameTL.setError("Username must be 6-20 characters and use only letters, numbers, or _")
            } else if (!isValidEmail(email)) {
                emailTL.setError("Invalid email format")
            } else if (!isValidPassword(password)) {
                passwordTL.setError("Password must be at least 8 characters, contain a number and an uppercase letter")
            } else if (password != repeat) {
                repeatTL.setError("Passwords do not match")
            } else {
                register(username, email, password)
            }
        }
    }
    private fun register(username: String, email: String, password: String){
        val request = RegisterRequest(username, email, password)
        ApiClient.apiService.register(request).enqueue(object: Callback<GenericResponse>{
            override fun onResponse(
                call: Call<GenericResponse>,
                response: Response<GenericResponse>
            ) {
                if(response.isSuccessful){
                    Toast.makeText(this@RegisterActivity, "Register successfully, please login!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    // Extract and show error message from server
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "").optString("message", "Login failed")
                    } catch (e: Exception) {
                        "Login failed"
                    }
                    showToast(errorMessage)
                }
            }
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                showToast("Network error: ${t.message}")
            }
        })
    }
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        return passwordRegex.matches(password)
    }
    private fun isValidUsername(username: String): Boolean {
        val usernameRegex = Regex("^[a-zA-Z0-9_]{6,20}$")
        return usernameRegex.matches(username)
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun clearErrorOnInput(editText: TextInputEditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

}