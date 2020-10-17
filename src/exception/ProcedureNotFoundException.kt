package ru.koy.exception

class ProcedureNotFoundException : RpcException() {
    override val code: Int
        get() = -32601

    override val message: String?
        get() = "Procedure not found."
}