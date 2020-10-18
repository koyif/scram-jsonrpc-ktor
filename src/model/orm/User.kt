package ru.koy.model.orm

class User(
    val id: Int,
    val name: String,
    val salt: ByteArray,
    val storedKey: ByteArray,
    val serverKey: ByteArray,
    val iteration: Int,
    val lastToken: String?
)