package com.shoutboxapp.shoutbox.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.shoutboxapp.shoutbox.notification.NameRepository
import com.shoutboxapp.shoutbox.screenstates.NameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NameViewModel(private val repository: NameRepository) : ViewModel() {
    private val _state = MutableStateFlow(NameState())
    val state: StateFlow<NameState> = _state.asStateFlow()
    val nameExists: LiveData<Boolean> = liveData {
        emit(repository.isNameAvailable()) // Repository function checks DB
    }
    fun setName(name: String) {
        viewModelScope.launch {
            repository.setName(name)
        }
    }

    fun clearName() {
        viewModelScope.launch {
            repository.clearName()
        }
    }

    suspend fun isNameAvailable(): Boolean {
        return repository.isNameAvailable()
    }

    suspend fun getName(): String? {
        return repository.getName()
    }
    fun updateName(name: String){
        _state.update { currentState ->
            currentState.copy(name = name)
        }
        setName(name)
    }
}