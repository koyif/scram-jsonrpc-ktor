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
import ru.koy.util.*
import java.time.LocalTime
import kotlin.random.Random

private val logger: Logger = LoggerFactory.getLogger("ru.koy.service.AuthService")

class AuthService(private val userRepository: UserRepository) : Service {

    suspend fun getSaltByUsername(params: List<Any>?): SaltResponse {
        this.checkSaltParams(params)

        val userByName = userRepository
            .getUserByName(params?.first() as String) ?: throw UserNotFoundException()

        val lastToken = params[1] as String + toBase64(generateSalt())

        userRepository.updateLastToken(userByName.id, lastToken)

        return SaltResponse(
            toBase64(userByName.salt),
            userByName.iteration,
            lastToken
        )
    }

    suspend fun authenticate(params: List<Any>?): String {
        this.checkAuthenticateParams(params)
        val user = userRepository.getUserByToken(params?.get(0) as String) ?: throw UserNotFoundException()
        val clientNonce = user.lastToken?.substring(0..23) ?: throw NonceNotFoundException()

        val authMessage = getAuthMessage(
            getClientFirstMessage(user.name, clientNonce),
            getServerFirstMessage(user.lastToken, toBase64(user.salt), user.iteration),
            getClientFinalMessageWithoutProof(user.lastToken)
        )

        val clientSignature = clientSignature(user.storedKey!!, authMessage)
        val storedKey = getStoredKey(clientSignature, fromBase64(params[1] as String))

        val compare = String.format(
            "Expected: %s, actual: %s",
            toBase64(user.storedKey), toBase64(storedKey)
        )

        logger.debug("authenticate: {}", compare)

        return if (storedKey.contentEquals(user.storedKey)) {
            "Success"
        } else {
            "Fail"
        }
    }

    suspend fun registration(params: List<Any>?): SaltResponse {
        this.checkRegistrationParams(params)
        val (username, clientToken) = params!![0] as String to params[1] as String

        val userByName = userRepository.getUserByName(username)

        if (userByName?.serverKey?.isNotEmpty() == true) {
            throw UserAlreadyExistsException()
        }

        val newSalt = generateSalt()
        val newIterations = Random(LocalTime.now().toNanoOfDay())
            .nextInt(4096, 131072)
        val token = clientToken + toBase64(generateSalt())

        if (userByName != null) {
            userRepository.updateUser(userByName, newSalt, newIterations, token)
        } else {
            userRepository.insertUser(username, newSalt, newIterations, token)
        }

        return SaltResponse(
            toBase64(newSalt),
            newIterations,
            token
        )
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
