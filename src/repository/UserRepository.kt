package ru.koy.repository

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import ru.koy.config.DatabaseConfiguration.dbQuery
import ru.koy.model.ScramCredentials
import ru.koy.model.ddl.Users
import ru.koy.model.orm.User

class UserRepository {
    suspend fun getUserByName(userName: String): User? = dbQuery {
        Users.select { (Users.name eq userName) }
            .mapNotNull { toUser(it) }
            .singleOrNull()
    }

    suspend fun insertUser(userName: String, scramCredentials: ScramCredentials) = dbQuery {
        Users.insert {
            it[name] = userName
            it[salt] = scramCredentials.salt
            it[storedKey] = scramCredentials.storedKey
            it[serverKey] = scramCredentials.serverKey
            it[iteration] = scramCredentials.iterations
        }
    }

    suspend fun checkUser(userName: String): Boolean = dbQuery {
        Users.select { (Users.name eq userName) }
            .any()
    }

    suspend fun updateLastToken(id: Int, token: String) = dbQuery {
        Users.update({ Users.id eq id }) {
            it[lastToken] = token
        }
    }

    private fun toUser(row: ResultRow): User =
        User(
            id = row[Users.id],
            name = row[Users.name],
            salt = row[Users.salt],
            storedKey = row[Users.storedKey],
            serverKey = row[Users.serverKey],
            iteration = row[Users.iteration],
            lastToken = row[Users.lastToken]
        )
}