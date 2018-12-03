package nl.viasalix.horarium.utils

import android.content.Context
import nl.viasalix.horarium.persistence.HorariumDatabase
import nl.viasalix.horarium.persistence.ScheduleRepository
import nl.viasalix.horarium.ui.main.ScheduleViewModelFactory
import nl.viasalix.horarium.utils.SharedPreferencesUtils.getCurrentUser

object InjectorUtils {

    fun getScheduleRepository(user: String, context: Context) =
        ScheduleRepository.getInstance(HorariumDatabase.getInstance(user, context).appointmentDao())

    fun getCurrentUserScheduleRepository(context: Context): ScheduleRepository {
        val currentUser = getCurrentUser(context)
        return getScheduleRepository(currentUser, context)
    }

    fun provideScheduleViewModelFactory(context: Context): ScheduleViewModelFactory {
        val repository = getCurrentUserScheduleRepository(context)
        return ScheduleViewModelFactory(repository)
    }

}