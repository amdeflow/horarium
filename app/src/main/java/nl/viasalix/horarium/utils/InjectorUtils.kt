package nl.viasalix.horarium.utils

import android.content.Context
import android.util.Log
import nl.viasalix.horarium.data.net.ZermeloApi
import nl.viasalix.horarium.data.persistence.HorariumDatabase
import nl.viasalix.horarium.data.repository.ScheduleRepository
import nl.viasalix.horarium.ui.main.ScheduleViewModelFactory
import nl.viasalix.horarium.utils.SharedPreferencesUtils.getAccessToken
import nl.viasalix.horarium.utils.SharedPreferencesUtils.getCurrentUser
import nl.viasalix.horarium.utils.SharedPreferencesUtils.getSchoolName
import nl.viasalix.horarium.utils.SharedPreferencesUtils.getUserSharedPreferences

object InjectorUtils {

    fun getScheduleRepository(schoolName: String, accessToken: String, user: String, context: Context) =
            ScheduleRepository.getInstance(
                    ZermeloApi.buildApi(schoolName, accessToken),
                    HorariumDatabase.getInstance(user, context).appointmentDao()
            )

    fun getCurrentUserScheduleRepository(context: Context): ScheduleRepository {
        val currentUser = getCurrentUser(context)
        val userSp = getUserSharedPreferences(currentUser, context)
        val schoolName = getSchoolName(userSp)
        val accessToken = getAccessToken(userSp)
        return getScheduleRepository(schoolName, accessToken, currentUser, context)
    }

    fun provideScheduleViewModelFactory(context: Context): ScheduleViewModelFactory {
        val repository = getCurrentUserScheduleRepository(context)
        return ScheduleViewModelFactory(repository)
    }

}
