package com.shoutboxapp.shoutbox
/*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shoutboxapp.shoutbox.screens.ChatAppScreen
import com.shoutboxapp.shoutbox.screens.NameScreen


@Composable
fun AppNavigation(navController: NavHostController){
   // val nameView = NameViewModel()
   // val shoutsView = ShoutsViewModel()
    NavHost(navController, "name"){
        composable("name"){
            NameScreen(navController = navController, viewModel = viewModel())
        }
        composable("chat/{dataKey}",
            arguments = listOf(navArgument("dataKey"){type = NavType.StringType})
        ){ backStackEntry ->
            ChatAppScreen(
                navController = navController,
                viewModel = viewModel(),
                backStackEntry.arguments?.getString("dataKey"),
                currentLocation
            )
        }
    }
}
*/