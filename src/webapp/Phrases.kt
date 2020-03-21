package com.raywenderlich.webapp

import com.raywenderlich.models.EPSession
import com.raywenderlich.models.User
import com.raywenderlich.redirect
import com.raywenderlich.repositories.Repository
import com.raywenderlich.securityCode
import com.raywenderlich.verifyCode
import io.ktor.application.call
import io.ktor.auth.authentication
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

const val PHRASES = "/phrases"

@KtorExperimentalLocationsAPI
@Location(PHRASES)
class Phrases

@KtorExperimentalLocationsAPI
fun Route.phrases(db: Repository, hashFunction: (String) -> String) {
    get<Phrases> {

        val user = call.sessions.get<EPSession>()?.let { db.user(it.userId) }

        if (user == null) {
            call.redirect(Signin())
        } else {
            val phrases = db.phrases()
            val date = System.currentTimeMillis()
            val code = call.securityCode(date, user, hashFunction)
            call.respond(
                FreeMarkerContent(
                    "phrases.ftl", mapOf(
                        "phrases" to phrases,
                        "user" to user,
                        "date" to date,
                        "code" to code
                    ),
                    user.userId
                )
            )
        }
    }
    post<Phrases> {

        val params = call.receiveParameters()

        val user = call.sessions.get<EPSession>()?.let { session -> db.user(session.userId) }

        val date = params["date"]?.toLongOrNull() ?: return@post call.redirect(it)
        val code = params["code"] ?: return@post call.redirect(it)
        val action = params["action"] ?: throw IllegalArgumentException("Missing parameter: action")

        if(user == null || !call.verifyCode(date, user, code, hashFunction)) {
            call.redirect(Signin())
        }

        when (action) {
            "delete" -> {
                val id = params["id"] ?: throw IllegalArgumentException("Missing parameter: id")
                db.remove(id)
            }
            "add" -> {
                val emoji = params["emoji"] ?: throw IllegalArgumentException("Missing parameter: emoji")
                val phrase = params["phrase"] ?: throw IllegalArgumentException("Missing parameter: phrase")

                db.add(user!!.userId, emoji, phrase)
            }
        }

        call.redirect(Phrases())
    }
}