package com.passmanager.models

import com.passmanager.utils.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import kotlinx.serialization.Contextual

@Serializable
data class User(
    val id: Int? = null,
    val username: String,
    val masterPassword: String,
    @Contextual
    val createdAt: Instant = Instant.now()
)

@Serializable
data class PasswordEntry(
    val id: Int,
    val userId: Int,
    val resourceName: String,
    val username: String,
    val password: String,
    val notes: String?,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class UserSettings(
    val id: Int,
    val userId: Int,
    val autoLogoutMinutes: Int,
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val masterPassword: String
)

@Serializable
data class PasswordEntryRequest(
    val resourceName: String,
    val username: String,
    val password: String,
    val masterPassword: String,
    val notes: String? = null
)

@Serializable
data class SettingsUpdateRequest(
    val autoLogoutMinutes: Int
)

@Serializable
data class DecryptPasswordRequest(
    val masterPassword: String
)

@Serializable
data class DecryptedPasswordResponse(
    val password: String
)

@Serializable
data class ChangeMasterPasswordRequest(
    val oldMasterPassword: String,
    val newMasterPassword: String
) 