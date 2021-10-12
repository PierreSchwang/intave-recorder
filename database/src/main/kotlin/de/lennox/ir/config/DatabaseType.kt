package de.lennox.ir.config

import de.lennox.ir.Driver
import de.lennox.ir.mongodb.MongoDriver
import de.lennox.ir.sql.mysql.MySqlDriver

enum class DatabaseType(val driverFunction: (config: DatabaseConfig) -> Driver) {

    MySQL({ config ->
        MySqlDriver(
            config.mySqlConfig.host,
            config.mySqlConfig.port,
            config.mySqlConfig.database,
            config.mySqlConfig.username,
            config.mySqlConfig.password
        )
    }),
    MongoDB({ config ->
        var credential: com.mongodb.MongoCredential? = null
        if (config.mongoDBConfig.authenticationNeeded) {
            credential = com.mongodb.MongoCredential.createCredential(
                config.mongoDBConfig.username,
                config.mongoDBConfig.authDb,
                config.mongoDBConfig.password.toCharArray()
            )
        }
        MongoDriver(
            config.mongoDBConfig.ip,
            config.mongoDBConfig.port,
            credential
        )
    }),
    SQLite({ config ->
        de.lennox.ir.sql.sqlite.SqliteDriver(java.io.File(config.sqliteConfig.file))
    });

    companion object {
        fun byConfigValue(config: String): DatabaseType? {
            for (value in values()) {
                if (value.name.equals(config, true)) {
                    return value
                }
            }
            return null
        }
    }

}