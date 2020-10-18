package ru.koy.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.koy.exception.InvalidParamsException
import ru.koy.exception.NonceNotFoundException
import ru.koy.exception.UserAlreadyExistsException
import ru.koy.exception.UserNotFoundException
import ru.koy.model.dto.SaltResponse
import ru.koy.repository.UserRepository
import ru.koy.service.Service
import ru.koy.util.ScramUtil
import java.time.LocalTime
import kotlin.random.Random

private val logger: Logger = LoggerFactory.getLogger("ru.koy.service.AuthService")

class AuthService(private val userRepository: UserRepository) : Service {

    suspend fun getSaltByUsername(params: List<Any>?): SaltResponse {
        this.checkSaltParams(params)

        val userByName = userRepository
            .getUserByName(params?.first() as String) ?: throw UserNotFoundException()

        val lastToken = params[1] as String + ScramUtil.toBase64(ScramUtil.generateSalt())

        userRepository.updateLastToken(userByName.id, lastToken)

        return SaltResponse(
            ScramUtil.toBase64(userByName.salt),
            userByName.iteration,
            lastToken
        )
    }

    suspend fun authenticate(params: List<Any>?): String {
        this.checkAuthenticateParams(params)
        val user = userRepository.getUserByToken(params?.get(0) as String) ?: throw UserNotFoundException()
        val clientNonce = user.lastToken?.substring(0..23) ?: throw NonceNotFoundException()

        val authMessage = ScramUtil.getAuthMessage(
            ScramUtil.getClientFirstMessage(user.name, clientNonce),
            ScramUtil.getServerFirstMessage(user.lastToken, ScramUtil.toBase64(user.salt), user.iteration),
            ScramUtil.getClientFinalMessageWithoutProof(user.lastToken)
        )

        val clientSignature = ScramUtil.clientSignature(user.storedKey, authMessage)
        val storedKey = ScramUtil.getStoredKey(clientSignature, ScramUtil.fromBase64(params[1] as String))

        val compare = String.format(
            "Expected: %s, actual: %s",
            ScramUtil.toBase64(user.storedKey), ScramUtil.toBase64(storedKey)
        )

        logger.debug("authenticate: {}", compare)

        return if (storedKey.contentEquals(user.storedKey)) {
            "Success"
        } else {
            "Fail"
        }
    }

    suspend fun registration(params: List<Any>?): String {
        this.checkRegistrationParams(params)
        if (userRepository.checkUser(params?.get(0) as String)) {
            throw UserAlreadyExistsException()
        }

        val credentials = ScramUtil.getScramCredentials(
            params[1] as String,
            Random(LocalTime.now().toNanoOfDay())
                .nextInt(4096, 1048576)
        )

        userRepository.insertUser(params[0] as String, credentials)
        return "Success"
    }

    private fun checkSaltParams(params: List<Any>?) {
        if (params?.size != 2 || params.any { !String::class.isInstance(it) }) {
            throw InvalidParamsException()
        }
    }

    private fun checkRegistrationParams(params: List<Any>?) {
        if (params?.size != 2 || params.any { !String::class.isInstance(it) }) {
            throw InvalidParamsException()
        }
    }

    private fun checkAuthenticateParams(params: List<Any>?) {
        if (params?.size != 2 || params.any { !String::class.isInstance(it) }) {
            throw InvalidParamsException()
        }
    }
}
