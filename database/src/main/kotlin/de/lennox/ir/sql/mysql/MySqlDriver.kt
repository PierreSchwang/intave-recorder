package de.lennox.ir.sql.mysql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.lennox.ir.sql.SqlDriver
import java.sql.Connection

class MySqlDriver(
    host: String, port: Int, database: String, username: String, password: String
) : SqlDriver() {

    private val pool: HikariDataSource

    init {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://$host:$port/$database"
        config.username = username
        config.password = password
        pool = HikariDataSource(config)
        migrate()
    }

    override fun getConnection(): Connection {
        return pool.connection
    }


}