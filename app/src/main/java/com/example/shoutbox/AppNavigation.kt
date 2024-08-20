package com.example.shoutbox

import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.R
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.shoutbox.repositories.ChatServerRepository
import com.example.shoutbox.screens.ChatScreen
import com.example.shoutbox.screens.NameScreen
import com.example.shoutbox.viewmodels.ChatViewModel
import com.example.shoutbox.viewmodels.NameViewModel


@Composable
fun AppNavigation(navController: NavHostController){
    val nameView = NameViewModel()
    val chatRepo = ChatServerRepository()
    val chatView = ChatViewModel(chatRepo)
    NavHost(navController, "name"){
        composable("name"){
            NameScreen(navController = navController, viewModel = nameView)
        }
        composable("chat"){
            ChatScreen(navController = navController, viewModel = chatView)
        }
    }
}
