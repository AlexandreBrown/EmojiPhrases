package com.raywenderlich.api

import com.raywenderlich.JwtService
import com.raywenderlich.hash
import com.raywenderlich.redirect
import com.raywenderlich.repositories.Repository
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI

const val LOGIN_ENDPOINT = "/login"

@KtorExperimentalLocationsAPI
@Location(LOGIN_ENDPOINT)
class Login

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.login(db: Repository, jwtService: JwtService) {

    post<Login> {

        val parameters = call.receiveParameters()

        val userId = parameters["userId"] ?: return@post call.redirect(it)
        val password = parameters["password"] ?: return@post call.redirect(it)

        val user = db.user(userId, hash(password))

        if (user != null) {
            val token = jwtService.generateToken(user)
            call.respondText(token)
        }else {
            call.respondText("Invalid user")
        }
    }
}
