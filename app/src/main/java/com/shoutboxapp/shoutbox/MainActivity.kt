package com.shoutboxapp.shoutbox

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Observer
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shoutboxapp.shoutbox.screens.ChatAppScreen
import com.shoutboxapp.shoutbox.screens.NameScreen
import com.shoutboxapp.shoutbox.ui.theme.ShoutboxTheme
import com.shoutboxapp.shoutbox.viewmodels.NameViewModel
import com.shoutboxapp.shoutbox.viewmodels.ShoutsViewModel
import androidx.lifecycle.SavedStateHandle
import com.shoutboxapp.shoutbox.notification.NotificationDbApp
import com.shoutboxapp.shoutbox.notification.NotificationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.messaging.FirebaseMessaging
import com.shoutboxapp.shoutbox.screens.LaunchScreen
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

// TODO: Add code to reconnectWebsocket onResume, and Toast

    private lateinit var shoutsViewModel: ShoutsViewModel
    private lateinit var nameViewModel: NameViewModel
    private lateinit var networkMonitor: NetworkMonitor

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationLauncher = registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
                // Now get the current location
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        PermissionManagerSingleton.init(this)
        val permissionManager = PermissionManagerSingleton.permissionManager
        permissionManager.setLocationLauncher(locationLauncher)
       // WindowCompat.setDecorFitsSystemWindows(window, false)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val nameRepository = (application as NotificationDbApp).repository
        nameViewModel = NameViewModel(nameRepository)
        shoutsViewModel = ShoutsViewModel(savedStateHandle = SavedStateHandle(), NotificationRepository(this),
                                            nameRepository, application)

        var nameOfUser: String? = ""
        runBlocking{
            nameOfUser = nameViewModel.getName()
        }

        Log.d("MainActivity", "Name of User : $nameOfUser")
        networkMonitor = NetworkMonitor(this)
        lifecycle.addObserver(networkMonitor)

        networkMonitor.isConnected.observe(this, Observer { isConnected ->
            if (isConnected) {
               // Toast.makeText(this, "Connected to Internet", Toast.LENGTH_SHORT).show()
                // Network is restored, trigger reconnection logic
            } else {
                Toast.makeText(this, "Connecting back to Internet", Toast.LENGTH_SHORT).show()
                shoutsViewModel.setStatusAsDisconnected()
                shoutsViewModel.reconnectWebSocket()
                // Network is lost, handle the situation if needed
            }
        })

        setContent {
            ShoutboxTheme {
                val nameExists by nameViewModel.nameExists.observeAsState()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                        NavHost(navController, startDestination = "launch") {
                            composable("launch") {
                                LaunchScreen(navController, nameViewModel, nameOfUser)
                            }

                            composable("name") {
                                NameScreen(navController = navController, viewModel = nameViewModel)
                            }
                            composable(
                                "chat/{dataKey}",
                                arguments = listOf(navArgument("dataKey") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                ChatAppScreen(
                                    navController = navController,
                                    viewModel = shoutsViewModel,
                                    nameOfUser
                                )
                            }
                        }
                    }
                }
            }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get the FCM registration token
            val token = task.result
            Log.d("FCM", "FCM Token: $token")
            shoutsViewModel.setFcmToken(token)
            // You can send this token to your backend to register the device
        }

    }

    override fun onResume() {
        super.onResume()
        shoutsViewModel.reconnectWebSocket()
        Log.d("Main", "Resumed APP")
    }

    override fun onPause() {
        super.onPause()
    }
}

