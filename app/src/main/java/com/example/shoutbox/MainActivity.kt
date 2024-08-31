package com.example.shoutbox

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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

class MainActivity : ComponentActivity() {
// TODO: Add code to reconnectWebsocket onResume, and Toast
    private val shoutsViewModel: ShoutsViewModel by viewModels()
    private val nameViewModel: NameViewModel by viewModels()
    private lateinit var networkMonitor: NetworkMonitor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            //ChatAppScreen(viewModel)
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
                            ChatAppScreen(navController = navController, viewModel = shoutsViewModel, backStackEntry.arguments?.getString("dataKey"))
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        shoutsViewModel.reconnectWebSocket()
        Log.d("Main", "Resumed APP")
    }
}

