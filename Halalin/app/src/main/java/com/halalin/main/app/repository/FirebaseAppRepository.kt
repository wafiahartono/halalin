package com.halalin.main.app.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FirebaseAppRepository : AppRepository {
    override suspend fun getLatestAppVersionCode() =
        Firebase.firestore.collection("app").document("android")
            .get().await()
            .getLong("version_code")
}
