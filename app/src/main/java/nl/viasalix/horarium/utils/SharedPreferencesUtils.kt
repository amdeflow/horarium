package nl.viasalix.horarium.utils

import android.content.Context
import android.content.SharedPreferences
import nl.viasalix.horarium.utils.Constants.SP_KEY_ACCESS_TOKEN
import nl.viasalix.horarium.utils.Constants.SP_KEY_CURRENT_USER
import nl.viasalix.horarium.utils.Constants.SP_KEY_SCHOOL_NAME
import org.jetbrains.anko.defaultSharedPreferences

object SharedPreferencesUtils {
    fun makeUserId(authCode: String) = "user_$authCode"

    fun getUserSharedPreferences(userId: String?, context: Context): SharedPreferences =
            context.getSharedPreferences(userId, Context.MODE_PRIVATE)

    fun getCurrentUser(context: Context) =
            context.defaultSharedPreferences.getString(SP_KEY_CURRENT_USER, "")!!

    fun getSchoolName(userSp: SharedPreferences) =
            userSp.getString(SP_KEY_SCHOOL_NAME, "")!!

    fun getAccessToken(userSp: SharedPreferences) =
            userSp.getString(SP_KEY_ACCESS_TOKEN, "")!!

    fun getCurrentUserSharedPreferences(context: Context): SharedPreferences =
            getUserSharedPreferences(getCurrentUser(context), context)
}

