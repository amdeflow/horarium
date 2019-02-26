package nl.viasalix.horarium.utils

object DatabaseUtils {
    fun formatDatabaseName(user: String) = "horarium-db_$user"
    fun formatModuleDatabaseName(user: String, moduleName: String) = "horarium-db_${user}_mod_$moduleName"
}
