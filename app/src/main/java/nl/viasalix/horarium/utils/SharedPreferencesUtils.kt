package nl.viasalix.horarium.utils

import android.content.Context
import android.content.SharedPreferences
import nl.viasalix.horarium.R
import org.jetbrains.anko.defaultSharedPreferences

object SharedPreferencesUtils {
    fun makeUserId(authCode: String) = "user_$authCode"

    fun getUserSharedPreferences(userId: String?, context: Context) =
        context.getSharedPreferences(userId, Context.MODE_PRIVATE)

    fun getCurrentUser(context: Context) =
        context.defaultSharedPreferences.getString(SP_KEY_CURRENT_USER, "")!!

    fun getCurrentUserSharedPreferences(context: Context): SharedPreferences =
        getUserSharedPreferences(getCurrentUser(context), context)
}

