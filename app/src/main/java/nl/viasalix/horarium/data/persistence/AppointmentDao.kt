/*
 * Copyright 2018 Rutger Broekhoff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.viasalix.horarium.data.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import nl.viasalix.horarium.data.AppointmentCustomizations
import nl.viasalix.horarium.data.zermelo.model.Appointment
import java.util.*

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM `appointment`")
    fun getAll(): LiveData<List<Appointment>>

    @Query("SELECT * FROM `appointment` WHERE `start` >= :from AND `end` <= :till")
    fun getAppointmentsFromTill(from: Date, till: Date): LiveData<List<Appointment>>

    @Query("SELECT * FROM `appointment` WHERE `teachers` LIKE '%' || :teacher || '%'")
    fun getAppointmentsByTeacher(teacher: String): LiveData<List<Appointment>>

    @Query("DELETE FROM `appointment` WHERE `start` >= :from AND `end` <= :till")
    fun deleteAppointmentsFromTill(from: Date, till: Date)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppointment(appointment: Appointment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppointments(appointments: List<Appointment>)

    @Query("UPDATE `appointment` SET `customizations` = :appointmentCustomizations WHERE `appointment_instance` = :appointmentInstance")
    fun updateAppointmentCustomizations(appointmentInstance: Long, appointmentCustomizations: AppointmentCustomizations)

    @Delete
    fun deleteAppointments(appointments: List<Appointment>)
}
