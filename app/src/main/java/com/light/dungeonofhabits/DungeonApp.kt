package com.light.dungeonofhabits

import android.app.Application
import com.light.dungeonofhabits.api.ApiClient
import com.light.dungeonofhabits.utils.SecurePrefs

class DungeonApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}
