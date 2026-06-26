package com.oraculo.app.data.local.preferences

import android.content.Context
import java.util.UUID

/*
 * Clase encargada de almacenar y recuperar la identidad del usuario.
 *
 * Se usa SharedPreferences por simplicidad y estabilidad.
 */
class UserIdentityStore(context: Context) {

    private val prefs =
        context.getSharedPreferences("oracle_user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    /*
     * Devuelve el user_id.
     * Si no existe, lo crea automáticamente.
     */
    fun getOrCreateUserId(): String {
        var userId = prefs.getString(KEY_USER_ID, null)

        if (userId == null) {
            userId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }

        return userId
    }

    /*
     * Guarda el nombre del usuario.
     */
    fun saveUserName(name: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putBoolean(KEY_ONBOARDING_DONE, true)
            .apply()
    }

    /*
     * Devuelve el nombre del usuario.
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /*
     * Indica si el usuario ya pasó el onboarding.
     */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }
}