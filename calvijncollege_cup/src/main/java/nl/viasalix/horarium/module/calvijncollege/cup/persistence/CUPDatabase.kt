package nl.viasalix.horarium.module.calvijncollege.cup.persistence

import nl.viasalix.horarium.module.calvijncollege.cup.data.Appointment
import nl.viasalix.horarium.utils.DatabaseUtils.formatModuleDatabaseName

@Database(entities = [Appointment::class, version = 1)
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
                    formatModuleDatabaseName("CUP")
            ).build()
        }

    }
}

