package nl.viasalix.horarium.module.calvijncollege.cup.data.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import nl.viasalix.horarium.module.calvijncollege.cup.data.cup.model.Appointment
import nl.viasalix.horarium.utils.DatabaseUtils.formatModuleDatabaseName

@Database(entities = [Appointment::class], version = 1)
abstract class CUPDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao

    companion object {

        @Volatile private var instance: CUPDatabase? = null

        fun getInstance(user: String, context: Context): CUPDatabase {
            return instance?: synchronized(this) {
                instance ?: buildDatabase(user, context).also { instance = it }
            }
        }

        private fun buildDatabase(user: String, context: Context): CUPDatabase {
            return Room.databaseBuilder(
                    context,
                    CUPDatabase::class.java,
                    formatModuleDatabaseName(user, "CUP")
            ).build()
        }

    }
}

