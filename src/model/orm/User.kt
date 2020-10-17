package ru.koy.model.orm

class User(
    val id: Int,
    val name: String,
    val salt: String,
    val storedKey: String,
    val serverKey: String,
    val iteration: Int,
    val dateCreated: Long
)