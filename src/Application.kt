package ru.koy

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import ru.koy.feature.JsonRpcFeature

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {

    routing {
        install(JsonRpcFeature)
        post("/") {
            call.response.status(HttpStatusCode.OK)
        }

    }
}
