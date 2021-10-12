package de.lennox.ir.config

import de.lennox.ir.web.json.DelegateJsonConfig
import java.io.File

inline fun <reified T> jsonConfig(configPath: File, default: T): DelegateJsonConfig<T> =
    DelegateJsonConfig(configPath, T::class.java, default)

class Config(
    file: File
) {

    var config: DatabaseConfig by jsonConfig(
        file, DatabaseConfig(
            "sqlite",
            MongoDBConfig(
                "127.0.0.1",
                1234,
                true,
                "user",
                "password",
                "admin"
            ),
            MySqlConfig(
                "127.0.0.1",
                3306,
                "intave",
                "root",
                "password"
            ),
            SqliteConfig(
                "./IntaveRecorder/recorder.db"
            )
        )
    )



}