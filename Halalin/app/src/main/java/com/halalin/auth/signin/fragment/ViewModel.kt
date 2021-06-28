package com.halalin.auth.signin.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halalin.auth.repository.AuthRepository
import com.halalin.auth.repository.FirebaseAuthRepository
import com.halalin.util.Event
import com.halalin.util.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val authRepository: AuthRepository = FirebaseAuthRepository

    enum class SignInMessage {
        FAILURE, INVALID_CREDENTIALS, LOADING, SUCCESS
    }

    private val _signInMessage = MutableLiveData<Event<SignInMessage>>()
    val signInMessage: LiveData<Event<SignInMessage>> = _signInMessage

    fun signIn(emailAddress: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        _signInMessage.postValue(Event(SignInMessage.LOADING))
        val message = try {
            authRepository.signIn(emailAddress, password)
            SignInMessage.SUCCESS
        } catch (e: Exception) {
            loge("Sign in failed", e)
            when (e) {
                is AuthRepository.InvalidCredentialsException -> SignInMessage.INVALID_CREDENTIALS
                else -> SignInMessage.FAILURE
            }
        }
        _signInMessage.postValue(Event(message))
    }

    fun signInAsGuest() = viewModelScope.launch(Dispatchers.IO) {
        _signInMessage.postValue(Event(SignInMessage.LOADING))
        val message = try {
            authRepository.signInAsGuest()
            SignInMessage.SUCCESS
        } catch (e: Exception) {
            loge("Sign in as guest failed", e)
            SignInMessage.FAILURE
        }
        _signInMessage.postValue(Event(message))
    }
}
