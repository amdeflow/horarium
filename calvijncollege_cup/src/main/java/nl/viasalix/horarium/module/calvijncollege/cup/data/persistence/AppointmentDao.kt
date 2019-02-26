package nl.viasalix.horarium.module.calvijncollege.cup.data.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import nl.viasalix.horarium.data.zermelo.model.Appointment

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM `appointment`")
    fun getAll(): LiveData<List<Appointment>>
}
