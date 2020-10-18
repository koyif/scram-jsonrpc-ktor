package ru.koy.exception

class UserAlreadyExistsException : RpcException() {
    override val code: Int
        get() = 22468

    override val message: String?
        get() = "User already exists"

}
