package com.ualachallenge

import android.app.Application
import com.domain.usecases.InitializeRoomDatabaseUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var initializeRoomDatabaseUseCase: InitializeRoomDatabaseUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize Room database migration in background
        applicationScope.launch {
            initializeRoomDatabaseUseCase()
        }
    }
}
