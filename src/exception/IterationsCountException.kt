package ru.koy.exception

class IterationsCountException : RpcException() {
    override val code: Int
        get() = 12032

    override val message: String
        get() = "Iterations must be between 4096 and 1048576"

}
