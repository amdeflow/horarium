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
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nl.viasalix.horarium.data.zermelo.model.Announcement

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM `announcement`")
    fun getAll(): LiveData<List<Announcement>>

    @Query("SELECT * FROM `announcement` WHERE `id` IN (:ids)")
    fun loadAllById(ids: List<Int>): LiveData<List<Announcement>>

    @Query("SELECT * FROM `announcement` WHERE `text` LIKE :text")
    fun loadAllByText(text: String): LiveData<List<Announcement>>

    @Query("SELECT * FROM `announcement` WHERE `start` >= :from AND `end` <= :till")
    fun getAnnouncementsFromTill(from: Long, till: Long): LiveData<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(announcement: Announcement)
}
