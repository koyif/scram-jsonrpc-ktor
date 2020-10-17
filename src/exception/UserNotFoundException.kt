package ru.koy.exception

class UserNotFoundException: RpcException() {
    override val code: Int
        get() = -1101

    override val message: String?
        get() = "User not found or blocked."
}
