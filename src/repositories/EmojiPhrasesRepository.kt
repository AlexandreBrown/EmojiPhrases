package com.raywenderlich.repositories

import com.raywenderlich.models.EmojiPhrase
import com.raywenderlich.models.EmojiPhrases
import com.raywenderlich.models.User
import com.raywenderlich.models.Users
import com.raywenderlich.repositories.DataBaseFactory.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException

class EmojiPhrasesRepository : Repository {

    override suspend fun add(userId: String, emojiValue: String, phraseValue: String) =
        dbQuery {
            val insertStatement = EmojiPhrases.insert {
                it[user] = userId
                it[emoji] = emojiValue
                it[phrase] = phraseValue
            }

            val result = insertStatement.resultedValues?.get(0)
            result?.toEmojiPhrase()
        }


    override suspend fun phrase(id: Int): EmojiPhrase? = dbQuery {
        EmojiPhrases.select {
            EmojiPhrases.id eq id
        }.mapNotNull {
            it.toEmojiPhrase()
        }.singleOrNull()
    }

    override suspend fun phrase(id: String): EmojiPhrase? = phrase(id.toInt())

    override suspend fun phrases(): List<EmojiPhrase> = dbQuery {
        EmojiPhrases.selectAll()
            .map {
                it.toEmojiPhrase()
            }
    }

    override suspend fun remove(id: String): Boolean = remove(id.toInt())

    override suspend fun remove(id: Int): Boolean {
        if (phrase(id) == null) {
            throw IllegalArgumentException("No phrase found for id $id.")
        }

        return dbQuery {
            EmojiPhrases.deleteWhere { EmojiPhrases.id eq id } > 0
        }
    }

    override suspend fun clear() {
        EmojiPhrases.deleteAll()
    }

    override suspend fun user(userId: String, hash: String?): User? {
        val user = dbQuery {
            Users.select {
                Users.id eq userId
            }.mapNotNull {
                it.toUser()
            }.singleOrNull()
        }

        return when {
            user == null -> null
            hash == null -> user
            user.passwordHash == hash -> user
            else -> null
        }
    }

    override suspend fun userByEmail(email: String): User? = dbQuery {
        Users.select {
            Users.email eq email
        }.mapNotNull {
            User(
                it[Users.id],
                email,
                it[Users.displayName],
                it[Users.passwordHash]
            )
        }.singleOrNull()
    }

    override suspend fun userById(userId: String): User? = dbQuery {
        Users.select {
            Users.id eq userId
        }.map {
            User(
                userId,
                it[Users.email],
                it[Users.displayName],
                it[Users.passwordHash]
            )
        }.singleOrNull()
    }

    override suspend fun createUser(user: User) {
        dbQuery {
            Users.insert {
                it[id] = user.userId
                it[displayName] = user.displayName
                it[email] = user.email
                it[passwordHash] = user.passwordHash
            }
            Unit
        }
    }

    private fun ResultRow.toEmojiPhrase(): EmojiPhrase =
        EmojiPhrase(
            id = this[EmojiPhrases.id].value,
            userId = this[EmojiPhrases.user],
            emoji = this[EmojiPhrases.emoji],
            phrase = this[EmojiPhrases.phrase]
        )

    private fun ResultRow.toUser(): User =
        User(
            userId = this[Users.id],
            email = this[Users.email],
            displayName = this[Users.displayName],
            passwordHash = this[Users.passwordHash]
        )
}