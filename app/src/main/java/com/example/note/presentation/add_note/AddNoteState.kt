package com.example.note.presentation.add_note

import android.net.Uri

data class AddNoteState (
    val noteId: Long? = null,
    val title: String = "",
    val content: String = "",
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isNoteSaved: Boolean = false,
    val isLoadingNote: Boolean = false,
    val isEditMode: Boolean = false,
    val currentImagePath: Uri? = null,
    val isCurrentImageChanged: Boolean = false
)