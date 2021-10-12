package de.lennox.ir.config

import com.google.gson.annotations.SerializedName

data class DatabaseConfig(

    val databaseType: String,

    @SerializedName("mongodb")
    val mongoDBConfig: MongoDBConfig,

    @SerializedName("mysql")
    val mySqlConfig: MySqlConfig,

    @SerializedName("sqlite")
    val sqliteConfig: SqliteConfig

)

data class SqliteConfig(
    @SerializedName("file")
    val file: String
)

data class MySqlConfig(
    @SerializedName("host")
    val host: String,
    @SerializedName("port")
    val port: Int,
    @SerializedName("database")
    val database: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class MongoDBConfig(
    @SerializedName("ip")
    val ip: String,
    @SerializedName("port")
    val port: Int,
    @SerializedName("requires_authentication")
    val authenticationNeeded: Boolean,
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("authentication_db")
    val authDb: String
)