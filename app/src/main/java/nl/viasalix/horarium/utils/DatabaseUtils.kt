package nl.viasalix.horarium.utils

object DatabaseUtils {
    fun formatDatabaseName(user: String) = "horarium-db_$user"
}