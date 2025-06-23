package com.example.note.presentation.notes

import com.example.note.domain.model.Note

data class NotesState (
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)