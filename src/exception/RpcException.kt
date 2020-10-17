package ru.koy.exception

abstract class RpcException : RuntimeException() {
    abstract val code: Int
}
