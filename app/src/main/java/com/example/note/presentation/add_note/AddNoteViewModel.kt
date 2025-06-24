package com.example.note.presentation.add_note

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.note.data.utils.ImageUtils
import com.example.note.domain.model.Note
import com.example.note.domain.usecase.AddNoteUseCase
import com.example.note.domain.usecase.GetNoteByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase,
    @ApplicationContext private val context: Context,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AddNoteState())
    val state: StateFlow<AddNoteState> = _state.asStateFlow()

    init {
        val noteId = savedStateHandle.get<Long>("noteId")
        if (noteId != null && noteId != -1L) {
            _state.value = _state.value.copy(noteId = noteId, isEditMode = true)
            handleIntent(AddNoteIntent.LoadNote(noteId))
        }
    }

    fun handleIntent(intent: AddNoteIntent) {
        when (intent) {
            is AddNoteIntent.LoadNote -> loadNote(intent.noteId)
            is AddNoteIntent.UpdateTitle -> updateTitle(intent.title)
            is AddNoteIntent.UpdateContent -> updateContent(intent.content)
            is AddNoteIntent.SelectImage -> selectImage(intent.uri)
            is AddNoteIntent.SaveNote -> saveNote()
            is AddNoteIntent.ClearError -> clearError()
        }
    }

    private fun loadNote(noteId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingNote = true, error = null)

            getNoteByIdUseCase(noteId)
                .onSuccess { note ->
                    note?.let {
                        _state.value = _state.value.copy(
                            title = it.title,
                            content = it.content,
                            selectedImageUri = it.imagePath?.toUri(),
                            isLoadingNote = false
                        )
                    } ?: run {
                        _state.value = _state.value.copy(
                            isLoadingNote = false,
                            error = "Note not found"
                        )
                    }
                }
        }
    }

    private fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    private fun updateContent(content: String) {
        _state.value = _state.value.copy(content = content)
    }

    private fun selectImage(uri: Uri?) {
        _state.value = _state.value.copy(selectedImageUri = uri)
        if (uri == null) {
            _state.value = _state.value.copy(error = null)
        }
    }

    private fun saveNote() {
        val currentState = _state.value

        if (currentState.title.isBlank()) {
            _state.value = currentState.copy(error = "Title cannot be empty")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            try {
                val imagePath = currentState.selectedImageUri?.let { uri ->
                    val fileName = "note_image_${System.currentTimeMillis()}.jpg"
                    ImageUtils.saveImageToInternalStorage(context, uri, fileName)
                }

                if (currentState.selectedImageUri != null && imagePath == null) {
                    _state.value = currentState.copy(
                        isLoading = false,
                        error = "Image size exceeds 2MB limit"
                    )
                    return@launch
                }

                val note = Note(
                    title = currentState.title.trim(),
                    content = currentState.content.trim(),
                    imagePath = imagePath
                )

                addNoteUseCase(note)
                    .onSuccess {
                        _state.value = currentState.copy(
                            isLoading = false,
                            isNoteSaved = true
                        )
                    }
                    .onFailure { e ->
                        _state.value = currentState.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to save note"
                        )
                    }

            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}