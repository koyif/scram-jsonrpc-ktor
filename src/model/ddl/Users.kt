package ru.koy.model.ddl

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 32).uniqueIndex()
    val salt = binary("salt", 16)
    val storedKey = binary("storedKey", 64)
    val serverKey = binary("serverKey", 64)
    val iteration = integer("iteration")
    val lastToken = varchar("lastToken", 48).nullable()

    override val primaryKey = PrimaryKey(id)
}
