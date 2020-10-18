package ru.koy.exception

class NonceNotFoundException : RpcException() {
    override val code: Int
        get() = 3216

    override val message: String?
        get() = "Previous nonce not found"
}
