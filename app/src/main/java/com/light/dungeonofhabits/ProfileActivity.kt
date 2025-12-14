package com.light.dungeonofhabits

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.databinding.ActivityProfileBinding
import com.light.dungeonofhabits.models.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getProfile()
        binding.registerbtn.setOnClickListener{
            updateProfile()
            binding.registerbtn.isEnabled = false
        }
    }

    private fun getProfile() {
        ApiClient.apiService.getUserProfile().enqueue(object : Callback<Profile> {
            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    binding.usernameEditText.setText(profile?.user?.username)
                    binding.emailEditText.setText(profile?.user?.email)
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Profile>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateProfile() {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()

        if (!isValidUsername(username)) {
            binding.textInputLayout3.error = "Username must be 6-20 characters and use only letters, numbers, or _"
            binding.registerbtn.isEnabled = true
            return
        } else {
            binding.textInputLayout3.error = null
        }

        if (!isValidEmail(email)) {
            binding.emailTextInputLayout.error = "Invalid email format"
            binding.registerbtn.isEnabled = true
            return
        } else {
            binding.emailTextInputLayout.error = null
        }

        val updates = mapOf(
            "username" to username,
            "email" to email
        )

        ApiClient.apiService.updateProfile(updates).enqueue(object : Callback<Profile> {
            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                binding.registerbtn.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Profile>, t: Throwable) {
                binding.registerbtn.isEnabled = true
                Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidUsername(username: String): Boolean {
        val usernameRegex = Regex("^[a-zA-Z0-9_]{6,20}$")
        return usernameRegex.matches(username)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
