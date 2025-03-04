package com.shoutboxapp.shoutbox.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shoutboxapp.shoutbox.PermissionManager
import com.shoutboxapp.shoutbox.viewmodels.ShoutsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatAppScreen(
    navController: NavController,
    viewModel: ShoutsViewModel,
    nameVar: String?,
) {
    val uiState by viewModel.state.collectAsState()
    var message by remember { mutableStateOf(TextFieldValue("")) }

    val context = LocalContext.current
    val errorMessage by viewModel.errorMessage.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val permissionManager = remember { PermissionManager(context) } // Initialize directly

    val listState = rememberLazyListState()
    var permissionDone by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var nameString by remember { mutableStateOf("$nameVar") }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.chatHistory.size) {
        // Request location and get the latitude and longitude via callback
        if (!permissionDone){
            permissionDone = true
        }
        listState.animateScrollToItem(kotlin.math.max(0,uiState.chatHistory.size - 1))
        val nameFromNavigation = navController.currentBackStackEntry?.arguments?.getString("dataKey")
        if (nameFromNavigation != null){
            nameString = nameFromNavigation
        }
        viewModel.setName(nameString)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Total Nearby Users : ${uiState.nearbyUsersNum}",
                    color = Color.Green) },
                        colors = TopAppBarDefaults.topAppBarColors(Black),
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .imePadding(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = message,
                    onValueChange = { newValue ->
                        if (!newValue.text.contains("\n")) {
                            message = newValue
                        }
                    },
                    placeholder = { Text("Enter a Shout...") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(
                                        kotlin.math.max(
                                            0,
                                            uiState.chatHistory.size - 1
                                        )
                                    )
                                }
                            }
                        },
                    interactionSource = interactionSource,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        val jsonMessage = """{"type" : "chat",
                    |"username": "$nameString", 
                    |"longitude":${longitude},
                    |"latitude":${latitude},
                    |"message": "${message.text}"}""".trimMargin()
                        viewModel.sendMessage(jsonMessage)
                        message = TextFieldValue("")
                    })
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (message.text.isEmpty()){
                        Toast.makeText(context, "Shout is Empty..", Toast.LENGTH_SHORT).show()
                    }else {
                        val jsonMessage = """{"type" : "chat",
                        |"username": "$nameString", 
                        |"longitude":${longitude},
                        |"latitude":${latitude},
                        |"message": "${message.text}"}""".trimMargin()
                        viewModel.sendMessage(jsonMessage)
                        errorMessage?.let { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()  // Reset error state after showing Toast
                        }
                        message = TextFieldValue("")
                    }
                }) {
                    Text("Shout")
                }
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Button(onClick = {
                    showDialog = !showDialog
                }) {
                    Text("Change My Name")
                }
                if (showDialog) {
                    InputDialog(
                        title = "Enter Your Name",
                        onDismiss = { showDialog = false },
                        onConfirm = {
                            if (it.length > 3){
                                nameString = it
                                showDialog = false
                                viewModel.setName(it)
                            }else {
                                Toast.makeText(context, "Name Length needs to be larger than 3 characters", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                // Middle Scrollable Window
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .imeNestedScroll(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    state = listState
                ) {
                    items(uiState.chatHistory) { msg ->
                        Row(
                            horizontalArrangement = Arrangement
                                .SpaceEvenly,
                            verticalAlignment = Alignment
                                .Top
                        ) {
                            MessageBox(msg.user, msg.message, msg.distance, msg.timestamp)
                        }
                        //Divider(thickness = 2.dp)
                    }
                }
                // Bottom Action Buttons
            }
        }
    )
}

@Composable
fun MessageBox(nameString: String, message: String, distance: Double, timestamp: Long) {
    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        ),
        modifier = Modifier
            .background(Black)
    ) {
        Row{
            Column (verticalArrangement = Arrangement
                .SpaceAround){
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ){
                    Text(
                        text = nameString,
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    DistanceDiplay(distance)
                    Spacer(modifier = Modifier.width(16.dp))
                    TimeDisplay(timestamp)
                }
                Divider(thickness = 0.1.dp)
                Row{
                    val gradientColors = listOf(Red, White, Green/*...*/)
                    Text(
                        text = message,
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                colors = gradientColors
                            ),
                            fontSize = 24.sp
                        )
                    )
                }
            }
        }
    }
}
@Composable
fun DistanceDiplay(distance: Double){
    // Yellow -> 1
    // Green  -> 2
    // Blue   -> 3
    // Maroon -> 4
    // Red    -> 5
    var distColor = Color.White.value
    //if (floor(distance) == 1.0)
    var x = ceil(distance)
    when (x){
        1.0 -> distColor = Color.White.value
        2.0 -> distColor = Color.Yellow.value
        3.0 -> distColor = Color.Green.value
        4.0 -> distColor = Color.Cyan.value
        5.0 -> distColor = Color.Magenta.value
        6.0 -> distColor = Color.Red.value
    }

    Text(
        text = "%.2f Km".format(distance),
        style = TextStyle(
            color = Color(distColor),
            fontSize = 20.sp
        )
    )
}
@Composable
fun TimeDisplay(timestamp: Long){
    val sdf = SimpleDateFormat("EEE, HH:mm", Locale.getDefault()) // "EEEE" for day, "HH:mm" for hour and minute
    val formattedDate = sdf.format(Date(timestamp))

    Text(
        text = "$formattedDate",
        style = TextStyle(
            color = Color.Green,
            fontSize = 20.sp,
            textAlign = TextAlign.Right
        ),
    )
}
@Preview(showBackground = true)
@Composable
fun ChatAppScreenPreview() {
    ChatScreen()
}
