package com.raywenderlich.api

import com.raywenderlich.API_VERSION
import com.raywenderlich.api.requests.PhrasesApiRequeset
import com.raywenderlich.apiUser
import com.raywenderlich.repositories.Repository
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.request.receiveChannel
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route

const val PHRASE_API_ENDPOINT = "$API_VERSION/phrases"

@KtorExperimentalLocationsAPI
@Location(PHRASE_API_ENDPOINT)
class PhraseApi

@KtorExperimentalLocationsAPI
fun Route.phrasesApi(db: Repository) {

    authenticate("jwt") {
        get<PhraseApi> {
            call.respond(db.phrases())
        }

        post<PhraseApi> {

            val user = call.apiUser!!

            try {
                val request = call.receive<PhrasesApiRequeset>()

                val phrase = db.add(user.userId, request.emoji, request.phrase)

                if(phrase != null){
                    call.respond(phrase)
                }else {
                    call.respondText("Invalid data received", status = HttpStatusCode.InternalServerError)
                }
            } catch (e: Throwable) {
                call.respondText("Invalid data received ${e.localizedMessage}", status = HttpStatusCode.BadRequest)
            }
        }
    }
}