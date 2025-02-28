package ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize()) {
        Icon(
            Icons.Filled.Favorite,
            contentDescription = "notes app",
            modifier = Modifier.size(50.dp).align(Alignment.Center)
        )
    }
}
