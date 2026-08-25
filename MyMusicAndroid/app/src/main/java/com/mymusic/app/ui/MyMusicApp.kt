package com.mymusic.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mymusic.app.ui.screens.HomeScreen
import com.mymusic.app.ui.screens.LibraryScreen
import com.mymusic.app.ui.screens.NowPlayingScreen
import com.mymusic.app.ui.theme.Orange
import com.mymusic.app.viewmodel.MainViewModel

@Composable
fun MyMusicApp(viewModel: MainViewModel, onRequestPermission: () -> Unit) {
    val state by viewModel.state.collectAsState()

    if (!state.hasPermission && state.library.isEmpty()) {
        PermissionScreen(onRequestPermission)
        return
    }

    val navController = rememberNavController()
    Box(Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { HomeScreen(viewModel, navController) }
            composable("library") { LibraryScreen(viewModel, navController) }
            composable("nowplaying") { NowPlayingScreen(viewModel, navController) }
        }
    }
}

/** Shown only once, before the permission prompt has been answered - after that, the app
 *  never shows this again, matching "shouldn't ask every time" exactly. */
@Composable
private fun PermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Orange, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(24.dp))
        Text("My Music needs access to your audio files", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "This is asked once, like any other music player. Every song on your device will be found automatically.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequestPermission, colors = ButtonDefaults.buttonColors(containerColor = Orange)) {
            Text("Allow access")
        }
    }
}
