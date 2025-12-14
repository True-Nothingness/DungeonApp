package com.light.dungeonofhabits

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.light.dungeonofhabits.api.ApiClient


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val logo = findViewById<ImageView>(R.id.imageView2)
        val name = findViewById<ImageView>(R.id.imageView4)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(animation)
        name.startAnimation(animation)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 2000)
    }

    private fun navigateNext() {
        val token = sharedPreferences.getString("jwt_token", null)
        ApiClient.init { token }

        if (!token.isNullOrEmpty()) {
            // Optionally validate token with backend here
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
