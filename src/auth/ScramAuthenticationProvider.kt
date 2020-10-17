package ru.koy.auth

import io.ktor.auth.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val Logger: Logger = LoggerFactory.getLogger("ru.koy.auth.ScramAuthenticationProvider")

class ScramAuthenticationProvider internal constructor(config: Configuration) : AuthenticationProvider(config) {
    class Configuration internal constructor(name: String?) : AuthenticationProvider.Configuration(name) {
        internal fun build() = ScramAuthenticationProvider(this)
    }
}

/**
 * Installs Scram Authentication mechanism
 */
fun Authentication.Configuration.scram(
    name: String? = null,
    configure: ScramAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = ScramAuthenticationProvider.Configuration(name).apply(configure).build()
    provider.authenticate()
    provider.getSalt()
    register(provider)
}

internal fun ScramAuthenticationProvider.getSalt() {
    pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        Logger.info("getSalt")
        val principal = context.principal
        if (principal != null) {
            return@intercept
        }
    }
}

internal fun ScramAuthenticationProvider.authenticate() {
    pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        Logger.info("authenticate")
        val principal = context.principal
        if (principal != null) {
            return@intercept
        }
    }
}
