package com.example.note

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import com.example.note.presentation.screens.NotesListScreen
import com.example.note.ui.theme.NoteTheme
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.note.presentation.screens.AddNoteScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteTheme {

                val window = (this as Activity).window
                SideEffect {
                    window.statusBarColor = Color(0xFF171C26).toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
                }
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NotesApp()
                }
            }
        }
    }
}

@Composable
fun NotesApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "notes-list"
    ) {
        composable("notes-list") {
            NotesListScreen(
                onAddNoteClick = {
                    navController.navigate("add-note/-1")
                },
                onNavigateToEditNote = { noteId ->
                    navController.navigate("add-note/$noteId")
                }
            )
        }

        composable(
            route = "add-note/{noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )) {
            AddNoteScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }

}