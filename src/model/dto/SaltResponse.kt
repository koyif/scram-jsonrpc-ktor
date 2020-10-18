package ru.koy.model.dto

data class SaltResponse(
    val salt: String,
    val i: Int,
    val rand: String
)
