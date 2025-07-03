package com.example.note.presentation.add_note

import android.net.Uri
import androidx.core.net.toUri
import com.example.note.domain.model.Note
import com.example.note.mvi.PartialChange

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

sealed class AddNotePartialChange: PartialChange<AddNoteState> {
    data class LoadingStarted(val noteId: Long) : AddNotePartialChange() {
        override fun reduce(oldState: AddNoteState): AddNoteState {
            return oldState.copy(noteId = noteId, isEditMode = true, isLoadingNote = true, error = null)
        }
    }

    data class NoteLoaded(val note: Note) : AddNotePartialChange() {
        override fun reduce(oldState: AddNoteState): AddNoteState {
            return oldState.copy(
                noteId = note.id,
                title = note.title,
                content = note.content,
                selectedImageUri = note.imagePath?.toUri(),
                isLoadingNote = false,
                error = null
            )
        }
    }

    data class UpdateTitle(val title: String) : AddNotePartialChange() {
        override fun reduce(oldState: AddNoteState): AddNoteState {
            return oldState.copy(title = title)
        }
    }

    data class UpdateContent(val content: String) : AddNotePartialChange() {
        override fun reduce(oldState: AddNoteState): AddNoteState {
            return oldState.copy(content = content)
        }
    }

    data class SelectImage(val uri: Uri?) : AddNotePartialChange() {
        override fun reduce(oldState: AddNoteState): AddNoteState {
            return oldState.copy(selectedImageUri = uri, isCurrentImageChanged = true)
        }
    }

    data object SavingStarted : AddNotePartialChange() {
        override fun reduce(oldState: AddNoteState): AddNoteState {
            return oldState.copy(isLoading = true, error = null)
        }
    }

    data object NoteSaved : AddNotePartialChange() {
        override fun reduce(oldState: AddNoteState): AddNoteState {
            return oldState.copy(
                isLoading = false,
                isNoteSaved = true
            )
        }
    }

    data class ErrorOccured(val error: String) : AddNotePartialChange() {
        override fun reduce(oldState: AddNoteState): AddNoteState {
            return oldState.copy(
                isLoadingNote = false,
                error = error
            )
        }
    }
}

sealed class AddNoteSideEffect {
    data object NavigateBack : AddNoteSideEffect()
    data class ShowToast(val message: String) : AddNoteSideEffect()
}