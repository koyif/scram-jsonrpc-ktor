package ru.koy.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import ru.koy.model.dto.JsonRpcRequest
import ru.koy.model.dto.JsonRpcResponse

class JsonRpcService {

    suspend fun getRpcRequest(call: ApplicationCall): JsonRpcRequest? {
        val json = call.receive<String>()
        val rpcRequest: JsonRpcRequest?

        try {
            rpcRequest = getRpcRequestFromJson(json)
        } catch (ex: JsonSyntaxException) {
            call.respondText(
                ContentType.Application.Json,
                HttpStatusCode.OK
            ) { getErrorResponse(-32700, "Parse error.") }
            return null
        }

        if (!this.validate(rpcRequest)) {
            call.respondText(
                ContentType.Application.Json,
                HttpStatusCode.OK
            ) { getErrorResponse(-32600, "Invalid JSON-RPC.") }
            return null
        }

        return rpcRequest
    }

    companion object {
        private val gson = Gson()
        fun getErrorResponse(code: Int, message: String?, id: Long? = null): String {
            val error = HashMap<String, Any?>()
            error["code"] = code
            error["message"] = message
            return gson.toJson(JsonRpcResponse(error = error, id = id))
        }

        fun getResultResponse(result: Any?, id: Long): String {
            return gson.toJson(JsonRpcResponse(result = result, id = id))
        }

        fun getRpcRequestFromJson(json: String): JsonRpcRequest? {
            return gson.fromJson(json, JsonRpcRequest::class.java)
        }
    }

    private fun validate(request: JsonRpcRequest?): Boolean {
        return listOf(request, request?.method, request?.jsonrpc)
            .none { it == null } && "2.0" == request?.jsonrpc
    }
}
