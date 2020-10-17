package ru.koy.model.ddl

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 32)
    val salt = varchar("salt", 64)
    val storedKey = varchar("storedKey", 128)
    val serverKey = varchar("serverKey", 128)
    val iteration = integer("iteration")

    val dateCreated = long("dateCreated")

    override val primaryKey = PrimaryKey(id)
}
