package com.example.note.presentation.notes

import com.example.note.domain.model.Note
import com.example.note.mvi.PartialChange

data class NotesState (
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class NotesPartialChange: PartialChange<NotesState> {
    data object LoadingStarted : NotesPartialChange() {
        override fun reduce(oldState: NotesState): NotesState {
            return oldState.copy(isLoading = true, error = null)
        }
    }

    data class NotesLoaded(val notes: List<Note>) : NotesPartialChange() {
        override fun reduce(oldState: NotesState): NotesState {
            return oldState.copy(
                notes = notes,
                isLoading = false,
                error = null
            )
        }
    }

    data class ErrorOccurred(val error: String) : NotesPartialChange() {
        override fun reduce(oldState: NotesState): NotesState {
            return oldState.copy(
                isLoading = false,
                error = error
            )
        }
    }

    data class NoteDeleted(val note: Note) : NotesPartialChange() {
        override fun reduce(oldState: NotesState): NotesState {
            return oldState.copy(
                notes = oldState.notes.filter { it.id != note.id }
            )
        }
    }
 }

sealed class NotesSideEffect {
    data class ShowToast(val message: String) : NotesSideEffect()
}