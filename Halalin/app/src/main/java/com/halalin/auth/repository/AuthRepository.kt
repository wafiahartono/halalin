package com.halalin.auth.repository

import androidx.lifecycle.LiveData
import com.halalin.auth.model.User
import com.halalin.util.ListenableRepository

interface AuthRepository : ListenableRepository {
    val authState: LiveData<AuthState>

    enum class AuthState {
        AUTHENTICATED, GUEST, UNAUTHENTICATED
    }

    val isAuthenticated: Boolean
    val isGuest: Boolean
    val user: User?

    suspend fun signInAsGuest()
    suspend fun signOut()
    suspend fun signUp(user: User)
    suspend fun signIn(emailAddress: String, password: String)

    class EmailAddressCollisionException : Exception()
    class InvalidCredentialsException : Exception()
    class MalformedEmailAddressException : Exception()
}
