package com.halalin.auth.signup.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halalin.auth.model.User
import com.halalin.auth.repository.AuthRepository
import com.halalin.auth.repository.FirebaseAuthRepository
import com.halalin.util.Event
import com.halalin.util.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val authRepository: AuthRepository = FirebaseAuthRepository

    enum class SignUpMessage {
        EMAIL_ADDRESS_ALREADY_REGISTERED, EMAIL_ADDRESS_MALFORMED, FAILURE, LOADING, SUCCESS
    }

    private val _signUpMessage = MutableLiveData<Event<SignUpMessage>>()
    val signUpMessage: LiveData<Event<SignUpMessage>> = _signUpMessage

    fun signUp(user: User) = viewModelScope.launch(Dispatchers.IO) {
        _signUpMessage.postValue(Event(SignUpMessage.LOADING))
        val message = try {
            authRepository.signUp(user)
            SignUpMessage.SUCCESS
        } catch (e: Exception) {
            loge("Sign up failed", e)
            when (e) {
                is AuthRepository.MalformedEmailAddressException -> SignUpMessage.EMAIL_ADDRESS_MALFORMED
                is AuthRepository.EmailAddressCollisionException -> SignUpMessage.EMAIL_ADDRESS_ALREADY_REGISTERED
                else -> SignUpMessage.FAILURE
            }
        }
        _signUpMessage.postValue(Event(message))
    }
}
