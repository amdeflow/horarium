package nl.viasalix.horarium.module.calvijncollege.cup.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import nl.viasalix.horarium.data.AppointmentCustomizations
import nl.viasalix.horarium.data.zermelo.model.Appointment
import java.util.*

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM `appointment`")
    fun getAll(): LiveData<List<Appointment>>
}
