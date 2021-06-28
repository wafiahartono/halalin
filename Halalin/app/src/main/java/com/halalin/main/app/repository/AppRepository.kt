package com.halalin.main.app.repository

interface AppRepository {
    suspend fun getLatestAppVersionCode(): Long?
}
