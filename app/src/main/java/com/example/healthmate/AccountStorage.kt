package com.example.healthmate

import android.content.Context

class AccountStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "HealthMateAccounts",
        Context.MODE_PRIVATE
    )

    fun register(name: String, email: String, password: String): Boolean {

        val normalizedEmail = email.trim().lowercase()

        if (preferences.contains(normalizedEmail)) {
            return false
        }

        preferences.edit()
            .putString("${normalizedEmail}_name", name)
            .putString("${normalizedEmail}_password", password)
            .apply()

        return true
    }

    fun login(email: String, password: String): Boolean {

        val normalizedEmail = email.trim().lowercase()

        val savedPassword = preferences.getString(
            "${normalizedEmail}_password",
            null
        )

        return savedPassword == password
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
}