package ru.koy.repository

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import ru.koy.config.DatabaseConfiguration.dbQuery
import ru.koy.model.ddl.Users
import ru.koy.model.orm.User

class UserRepository {
    suspend fun getUserByName(userName: String): User? = dbQuery {
        Users.select { (Users.name eq userName) }
            .mapNotNull { toUser(it) }
            .singleOrNull()
    }

    private fun toUser(row: ResultRow): User =
        User(
            id = row[Users.id],
            name = row[Users.name],
            salt = row[Users.salt],
            storedKey = row[Users.storedKey],
            serverKey = row[Users.serverKey],
            iteration = row[Users.iteration],
            dateCreated = row[Users.dateCreated]
        )
}