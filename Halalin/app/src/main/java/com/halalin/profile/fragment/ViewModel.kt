package com.halalin.profile.fragment

import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import com.halalin.auth.model.User
import com.halalin.auth.repository.AuthRepository
import com.halalin.auth.repository.FirebaseAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val authRepository: AuthRepository = FirebaseAuthRepository

    val user: LiveData<User?> = authRepository.authState.switchMap {
        liveData { emit(authRepository.user) }
    }

    fun signOut() = viewModelScope.launch(Dispatchers.IO) {
        authRepository.signOut()
    }
}
