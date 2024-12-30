package com.example.shoutbox

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Observer
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shoutbox.screens.ChatAppScreen
import com.example.shoutbox.screens.NameScreen
import com.example.shoutbox.ui.theme.ShoutboxTheme
import com.example.shoutbox.viewmodels.NameViewModel
import com.example.shoutbox.viewmodels.ShoutsViewModel
import androidx.lifecycle.SavedStateHandle
import com.example.shoutbox.notification.NotificationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

// TODO: Add code to reconnectWebsocket onResume, and Toast

    private lateinit var shoutsViewModel: ShoutsViewModel
    private lateinit var nameViewModel: NameViewModel
    private lateinit var networkMonitor: NetworkMonitor

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        shoutsViewModel = ShoutsViewModel(savedStateHandle = SavedStateHandle(), NotificationRepository(this),
                                            application)
        nameViewModel = NameViewModel()
        shoutsViewModel.errorMessage.observe(this) { message ->
            Log.d("Shout", "Toast not working")
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        networkMonitor = NetworkMonitor(this)
        lifecycle.addObserver(networkMonitor)

        networkMonitor.isConnected.observe(this, Observer { isConnected ->
            if (isConnected) {
               // Toast.makeText(this, "Connected to Internet", Toast.LENGTH_SHORT).show()
                // Network is restored, trigger reconnection logic
            } else {
                Toast.makeText(this, "Connecting to back to Internet", Toast.LENGTH_SHORT).show()
                shoutsViewModel.reconnectWebSocket()
                // Network is lost, handle the situation if needed
            }
        })

        setContent {
            ShoutboxTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()

                    NavHost(navController, "name"){
                        composable("name"){
                            NameScreen(navController = navController, viewModel = nameViewModel)
                        }
                        composable("chat/{dataKey}",
                            arguments = listOf(navArgument("dataKey"){type = NavType.StringType})
                        ){ backStackEntry ->
                            ChatAppScreen(navController = navController,
                                viewModel = shoutsViewModel,
                                backStackEntry.arguments?.getString("dataKey"))
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

