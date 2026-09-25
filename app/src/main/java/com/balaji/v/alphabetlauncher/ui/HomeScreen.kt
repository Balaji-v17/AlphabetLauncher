package com.balaji.v.alphabetlauncher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.balaji.v.alphabetlauncher.data.AppRepositoryViewModel
import com.balaji.v.alphabetlauncher.data.LaunchableApp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FAVOURITE_NAMES = listOf("WhatsApp", "Chrome", "Camera", "Calculator", "Gmail")

@Composable
fun HomeScreen(viewModel: AppRepositoryViewModel = viewModel()) {
    val appsByLetter by viewModel.appsByLetter.collectAsState()
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var now by remember { mutableStateOf(Date()) }

    val allApps = remember(appsByLetter) { appsByLetter.values.flatten() }
    val favourites = remember(allApps) {
        FAVOURITE_NAMES.mapNotNull { name -> allApps.find { it.label.equals(name, ignoreCase = true) } }
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (selectedLetter == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp),
            ) {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                )
                Text(
                    text = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(now),
                    color = Color.White,
                )
                Spacer(Modifier.height(24.dp))
                favourites.forEach { app ->
                    AppRow(app = app, onClick = { viewModel.launch(app) })
                }
            }
        } else {
            val apps = appsByLetter[selectedLetter].orEmpty()
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp),
            ) {
                Text(
                    text = selectedLetter.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                )
                Spacer(Modifier.height(12.dp))
                if (apps.isEmpty()) {
                    Text("No apps", color = Color.Gray)
                } else {
                    LazyColumn {
                        items(apps, key = { it.packageName }) { app ->
                            AppRow(app = app, onClick = { viewModel.launch(app) })
                        }
                    }
                }
            }
        }

        AlphabetBar(
            onLetterChanged = { selectedLetter = it },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
        )
    }
}

@Composable
private fun AppRow(app: LaunchableApp, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        val bitmap = remember(app.packageName) {
            app.icon.toBitmap(width = 96, height = 96).asImageBitmap()
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Spacer(Modifier.width(16.dp))
        Text(app.label, color = Color.White)
    }
}