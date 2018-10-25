package nl.viasalix.horarium.persistence

import android.content.Context
import android.content.SharedPreferences

fun makeUserId(authCode: String) = "user_$authCode"

fun getUserSharedPreferences(userId: String, context: Context): SharedPreferences {
    return context.getSharedPreferences(userId, Context.MODE_PRIVATE)
}

