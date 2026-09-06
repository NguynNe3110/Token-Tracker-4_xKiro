package com.uzuu.adminitratortoken.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "token_tracker_encrypted_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(alias: String, apiKey: String) {
        prefs.edit().putString("api_key_$alias", apiKey).apply()
    }

    fun getApiKey(alias: String): String? {
        return prefs.getString("api_key_$alias", null)
    }

    fun deleteApiKey(alias: String) {
        prefs.edit().remove("api_key_$alias").apply()
    }

    fun hasApiKey(alias: String): Boolean {
        return prefs.contains("api_key_$alias")
    }
}
