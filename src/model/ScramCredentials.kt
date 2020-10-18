package ru.koy.model

class ScramCredentials(
    val salt: ByteArray,
    val storedKey: ByteArray,
    val serverKey: ByteArray,
    val iterations: Int
)
