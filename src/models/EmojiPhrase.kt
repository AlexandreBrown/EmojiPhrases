package com.raywenderlich.models

import kotlinx.serialization.Serializable
import org.h2.table.Column
import org.jetbrains.exposed.dao.IntIdTable

@Serializable
data class EmojiPhrase(
    val id: Int,
    val userId: String,
    val emoji: String,
    val phrase: String
): java.io.Serializable

object EmojiPhrases: IntIdTable() {

    val user = varchar("user_id", 20).index()

    val emoji = varchar("emoji", 255)

    val phrase= varchar("phrase", 255)

}