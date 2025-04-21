package com.passmanager

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.passmanager.database.*
import com.passmanager.models.*
import com.passmanager.utils.DatabaseFactory
import com.passmanager.utils.SecurityUtils
import com.passmanager.exceptions.*
import com.passmanager.config.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val config = environment.config
    DatabaseFactory.init(config)

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    install(Authentication) {
        jwt {
            val jwtAudience = config.property("ktor.security.jwt.audience").getString()
            realm = config.property("ktor.security.jwt.realm").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(config.property("ktor.security.jwt.secret").getString()))
                    .withAudience(jwtAudience)
                    .withIssuer(config.property("ktor.security.jwt.issuer").getString())
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    configureSwagger()

    routing {
        // Auth routes
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val user = DatabaseFactory.dbQuery {
                Users.select { Users.username eq request.username }.firstOrNull()
            }
            if (user != null) {
                call.respond(HttpStatusCode.Conflict, "Username already exists")
                return@post
            }

            val hashedPassword = SecurityUtils.hashPassword(request.masterPassword)
            val userId = DatabaseFactory.dbQuery {
                Users.insert {
                    it[username] = request.username
                    it[masterPassword] = hashedPassword
                    it[createdAt] = Instant.now()
                } get Users.id
            }

            // Create default settings for user
            DatabaseFactory.dbQuery {
                com.passmanager.database.UserSettings.insert {
                    it[com.passmanager.database.UserSettings.userId] = userId
                    it[com.passmanager.database.UserSettings.autoLogoutMinutes] = 15
                    it[com.passmanager.database.UserSettings.updatedAt] = Instant.now()
                }
            }

            call.respond(HttpStatusCode.Created, mapOf("id" to userId.value))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = DatabaseFactory.dbQuery {
                Users.select { Users.username eq request.username }.firstOrNull()
            }

            if (user == null || !SecurityUtils.verifyPassword(request.password, user[Users.masterPassword])) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            val token = generateToken(user[Users.id].value, config)
            call.respond(mapOf("token" to token))
        }

        authenticate {
            // Password entries routes
            get("/passwords") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val entries = DatabaseFactory.dbQuery {
                    PasswordEntries.select { PasswordEntries.userId eq userId }
                        .map {
                            PasswordEntry(
                                id = it[PasswordEntries.id].value,
                                userId = it[PasswordEntries.userId].value,
                                resourceName = it[PasswordEntries.resourceName],
                                username = it[PasswordEntries.username],
                                password = it[PasswordEntries.encryptedPassword],
                                notes = it[PasswordEntries.notes],
                                createdAt = it[PasswordEntries.createdAt],
                                updatedAt = it[PasswordEntries.updatedAt]
                            )
                        }
                }
                call.respond(entries)
            }

            get("/passwords/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val entryId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")

                val entry = DatabaseFactory.dbQuery {
                    PasswordEntries.select { 
                        (PasswordEntries.id eq entryId) and (PasswordEntries.userId eq userId) 
                    }.firstOrNull()
                } ?: throw NotFoundException("Password entry not found")

                call.respond(
                    PasswordEntry(
                        id = entry[PasswordEntries.id].value,
                        userId = entry[PasswordEntries.userId].value,
                        resourceName = entry[PasswordEntries.resourceName],
                        username = entry[PasswordEntries.username],
                        password = entry[PasswordEntries.encryptedPassword],
                        notes = entry[PasswordEntries.notes],
                        createdAt = entry[PasswordEntries.createdAt],
                        updatedAt = entry[PasswordEntries.updatedAt]
                    )
                )
            }

            post("/passwords") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val request = call.receive<PasswordEntryRequest>()

                val encryptedPassword = SecurityUtils.encryptPassword(request.password, request.password)

                val entryId = DatabaseFactory.dbQuery {
                    PasswordEntries.insert {
                        it[PasswordEntries.userId] = userId
                        it[resourceName] = request.resourceName
                        it[username] = request.username
                        it[PasswordEntries.encryptedPassword] = encryptedPassword
                        it[notes] = request.notes
                        it[createdAt] = Instant.now()
                        it[updatedAt] = Instant.now()
                    } get PasswordEntries.id
                }

                call.respond(HttpStatusCode.Created, mapOf("id" to entryId.value))
            }

            put("/passwords/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val entryId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")
                val request = call.receive<PasswordEntryRequest>()

                val encryptedPassword = SecurityUtils.encryptPassword(request.password, request.password)

                val updated = DatabaseFactory.dbQuery {
                    PasswordEntries.update({ 
                        (PasswordEntries.id eq entryId) and (PasswordEntries.userId eq userId) 
                    }) {
                        it[resourceName] = request.resourceName
                        it[username] = request.username
                        it[PasswordEntries.encryptedPassword] = encryptedPassword
                        it[notes] = request.notes
                        it[updatedAt] = Instant.now()
                    }
                }

                if (updated == 0) {
                    throw NotFoundException("Password entry not found")
                }

                call.respond(HttpStatusCode.OK)
            }

            delete("/passwords/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val entryId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid ID")

                val deleted = DatabaseFactory.dbQuery {
                    PasswordEntries.deleteWhere { 
                        (PasswordEntries.id eq entryId) and (PasswordEntries.userId eq userId) 
                    }
                }

                if (deleted == 0) {
                    throw NotFoundException("Password entry not found")
                }

                call.respond(HttpStatusCode.OK)
            }

            put("/change-master-password") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()
                val request = call.receive<ChangeMasterPasswordRequest>()

                val user = DatabaseFactory.dbQuery {
                    Users.select { Users.id eq userId }.firstOrNull()
                } ?: throw NotFoundException("User not found")

                if (!SecurityUtils.verifyPassword(request.oldMasterPassword, user[Users.masterPassword])) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid current password")
                    return@put
                }

                val newHashedPassword = SecurityUtils.hashPassword(request.newMasterPassword)
                DatabaseFactory.dbQuery {
                    Users.update({ Users.id eq userId }) {
                        it[masterPassword] = newHashedPassword
                    }
                }

                call.respond(HttpStatusCode.OK)
            }

            // Settings routes
            get("/settings") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asInt() ?: throw UnauthorizedException()
                val settings = DatabaseFactory.dbQuery {
                    com.passmanager.database.UserSettings.select { com.passmanager.database.UserSettings.userId eq userId }
                        .map { row ->
                            com.passmanager.models.UserSettings(
                                id = row[com.passmanager.database.UserSettings.id].value,
                                userId = row[com.passmanager.database.UserSettings.userId].value,
                                autoLogoutMinutes = row[com.passmanager.database.UserSettings.autoLogoutMinutes],
                                updatedAt = row[com.passmanager.database.UserSettings.updatedAt]
                            )
                        }
                        .firstOrNull()
                } ?: throw NotFoundException("Settings not found")
                call.respond(settings)
            }

            put("/settings") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asInt() ?: throw UnauthorizedException()
                val request = call.receive<SettingsUpdateRequest>()
                DatabaseFactory.dbQuery {
                    com.passmanager.database.UserSettings.update({ com.passmanager.database.UserSettings.userId eq userId }) {
                        it[com.passmanager.database.UserSettings.autoLogoutMinutes] = request.autoLogoutMinutes
                        it[com.passmanager.database.UserSettings.updatedAt] = Instant.now()
                    }
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

private fun generateToken(userId: Int, config: ApplicationConfig): String {
    val jwtLifetimeMinutes = config.propertyOrNull("ktor.security.jwt.lifetimeMinutes")?.getString()?.toIntOrNull() ?: 60
    val token = JWT.create()
        .withAudience(config.property("ktor.security.jwt.audience").getString())
        .withIssuer(config.property("ktor.security.jwt.issuer").getString())
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + jwtLifetimeMinutes * 60 * 1000))
        .sign(Algorithm.HMAC256(config.property("ktor.security.jwt.secret").getString()))
    return token
} 