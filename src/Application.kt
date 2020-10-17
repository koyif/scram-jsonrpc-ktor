package ru.koy

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import ru.koy.auth.scram
import ru.koy.feature.JsonRpcFeature
import ru.koy.service.JsonRpcService
import javax.naming.AuthenticationException

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    install(Sessions) {
        cookie<UserIdPrincipal>(
            "auth", storage = SessionStorageMemory()
        ) {
            cookie.path = "/"
        }
    }
//    install(Authentication) {
//        session<UserIdPrincipal>("session") {
//            challenge {
//                throw AuthenticationException()
//            }
//            validate { session: UserIdPrincipal ->
//                session
//            }
//        }
//        scram("scram") {}
//    }


    routing {
        install(StatusPages) {
            exception<AuthenticationException> {
                call.respond(
                    JsonRpcService.getErrorResponse(
                        HttpStatusCode.Unauthorized.value,
                        HttpStatusCode.Unauthorized.description
                    )
                )
            }
        }
        install(JsonRpcFeature)
        route("/") {
//            authenticate("session", "scram") {
                post {
                    call.response.status(HttpStatusCode.OK)
                }
//            }
        }

    }
}
