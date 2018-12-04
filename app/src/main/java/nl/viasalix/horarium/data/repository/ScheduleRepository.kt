package nl.viasalix.horarium.data.repository

import androidx.lifecycle.LiveData
import nl.viasalix.horarium.data.net.ZermeloApi
import nl.viasalix.horarium.data.persistence.AppointmentDao
import nl.viasalix.horarium.data.net.args.GetAppointmentsArgs
import nl.viasalix.horarium.data.zermelo.model.Appointment
import nl.viasalix.horarium.utils.DateUtils.endOfWeek
import nl.viasalix.horarium.utils.DateUtils.getWeekWithOffset
import nl.viasalix.horarium.utils.DateUtils.startOfWeek
import org.jetbrains.anko.doAsync
import java.util.*

class ScheduleRepository(
        private val api: ZermeloApi,
        private val appointmentDao: AppointmentDao
) {

    fun getAppointmentsFromTill(from: Date, till: Date): LiveData<List<Appointment>> {
        refreshAppointmentsFromTill(from, till)
        return appointmentDao.getAppointments()
    }

    private fun refreshAppointmentsFromTill(from: Date, till: Date) {
        doAsync {
            val response = api.getAppointmentsWithArgs(GetAppointmentsArgs(from, till)).execute()
            response.body()?.response?.data?.run(appointmentDao::insertAppointments)
        }
    }

    private fun refreshAppointments(currentWeek: Int, currentYear: Int) {
        doAsync {
            val twoWeeksAgo = getWeekWithOffset(currentWeek, currentYear, -2)
            val inTwoWeeks = getWeekWithOffset(currentWeek, currentYear, +2)
            val from = startOfWeek(twoWeeksAgo.second, twoWeeksAgo.first)
            val till = endOfWeek(inTwoWeeks.second, inTwoWeeks.first)

            val response = api.getAppointmentsWithArgs(GetAppointmentsArgs(from, till)).execute()
            response.body()?.response?.data?.run {
                appointmentDao.deleteAppointmentsFromTill(from, till)
                appointmentDao::insertAppointments
            }
        }
    }

    companion object {

        @Volatile
        private var instance: ScheduleRepository? = null

        fun getInstance(api: ZermeloApi, appointmentDao: AppointmentDao) =
                instance ?: synchronized(this) {
                    instance ?: ScheduleRepository(api, appointmentDao).also { instance = it }
                }

    }

}