package com.light.dungeonofhabits

import android.app.Application
import com.light.dungeonofhabits.api.ApiClient

class DungeonApp : Application() {
    override fun onCreate() {
        super.onCreate()

        ApiClient.init {
            // return the token (null if not logged in)
            getSharedPreferences("prefs", MODE_PRIVATE)
                .getString("jwt_token", null)
        }
    }
}
