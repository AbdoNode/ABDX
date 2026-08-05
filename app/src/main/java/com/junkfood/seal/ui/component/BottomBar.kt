package com.junkfood.seal.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R

@Composable
fun BottomBar(
    current: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {

        NavigationBarItem(
            selected = current == "home",
            onClick = { onNavigate("home") },
            icon = {
                Icon(Icons.Outlined.Home, null)
            },
            label = {
                Text("Home")
            }
        )

        NavigationBarItem(
            selected = current == "settings",
            onClick = { onNavigate("settings") },
            icon = {
                Icon(Icons.Outlined.Settings, null)
            },
            label = {
                Text(stringResource(R.string.settings))
            }
        )
    }
}