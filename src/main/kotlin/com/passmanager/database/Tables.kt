package com.passmanager.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : IntIdTable() {
    val username = varchar("username", 50).uniqueIndex()
    val masterPassword = varchar("master_password", 255)
    val encryptedMasterPassword = varchar("encrypted_master_password", 255)
    val createdAt = timestamp("created_at")
}

object PasswordEntries : IntIdTable() {
    val userId = reference("user_id", Users)
    val resourceName = varchar("resource_name", 255)
    val username = varchar("username", 255)
    val encryptedPassword = varchar("encrypted_password", 1000)
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

object UserSettings : IntIdTable() {
    val userId = reference("user_id", Users).uniqueIndex()
    val autoLogoutMinutes = integer("auto_logout_minutes").default(15)
    val updatedAt = timestamp("updated_at")
} 