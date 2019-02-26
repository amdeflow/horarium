package nl.viasalix.horarium.utils

object DatabaseUtils {
    fun formatDatabaseName(user: String) = "horarium-db_$user"
    fun formatModuleDatabaseName(moduleName: String) = "horarium-db-mod_$moduleName"
}