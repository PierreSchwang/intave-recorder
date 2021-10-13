package de.lennox.ir.sql.sqlite

import de.lennox.ir.sql.SqlDriver
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.function.Supplier

class SqliteDriver(
    database: File
) : SqlDriver() {

    private var resolver: Supplier<Connection>

    init {
        if (database.parentFile != null && !database.parentFile.exists()) {
            database.parentFile.mkdirs()
        }
        if (!database.exists()) {
            database.createNewFile()
        }
        Class.forName("org.sqlite.JDBC")
        resolver = Supplier {
            DriverManager.getConnection("jdbc:sqlite:$database")
        }
        migrate()
    }

    override fun getConnection(): Connection {
        return resolver.get()
    }

}