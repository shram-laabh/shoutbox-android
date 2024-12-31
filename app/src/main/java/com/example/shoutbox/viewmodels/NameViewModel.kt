package com.example.shoutbox.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.shoutbox.notification.NameRepository
import com.example.shoutbox.repositories.ChatServerRepository
import com.example.shoutbox.screenstates.NameState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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