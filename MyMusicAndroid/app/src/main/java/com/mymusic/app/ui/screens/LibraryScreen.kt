package com.mymusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mymusic.app.data.Track
import com.mymusic.app.ui.theme.Orange
import com.mymusic.app.viewmodel.MainViewModel

private val TABS = listOf("Playlists", "Artists", "Albums", "Songs", "Genres", "Folders")

@Composable
fun LibraryScreen(viewModel: MainViewModel, nav: NavHostController) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(bottomBar = { MiniPlayerBar(viewModel, nav) }) { padding ->
        Column(Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = Orange, contentColor = Color.White) {
                TABS.forEachIndexed { i, label ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(label) })
                }
            }

            val grouped: Map<String, List<Track>> = when (TABS[selectedTab]) {
                "Artists" -> state.library.groupBy { it.artist }
                "Albums" -> state.library.groupBy { it.album }
                "Genres" -> state.library.groupBy { it.genre ?: "Unknown genre" }
                "Folders" -> state.library.groupBy { it.folder }
                else -> emptyMap()
            }

            when (TABS[selectedTab]) {
                "Songs" -> SongList(state.library.sortedByDescending { it.dateAdded }, viewModel, nav)
                "Playlists" -> PlaylistList(state.library, viewModel, nav)
                else -> GroupedGrid(grouped, viewModel, nav)
            }
        }
    }
}

@Composable
private fun SongList(tracks: List<Track>, viewModel: MainViewModel, nav: NavHostController) {
    LazyColumn {
        items(tracks) { track ->
            TrackRow(track) {
                viewModel.playQueue(tracks, tracks.indexOf(track))
                nav.navigate("nowplaying")
            }
        }
    }
}

@Composable
private fun TrackRow(track: Track, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.albumArtUri, contentDescription = null,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE0E0E0))
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(track.title, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(track.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

@Composable
private fun GroupedGrid(groups: Map<String, List<Track>>, viewModel: MainViewModel, nav: NavHostController) {
    LazyColumn {
        items(groups.keys.sorted()) { key ->
            val tracks = groups[key].orEmpty()
            Row(
                Modifier.fillMaxWidth()
                    .clickable {
                        viewModel.playQueue(tracks, 0)
                        nav.navigate("nowplaying")
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = tracks.firstOrNull()?.albumArtUri, contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFE0E0E0))
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(key, fontWeight = FontWeight.Medium)
                    Text("${tracks.size} songs", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun PlaylistList(library: List<Track>, viewModel: MainViewModel, nav: NavHostController) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn {
            item {
                Text(
                    "Auto Playlists", fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                Row(
                    Modifier.fillMaxWidth()
                        .clickable {
                            val liked = library.filter { state.liked.contains(it.id) }
                            if (liked.isNotEmpty()) { viewModel.playQueue(liked, 0); nav.navigate("nowplaying") }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) { Text("Thumbs up (${state.liked.size})") }
            }
            item {
                Text(
                    "Your Playlists", fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(state.playlists) { playlist ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(playlist.name)
                    TextButton(onClick = { viewModel.deletePlaylist(playlist.id) }) { Text("Delete") }
                }
            }
        }
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = Orange,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) { Text("+", color = Color.White) }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New playlist") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Playlist name") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) viewModel.createPlaylist(name.trim())
                    showCreateDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }
}
