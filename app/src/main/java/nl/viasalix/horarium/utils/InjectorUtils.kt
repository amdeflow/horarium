package nl.viasalix.horarium.utils

import android.content.Context
import nl.viasalix.horarium.data.net.ZermeloApi
import nl.viasalix.horarium.data.persistence.HorariumDatabase
import nl.viasalix.horarium.data.repository.ScheduleRepository
import nl.viasalix.horarium.ui.main.ScheduleViewModelFactory
import nl.viasalix.horarium.utils.SharedPreferencesUtils.getAccessToken
import nl.viasalix.horarium.utils.SharedPreferencesUtils.getCurrentUser
import nl.viasalix.horarium.utils.SharedPreferencesUtils.getSchoolName

object InjectorUtils {

    fun getScheduleRepository(schoolName: String, accessToken: String, user: String, context: Context) =
            ScheduleRepository.getInstance(
                    ZermeloApi.getInstance(schoolName, accessToken),
                    HorariumDatabase.getInstance(user, context).appointmentDao()
            )

    fun getCurrentUserScheduleRepository(context: Context): ScheduleRepository {
        val currentUser = getCurrentUser(context)
        val schoolName = getSchoolName(context)
        val accessToken = getAccessToken(currentUser, context)
        return getScheduleRepository(schoolName, accessToken, currentUser, context)
    }

    fun provideScheduleViewModelFactory(context: Context): ScheduleViewModelFactory {
        val repository = getCurrentUserScheduleRepository(context)
        return ScheduleViewModelFactory(repository)
    }

}