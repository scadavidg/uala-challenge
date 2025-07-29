package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.local.migration.JsonToRoomMigrationService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CacheMigrationViewModel @Inject constructor(
    private val migrationService: JsonToRoomMigrationService
) : ViewModel() {

    val migrationProgress: StateFlow<Float> = migrationService.migrationProgress
    val isMigrationInProgress: StateFlow<Boolean> = migrationService.isMigrationInProgress

    // New state to notify when migration completes
    private val _migrationCompleted = MutableStateFlow(false)
    val migrationCompleted: StateFlow<Boolean> = _migrationCompleted

    fun startMigration() {
        viewModelScope.launch {
            // Reset completion state
            _migrationCompleted.value = false

            // Start migration in background without blocking UI
            migrationService.migrateIfNeeded()

            // After migration completes, wait a bit to ensure UI has time to load data
            delay(500)

            // Set progress to 100% and then hide the indicator
            migrationService.setProgress(100f)
            delay(300) // Show 100% briefly
            migrationService.setMigrationInProgress(false)

            // Notify that migration has completed
            _migrationCompleted.value = true
        }
    }

    // Allow checking if migration is in progress without blocking
    fun isMigrationRunning(): Boolean = isMigrationInProgress.value

    // Reset completion state
    fun resetMigrationCompleted() {
        _migrationCompleted.value = false
    }
}
