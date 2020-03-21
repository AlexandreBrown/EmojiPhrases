package com.raywenderlich.webapp

import com.raywenderlich.MIN_PASSWORD_LENGTH
import com.raywenderlich.MIN_USER_ID_LENGTH
import com.raywenderlich.models.EPSession
import com.raywenderlich.redirect
import com.raywenderlich.repositories.Repository
import com.raywenderlich.userNameValid
import io.ktor.application.call
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.Parameters
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import kotlin.math.sign

const val SIGNIN = "/signin"

@KtorExperimentalLocationsAPI
@Location(SIGNIN)
data class Signin(
    val userId: String = "",
    val error: String = ""
)

@KtorExperimentalLocationsAPI
fun Route.signin(db: Repository, hashFunction: (String) -> String) {

    get<Signin> {

        val user = call.sessions.get<EPSession>()?.let { db.user(it.userId) }

        if(user != null) {
            call.redirect(Home())
        }else {
            call.respond(FreeMarkerContent("signin.ftl", mapOf(
                "userId" to it.userId,
                "error" to it.error
            )))
        }
    }

    post<Signin> {

        val signInParameters = call.receive<Parameters>()

        val userId = signInParameters["userId"] ?: return@post call.redirect(it)
        val password = signInParameters["password"] ?: return@post call.redirect(it)

        val signInError = Signin(userId)

        val signin = when {
            userId.length < MIN_USER_ID_LENGTH -> null
            password.length < MIN_PASSWORD_LENGTH -> null
            !userNameValid(userId) -> null
            else -> db.user(userId, hashFunction(password))
        }

        if(signin == null){
            call.redirect(signInError.copy(error = "Invalid username or password"))
        } else {
            call.sessions.set(EPSession(signInError.userId))
            call.redirect(Phrases())
        }
    }
}
