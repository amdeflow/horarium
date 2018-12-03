package nl.viasalix.horarium.persistence

import nl.viasalix.horarium.utils.DateUtils.endOfWeek
import nl.viasalix.horarium.utils.DateUtils.startOfWeek
import java.util.*

class ScheduleRepository private constructor(private val appointmentDao: AppointmentDao) {

    fun getAppointments() = appointmentDao.getAppointments()

    fun getAppointmentsFromTill(from: Date, till: Date) =
            appointmentDao.getAppointmentsFromTill(from, till)

    fun getAppointmentsInWeekOfYear(week: Int, year: Int) =
            getAppointmentsFromTill(startOfWeek(week, year), endOfWeek(week, year))

    companion object {

        @Volatile private var instance: ScheduleRepository? = null

        fun getInstance(appointmentDao: AppointmentDao) =
                instance ?: synchronized(this) {
                    instance ?: ScheduleRepository(appointmentDao).also { instance = it }
                }

    }
}