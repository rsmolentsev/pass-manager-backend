package com.passmanager.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import com.passmanager.database.*

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("ktor.database.driver").getString()
        val jdbcURL = config.property("ktor.database.url").getString()
        val dbUsername = config.property("ktor.database.user").getString()
        val dbPassword = config.property("ktor.database.password").getString()
        val maxPoolSize = config.property("ktor.database.maxPoolSize").getString().toInt()

        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = driverClassName
        hikariConfig.jdbcUrl = jdbcURL
        hikariConfig.username = dbUsername
        hikariConfig.password = dbPassword
        hikariConfig.maximumPoolSize = maxPoolSize
        hikariConfig.isAutoCommit = false
        hikariConfig.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        hikariConfig.validate()

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(Users, PasswordEntries, UserSettings)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
} 