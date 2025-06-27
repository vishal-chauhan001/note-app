package com.example.note.presentation.notes

import com.example.note.domain.model.Note

sealed class NotesIntent {
    data object LoadNotes : NotesIntent()
    data class DeleteNote(val note: Note) : NotesIntent()
}