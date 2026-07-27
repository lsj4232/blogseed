package com.iplab.blogseed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iplab.blogseed.ui.BlogSeedTheme
import com.iplab.blogseed.ui.EditorScreen
import com.iplab.blogseed.ui.HomeScreen
import com.iplab.blogseed.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlogSeedTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot(vm: MainViewModel = viewModel()) {
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        modifier = Modifier
    ) { padding ->
        NavHost(navController = nav, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    vm = vm,
                    padding = padding,
                    onOpenDraft = { id ->
                        vm.open(id)
                        nav.navigate("editor")
                    },
                    onSettings = { nav.navigate("settings") }
                )
            }
            composable("editor") {
                EditorScreen(vm = vm, padding = padding, onBack = { nav.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(vm = vm, padding = padding, onBack = { nav.popBackStack() })
            }
        }
    }
}
