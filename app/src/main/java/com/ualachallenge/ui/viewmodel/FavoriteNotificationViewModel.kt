package com.ualachallenge.ui.viewmodel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteNotificationViewModel @Inject constructor() {

    private val _favoriteChangeEvents = MutableSharedFlow<FavoriteChangeEvent>()
    val favoriteChangeEvents: SharedFlow<FavoriteChangeEvent> = _favoriteChangeEvents.asSharedFlow()

    fun notifyFavoriteChange(cityId: Int, isFavorite: Boolean) {
        // Use a simple coroutine scope since this is not a ViewModel
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            _favoriteChangeEvents.emit(FavoriteChangeEvent.Changed(cityId, isFavorite))
        }
    }

    fun notifyFavoriteToggle(cityId: Int) {
        // Use a simple coroutine scope since this is not a ViewModel
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            _favoriteChangeEvents.emit(FavoriteChangeEvent.Toggled(cityId))
        }
    }
}

sealed class FavoriteChangeEvent {
    data class Changed(val cityId: Int, val isFavorite: Boolean) : FavoriteChangeEvent()
    data class Toggled(val cityId: Int) : FavoriteChangeEvent()
} 