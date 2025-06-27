package com.example.note.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note.domain.model.Note
import com.example.note.domain.usecase.DeleteNoteUseCase
import com.example.note.domain.usecase.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val state: StateFlow<NotesState> = _state.asStateFlow()

    init {
        handleIntent(NotesIntent.LoadNotes)
    }

    fun handleIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.LoadNotes -> loadNotes()
            is NotesIntent.DeleteNote -> deleteNote(intent.note)
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            getNotesUseCase()
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { notes ->
                    _state.value = _state.value.copy(
                        notes = notes,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            deleteNoteUseCase(note)
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        error = e.message ?: "Failed to delete note"
                    )
                }
        }
    }
}