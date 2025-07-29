package com.ualachallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.models.Result
import com.domain.usecases.GetOnlineModeUseCase
import com.domain.usecases.ToggleOnlineModeUseCase
import com.ualachallenge.ui.viewmodel.states.OnlineModeState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CityOnlineModeViewModel @Inject constructor(
    private val toggleOnlineModeUseCase: ToggleOnlineModeUseCase,
    private val getOnlineModeUseCase: GetOnlineModeUseCase
) : ViewModel() {

    private val _onlineModeState = MutableStateFlow<OnlineModeState>(OnlineModeState.Idle)
    val onlineModeState: StateFlow<OnlineModeState> = _onlineModeState.asStateFlow()

    private val _isOnlineMode = MutableStateFlow(false)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    init {
        loadOnlineMode()
    }

    fun loadOnlineMode() {
        viewModelScope.launch {
            when (val result = getOnlineModeUseCase()) {
                is Result.Success -> {
                    _isOnlineMode.update { result.data }
                }
                is Result.Error, is Result.Loading -> {
                    // Keep default value (false)
                }
            }
        }
    }

    fun toggleOnlineMode() {
        val currentMode = _isOnlineMode.value
        _onlineModeState.update { OnlineModeState.Toggling }

        viewModelScope.launch {
            try {
                when (val result = toggleOnlineModeUseCase(!currentMode)) {
                    is Result.Success -> {
                        _isOnlineMode.update { !currentMode }
                        _onlineModeState.update { OnlineModeState.Idle }
                    }
                    is Result.Error -> {
                        _onlineModeState.update { OnlineModeState.Error(result.message) }
                    }
                    is Result.Loading -> {
                        _onlineModeState.update { OnlineModeState.Toggling }
                    }
                }
            } catch (e: Exception) {
                _onlineModeState.update {
                    OnlineModeState.Error("Failed to toggle online mode: ${e.message}")
                }
            }
        }
    }

    fun isToggling(): Boolean = _onlineModeState.value is OnlineModeState.Toggling

    fun getError(): String? {
        return when (val state = _onlineModeState.value) {
            is OnlineModeState.Error -> state.message
            else -> null
        }
    }

    fun clearError() {
        _onlineModeState.update { OnlineModeState.Idle }
    }
}
