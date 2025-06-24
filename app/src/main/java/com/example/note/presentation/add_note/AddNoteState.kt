package com.example.note.presentation.add_note

data class AddNoteState (
    val title: String = "",
    val content: String = "",
    val selectedImageUri: android.net.Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isNoteSaved: Boolean = false
)