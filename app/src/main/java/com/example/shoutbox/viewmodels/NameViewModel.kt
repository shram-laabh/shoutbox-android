package com.example.shoutbox.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoutbox.repositories.ChatServerRepository
import com.example.shoutbox.screenstates.NameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NameViewModel() : ViewModel() {
    private val _state = MutableStateFlow(NameState())
    val state: StateFlow<NameState> = _state.asStateFlow()

    fun updateName(name: String){
        _state.update { currentState ->
            currentState.copy(name = name)
        }
    }
}