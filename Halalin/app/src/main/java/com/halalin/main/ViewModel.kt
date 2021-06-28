package com.halalin.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.halalin.auth.repository.AuthRepository
import com.halalin.auth.repository.FirebaseAuthRepository
import com.halalin.main.app.repository.AppRepository
import com.halalin.main.app.repository.FirebaseAppRepository
import com.halalin.util.Event
import com.halalin.util.loge
import kotlinx.coroutines.Dispatchers

class ViewModel : ViewModel() {
    private val appRepository: AppRepository = FirebaseAppRepository
    private val authRepository: AuthRepository = FirebaseAuthRepository

    val latestAppVersionCode = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        emit(
            try {
                appRepository.getLatestAppVersionCode()
            } catch (e: Exception) {
                loge("Fetch latest app version code failed", e)
                null
            }
        )
    }

    val authState = authRepository.authState.map { Event(it) }
    val isAuthenticated get() = authRepository.isAuthenticated
}
