package ru.koy.service.impl

import ru.koy.exception.InvalidParamsException
import ru.koy.exception.UserNotFoundException
import ru.koy.model.dto.SaltResponse
import ru.koy.repository.UserRepository
import ru.koy.service.Service
import java.nio.file.attribute.UserPrincipalNotFoundException
import kotlin.reflect.KClass

class AuthService(userRepository: UserRepository) : Service {
    private val userRepository = userRepository

    suspend fun getSaltByUsername(params: List<Any>?): SaltResponse {
        this.checkParams(params, String::class)
        val userByName = userRepository.getUserByName(params?.first().toString())?: throw UserNotFoundException()
        return SaltResponse(userByName.salt, userByName.iteration)
    }

    fun authenticate(params: List<Any>?): Double? {
        this.checkParams(params, Double::class)
        println(params)
        return params?.first() as Double
    }

    private fun checkParams(params: List<Any>?, kClass: KClass<out Any>) {
        if (params.isNullOrEmpty() || !kClass.isInstance(params.first())) {
            throw InvalidParamsException()
        }
    }
}