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

package nl.viasalix.horarium.persistence

import androidx.room.*
import nl.viasalix.horarium.zermelo.model.Appointment

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM `appointment`")
    fun getAppointments(): List<Appointment>

    @Query("SELECT * FROM `appointment` WHERE `start` >= :from AND `end` <= :till")
    fun getAppointmentsFromTill(from: Long, till: Long): List<Appointment>

    @Query("SELECT * FROM `appointment` WHERE `teachers` LIKE '%' || :teacher || '%'")
    fun getAppointmentsByTeacher(teacher: String): List<Appointment>

    @Query("DELETE FROM `appointment` WHERE `start` >= :from AND `end` <= :till")
    fun deleteAppointmentsFromTill(from: Long, till: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppointment(appointment: Appointment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppointments(appointments: List<Appointment>)

    @Delete
    fun deleteAppointments(appointments: List<Appointment>)
}