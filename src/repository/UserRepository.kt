package ru.koy.repository

import org.jetbrains.exposed.sql.*
import ru.koy.config.DatabaseConfiguration.dbQuery
import ru.koy.model.ddl.Users
import ru.koy.model.orm.User

class UserRepository {
    suspend fun getUserByName(userName: String): User? = dbQuery {
        Users.select { (Users.name eq userName) }
            .mapNotNull { toUser(it) }
            .singleOrNull()
    }

    suspend fun getUserByToken(token: String): User? = dbQuery {
        Users.select { Users.lastToken.eq(token) and Users.storedKey.isNotNull() }
            .mapNotNull { toUser(it) }
            .singleOrNull()
    }

    suspend fun insertUser(userName: String, newSalt: ByteArray, i: Int, token: String) = dbQuery {
        Users.insert {
            it[name] = userName
            it[salt] = newSalt
            it[iteration] = i
            it[lastToken] = token
        }
    }

    suspend fun updateUser(userByName: User, newSalt: ByteArray, newIterations: Int, token: String) = dbQuery {
        Users.update({ Users.id.eq(userByName.id) }) {
            it[salt] = newSalt
            it[iteration] = newIterations
            it[lastToken] = token
        }
    }

    suspend fun checkUserExists(userName: String): Boolean = dbQuery {
        Users.select { Users.name.eq(userName) and Users.storedKey.isNotNull() }
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
