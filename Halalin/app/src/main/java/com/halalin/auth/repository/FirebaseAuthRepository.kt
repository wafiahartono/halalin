package com.halalin.auth.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.halalin.auth.model.User
import com.halalin.auth.repository.AuthRepository.AuthState
import com.halalin.util.logd
import kotlinx.coroutines.tasks.await

object FirebaseAuthRepository : AuthRepository {
    private val _authState = MutableLiveData<AuthState>()
    override val authState: LiveData<AuthState> get() = _authState

    override val isAuthenticated get() = _authState.value == AuthState.AUTHENTICATED
    override val isGuest get() = _authState.value == AuthState.GUEST

    private var _user: User? = null
    override val user get() = _user

    private val firebaseAuthStateListener = FirebaseAuth.AuthStateListener {
        logd("Firebase auth state changed")
        val firebaseUser = it.currentUser
        _authState.value = when {
            firebaseUser == null -> AuthState.UNAUTHENTICATED
            firebaseUser.isAnonymous -> AuthState.GUEST
            else -> AuthState.AUTHENTICATED
        }
    }

    init {
        logd("init")
        val firebaseUser = Firebase.auth.currentUser
        _authState.value = when {
            firebaseUser == null -> AuthState.UNAUTHENTICATED
            firebaseUser.isAnonymous -> AuthState.GUEST
            else -> AuthState.AUTHENTICATED
        }
        _user = getLocalUserData()
    }

    private fun getLocalUserData(): User? {
        val firebaseUser = Firebase.auth.currentUser
        return when {
            firebaseUser == null -> null
            firebaseUser.isAnonymous -> null
            else -> User(
                firebaseUser.displayName,
                firebaseUser.email,
                firebaseUser.uid,
                profilePictureUrl = firebaseUser.photoUrl?.toString()
            )
        }
    }

    private suspend fun getUserData(): User? {
        val firebaseUser = Firebase.auth.currentUser
        return when {
            firebaseUser == null -> null
            firebaseUser.isAnonymous -> null
            else -> User(
                firebaseUser.displayName,
                firebaseUser.email,
                firebaseUser.uid,
                profilePictureUrl = firebaseUser.photoUrl?.toString()
            )
        }
    }

    override suspend fun signInAsGuest() {
        Firebase.auth.signInAnonymously().await()
        _user = getUserData()
    }

    override suspend fun signOut() {
        Firebase.auth.signOut()
        _user = getUserData()
    }

    override suspend fun signUp(user: User) {
        try {
            Firebase.auth.createUserWithEmailAndPassword(
                user.emailAddress!!, user.password!!
            ).await().user!!.updateProfile(userProfileChangeRequest {
                displayName = user.displayName
                photoUri = Uri.parse(user.profilePictureUrl)
            }).await()
            _user = getUserData()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw AuthRepository.MalformedEmailAddressException()
        } catch (e: FirebaseAuthUserCollisionException) {
            throw AuthRepository.EmailAddressCollisionException()
        }
    }

    override suspend fun signIn(emailAddress: String, password: String) {
        try {
            Firebase.auth.signInWithEmailAndPassword(emailAddress, password).await()
            _user = getUserData()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw AuthRepository.InvalidCredentialsException()
        } catch (e: FirebaseAuthInvalidUserException) {
            throw AuthRepository.InvalidCredentialsException()
        }
    }

    override fun startListening() {
        Firebase.auth.addAuthStateListener(firebaseAuthStateListener)
    }

    override fun stopListening() {
        Firebase.auth.removeAuthStateListener(firebaseAuthStateListener)
    }
}
