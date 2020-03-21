package com.raywenderlich

import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


const val MIN_USER_ID_LENGTH = 4
const val MIN_PASSWORD_LENGTH = 6

@KtorExperimentalAPI
val hashKey = hex(System.getenv("SECRET_KEY"))

@KtorExperimentalAPI
val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")

@KtorExperimentalAPI
fun hash(password: String): String {

    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)

    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}

private val userIdPattern = "[a-zA-Z0-9_.]+".toRegex()

fun userNameValid(userId: String) = userId.matches(userIdPattern)