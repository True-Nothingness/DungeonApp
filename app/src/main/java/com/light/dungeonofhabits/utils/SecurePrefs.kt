package com.light.dungeonofhabits.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

object SecurePrefs {

    private const val PREFS_NAME = "secure_user_prefs"
    private const val KEY_JWT_TOKEN = "jwt_token"

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(context: Context, token: String) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit { putString(KEY_JWT_TOKEN, token) }
    }

    fun getToken(context: Context): String? {
        val prefs = getEncryptedSharedPreferences(context)
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun clear(context: Context) {
        val prefs = getEncryptedSharedPreferences(context)
        prefs.edit(commit = true) { clear() }
    }
}
