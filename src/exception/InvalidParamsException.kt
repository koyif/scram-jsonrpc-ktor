package ru.koy.exception

class InvalidParamsException : RpcException() {
    override val code: Int
        get() = -32602

    override val message: String?
        get() = "Invalid params"
}
