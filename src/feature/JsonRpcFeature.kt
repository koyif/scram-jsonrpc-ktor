package ru.koy.feature

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.koy.exception.ProcedureNotFoundException
import ru.koy.exception.RpcException
import ru.koy.repository.UserRepository
import ru.koy.service.JsonRpcService
import ru.koy.service.impl.AuthService
import ru.koy.service.impl.HelloService

class JsonRpcFeature(configuration: Configuration) {
    private val logger: Logger = LoggerFactory.getLogger("ru.koy.feature.JsonRpcFeature")

    private val jsonRpcService = JsonRpcService()
    private val authService = AuthService(UserRepository())
    private val helloService = HelloService()

    class Configuration {

    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, JsonRpcFeature> {
        override val key = AttributeKey<JsonRpcFeature>("JsonRpcFeature")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): JsonRpcFeature {
            val configuration = Configuration().apply(configure)
            val feature = JsonRpcFeature(configuration)

            pipeline.intercept(ApplicationCallPipeline.Call) {
                feature.intercept(this)
            }
            return feature
        }
    }

    private suspend fun intercept(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val call = pipelineContext.call
        val jsonRequest = jsonRpcService.getRpcRequest(call) ?: return

        logger.debug("jsonRequest - method: {} , params: {}", jsonRequest.method, jsonRequest.params)

        var result: Any? = null

        try {
            result = when (jsonRequest.method) {
                "AuthService.registration" -> authService.registration(jsonRequest.params)
                "AuthService.getSaltByUsername" -> authService.getSaltByUsername(jsonRequest.params)
                "AuthService.authenticate" -> authService.authenticate(jsonRequest.params)
                "HelloService.sayHello" -> helloService.sayHello()
                else -> throw ProcedureNotFoundException()
            }
        } catch (ex: RpcException) {
            call.respondText(
                ContentType.Application.Json,
                HttpStatusCode.OK
            ) { JsonRpcService.getErrorResponse(ex.code, ex.message, jsonRequest.id) }
            return
        }

        if (jsonRequest.id != null) {
            call.respondText(
                ContentType.Application.Json,
                HttpStatusCode.OK
            ) { JsonRpcService.getResultResponse(result, jsonRequest.id) }
            return
        }
    }
}