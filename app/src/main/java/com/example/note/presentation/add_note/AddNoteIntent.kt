package com.example.note.presentation.add_note

import android.net.Uri

sealed class AddNoteIntent {
    data class UpdateTitle(val title: String) : AddNoteIntent()
    data class UpdateContent(val content: String) : AddNoteIntent()
    data class SelectImage(val uri: Uri?) : AddNoteIntent()
    object SaveNote : AddNoteIntent()
    object ClearError : AddNoteIntent()
}