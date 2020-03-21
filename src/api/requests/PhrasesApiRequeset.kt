package com.raywenderlich.api.requests

import kotlinx.serialization.Serializable

@Serializable
data class PhrasesApiRequeset(
    val emoji: String,
    val phrase: String
)