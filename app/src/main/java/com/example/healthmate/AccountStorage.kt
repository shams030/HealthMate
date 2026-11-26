
package com.example.healthmate

import android.content.Context
import java.security.MessageDigest

class AccountStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "HealthMateAccounts",
        Context.MODE_PRIVATE
    )

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun register(name: String, email: String, password: String): Boolean {

        val normalizedEmail = email.trim().lowercase()

        if (accountExists(normalizedEmail)) {
            return false
        }

        preferences.edit()
            .putString("${normalizedEmail}_name", name)
            .putString("${normalizedEmail}_password", hashPassword(password))
            .apply()

        return true
    }

    fun login(email: String, password: String): Boolean {

        val normalizedEmail = email.trim().lowercase()

        val savedPassword = preferences.getString(
            "${normalizedEmail}_password",
            null
        )

        return savedPassword == hashPassword(password)
    }

    fun accountExists(email: String): Boolean {

        val normalizedEmail = email.trim().lowercase()

        return preferences.contains("${normalizedEmail}_password")
    }

    fun getName(email: String): String {

        val normalizedEmail = email.trim().lowercase()

        return preferences.getString(
            "${normalizedEmail}_name",
            "HealthMate User"
        ) ?: "HealthMate User"
    }
    fun saveLoginSession(email: String) {
        preferences.edit()
            .putString("logged_in_email", email.trim().lowercase())
            .apply()
    }

    fun getLoggedInEmail(): String? {
        return preferences.getString("logged_in_email", null)
    }

    fun clearLoginSession() {
        preferences.edit()
            .remove("logged_in_email")
            .apply()
    }
}
