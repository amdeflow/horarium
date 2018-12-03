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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import nl.viasalix.horarium.converters.HorariumTypeConverters
import nl.viasalix.horarium.utils.DatabaseUtils.formatDatabaseName
import nl.viasalix.horarium.zermelo.model.Announcement
import nl.viasalix.horarium.zermelo.model.Appointment
import nl.viasalix.horarium.zermelo.model.ParentTeacherNight

@Database(entities = [Announcement::class, Appointment::class, ParentTeacherNight::class], version = 1)
@TypeConverters(HorariumTypeConverters::class)
abstract class HorariumDatabase : RoomDatabase() {
    abstract fun announcementDao(): AnnouncementDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun parentTeacherNightDao(): ParentTeacherNightDao

    companion object {

        @Volatile private var instance: HorariumDatabase? = null

        fun getInstance(user: String, context: Context): HorariumDatabase {
            return instance?: synchronized(this) {
                instance ?: buildDatabase(user, context).also { instance = it }
            }
        }

        private fun buildDatabase(user: String, context: Context): HorariumDatabase {
            return Room.databaseBuilder(context, HorariumDatabase::class.java,
                    formatDatabaseName(user)).addCallback(object: RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // TODO: get WorkManager up and running
                        }
                    })
                    .build()
        }
    }
}