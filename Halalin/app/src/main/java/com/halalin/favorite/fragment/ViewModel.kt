package com.halalin.favorite.fragment

import androidx.lifecycle.ViewModel
import com.halalin.profile.repository.FirebaseUserRepository
import com.halalin.profile.repository.UserRepository

class ViewModel : ViewModel() {
    private val userRepository: UserRepository = FirebaseUserRepository

    val favoriteVendorList = userRepository.favoriteVendorList
}
