package com.raywenderlich.webapp

import com.raywenderlich.models.EPSession
import com.raywenderlich.repositories.Repository
import io.ktor.application.call
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions

const val HOME = "/"

@KtorExperimentalLocationsAPI
@Location(HOME)
class Home

@KtorExperimentalLocationsAPI
fun Route.home(db: Repository) {

    get<Home> {

        val user = call.sessions.get<EPSession>()?.let { db.user(it.userId) }

        call.respond(FreeMarkerContent("home.ftl", mapOf(
            "user" to user
        )))
    }
}