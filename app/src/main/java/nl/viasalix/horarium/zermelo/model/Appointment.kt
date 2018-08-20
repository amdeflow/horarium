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

package nl.viasalix.horarium.zermelo.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import nl.viasalix.horarium.persistence.HorariumTypeConverters

@Entity
data class Appointment(
    // Does not change across versions
    @ColumnInfo(name = "appointment_instance")
    @PrimaryKey
    var appointmentInstance: Long = -1,
    // Basically the same as version, changes when modified - therefore should not be primary key
    var id: Long = -1,
    // Start time in seconds since the UNIX Epoch time
    var start: Long = -1,
    // End time in seconds since the UNIX Epoch time
    var end: Long = -1,
    // Start time slot in the schedule
    @ColumnInfo(name = "start_time_slot")
    var startTimeSlot: Int = -1,
    // End time slot in the timetable
    @ColumnInfo(name = "end_time_slot")
    var endTimeSlot: Int = -1,
    // List of subjects (taught) during the appointment
    @TypeConverters(HorariumTypeConverters::class)
    var subjects: MutableList<String> = mutableListOf(),
    // One of ["unknown", "lesson", "exam", "activity", "choice", "talk", "other"]
    var type: String = "",
    // Speaks for itself
    var remark: String = "",
    // List of locations the appointment (might) take(s) place at
    @TypeConverters(HorariumTypeConverters::class)
    var locations: MutableList<String> = mutableListOf(),
    // List of teachers the appointment (might) (be/is) taught by
    @TypeConverters(HorariumTypeConverters::class)
    var teachers: MutableList<String> = mutableListOf(),
    // List of groups participating in the appointment
    @TypeConverters(HorariumTypeConverters::class)
    var groups: MutableList<String> = mutableListOf(),
    // Time the appointment was first created at
    var created: Long = -1,
    // Time the appointment was last modified at
    @ColumnInfo(name = "last_modified")
    var lastModified: Long = -1,
    // Whether the appointment is still valid or not
    var valid: Boolean = false,
    // Whether the appointment should be hidden to the user (by default) or not
    var hidden: Boolean = false,
    // Whether the appointment is cancelled or not
    var cancelled: Boolean = false,
    // Whether the appointment is modified or not
    var modified: Boolean = false,
    // Whether the appointment is moved or not - not meant for consumption
    var moved: Boolean = false,
    // Whether the appointment was added to the original schedule - not meant for consumption
    var new: Boolean = false,
    // Description of the changes made
    @ColumnInfo(name = "change_description")
    var changeDescription: String = "",
    // Branch of school the appointment belongs to (might be null)
    @ColumnInfo(name = "branch_of_school")
    var branchOfSchool: Long = -1,
    // Branch code of the branchOfSchool
    var branch: String = ""
)