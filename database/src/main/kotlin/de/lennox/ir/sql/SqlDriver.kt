package de.lennox.ir.sql

import de.lennox.ir.Driver
import de.lennox.ir.entity.PasswordEntity
import de.lennox.ir.entity.RecordEntity
import java.sql.Connection

const val BATCH_SIZE = 250

abstract class SqlDriver : Driver {

    abstract fun getConnection(): Connection

    protected fun migrate() {
        getConnection().use { connection ->
            connection.autoCommit = false
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS intave_records (" + "id VARCHAR(8) NOT NULL PRIMARY KEY, uuid VARCHAR(36) NOT NULL, owner VARCHAR(16) NOT NULL)"
            ).executeUpdate()
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS intave_record_logs(" + "id VARCHAR(8) NOT NULL , violation VARCHAR(256))"
            ).executeUpdate()
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS intave_passwords(" + "id VARCHAR(256) NOT NULL, password VARCHAR(50) NOT NULL, fingerprint VARCHAR(128))"
            ).executeUpdate()
            connection.commit()
        }
    }

    override fun pushRecord(recordEntity: RecordEntity) {
        getConnection().use { connection ->
            // Insert every violation as a batch
            connection.prepareStatement(
                "INSERT INTO intave_record_logs (id, violation) VALUES (?, ?)"
            ).use { preparedStatement ->
                var amount = 0
                for (log in recordEntity.logs) {
                    preparedStatement.setString(1, recordEntity.recordId)
                    preparedStatement.setString(2, log)
                    preparedStatement.addBatch()
                    if (++amount % BATCH_SIZE == 0 || amount == recordEntity.logs.size) {
                        preparedStatement.executeBatch()
                    }
                }
            }
            // Insert the record entry which holds the player data as well as the id
            connection.prepareStatement(
                "INSERT INTO intave_records(id, uuid, owner) VALUES (?, ?, ?)"
            ).use { preparedStatement ->
                preparedStatement.setString(1, recordEntity.recordId)
                preparedStatement.setString(2, recordEntity.uuid)
                preparedStatement.setString(3, recordEntity.owner)
                preparedStatement.executeUpdate()
            }
        }
    }

    override fun recordBy(id: String): RecordEntity? {
        getConnection().use { connection ->
            var entity: RecordEntity? = null
            // Fetch the related user data first
            connection.prepareStatement("SELECT * FROM intave_records WHERE id = ?").use { preparedStatement ->
                preparedStatement.setString(1, id)
                preparedStatement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        return entity
                    }
                    entity = RecordEntity(
                        resultSet.getString("id"),
                        mutableListOf(),
                        resultSet.getString("owner"),
                        resultSet.getString("uuid")
                    )
                }
            }

            // Fetch all violations related to the id
            connection.prepareStatement("SELECT violation FROM intave_record_logs WHERE id = ?")
                .use { preparedStatement ->
                    preparedStatement.setString(1, id)
                    preparedStatement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            (entity!!.logs as MutableList).add(resultSet.getString("violation"))
                        }
                    }
                }
            return entity
        }
    }

    override fun pushPassword(passwordEntity: PasswordEntity) {
        getConnection().use { connection ->
            connection.prepareStatement(
                "INSERT INTO intave_passwords (id, password, fingerprint) VALUES (?, ?, ?)"
            ).use { preparedStatement ->
                preparedStatement.setString(1, passwordEntity.passwordId)
                preparedStatement.setString(2, passwordEntity.password)
                preparedStatement.setString(3, passwordEntity.linkedFingerprint)
                preparedStatement.executeUpdate()
            }
        }
    }

    override fun passwordById(id: String): PasswordEntity? {
        getConnection().use { connection ->
            connection.prepareStatement("SELECT * FROM intave_passwords WHERE id = ?").use { preparedStatement ->
                preparedStatement.setString(1, id)
                preparedStatement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        return null
                    }
                    return PasswordEntity(
                        resultSet.getString("id"),
                        resultSet.getString("password"),
                        resultSet.getString("fingerprint")
                    )
                }
            }
        }
    }

    override fun passwordByFingerprint(fingerprint: String): PasswordEntity? {
        getConnection().use { connection ->
            connection.prepareStatement(
                "SELECT * FROM intave_passwords WHERE fingerprint = ?"
            ).use { preparedStatement ->
                preparedStatement.setString(1, fingerprint)
                preparedStatement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        return null
                    }
                    return PasswordEntity(
                        resultSet.getString("id"),
                        resultSet.getString("password"),
                        resultSet.getString("fingerprint")
                    )
                }
            }
        }
    }

    override fun passwordBy(password: String): PasswordEntity? {
        getConnection().use { connection ->
            connection.prepareStatement(
                "SELECT * FROM intave_passwords WHERE password = ?"
            ).use { preparedStatement ->
                preparedStatement.setString(1, password)
                preparedStatement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        return null
                    }
                    return PasswordEntity(
                        resultSet.getString("id"),
                        resultSet.getString("password"),
                        resultSet.getString("fingerprint")
                    )
                }
            }
        }
    }

    override fun deletePasswordBy(id: String): Boolean {
        getConnection().use { connection ->
            connection.prepareStatement(
                "DELETE FROM intave_passwords WHERE id = ?"
            ).use { preparedStatement ->
                preparedStatement.setString(1, id)
                return preparedStatement.executeUpdate() > 0
            }
        }
    }

    override fun updatePasswordFingerprint(id: String, fingerprint: String) {
        getConnection().use { connection ->
            connection.prepareStatement(
                "UPDATE intave_passwords SET fingerprint = ? WHERE id = ?"
            ).use { preparedStatement ->
                preparedStatement.setString(1, fingerprint)
                preparedStatement.setString(2, id)
                preparedStatement.executeUpdate()
            }
        }
    }

}