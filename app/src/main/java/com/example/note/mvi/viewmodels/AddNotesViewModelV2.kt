package com.example.note.mvi.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.note.data.utils.ImageUtils
import com.example.note.domain.model.Note
import com.example.note.domain.usecase.AddNoteUseCase
import com.example.note.domain.usecase.GetNoteByIdUseCase
import com.example.note.domain.usecase.UpdateNoteUseCase
import com.example.note.mvi.BaseViewModel
import com.example.note.presentation.add_note.AddNoteIntent
import com.example.note.presentation.add_note.AddNotePartialChange
import com.example.note.presentation.add_note.AddNoteSideEffect
import com.example.note.presentation.add_note.AddNoteState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNotesViewModelV2 @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    @ApplicationContext private val context: Context,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<AddNoteIntent, AddNoteState, AddNoteSideEffect, AddNotePartialChange>() {

    private val noteId = savedStateHandle.get<Long>("noteId")
    private val isEditMode = noteId != null && noteId != -1L

    override fun initialViewState(): AddNoteState = AddNoteState(
        noteId = noteId,
        isEditMode = isEditMode
    )

    override fun changeSideToEffect(change: AddNotePartialChange): List<AddNoteSideEffect> {
        val sideEffect = when (change) {
            is AddNotePartialChange.LoadingStarted -> {
                AddNoteSideEffect.ShowToast("Fetching note...")
            }

            is AddNotePartialChange.NoteLoaded -> {
                AddNoteSideEffect.ShowToast("Note Loaded!!")
            }

            is AddNotePartialChange.ErrorOccured -> {
                AddNoteSideEffect.ShowToast(change.error)
            }

            is AddNotePartialChange.SelectImage -> null
            is AddNotePartialChange.UpdateContent -> null
            is AddNotePartialChange.UpdateTitle -> null
            is AddNotePartialChange.NoteSaved -> {
                AddNoteSideEffect.ShowToast("Note saved successfully")
                AddNoteSideEffect.NavigateBack
            }
            is AddNotePartialChange.SavingStarted -> {
                AddNoteSideEffect.ShowToast("Note Saving...")
            }
        }

        return mutableListOf<AddNoteSideEffect>().apply {
            sideEffect?.let { add(it) }
        }
    }

    init {
        val noteId = savedStateHandle.get<Long>("noteId")
        if(noteId != null && noteId != -1L) {
            viewModelScope.launch {
                processIntent(AddNoteIntent.LoadNote(noteId))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun Flow<AddNoteIntent>.toPartialChanges(): Flow<AddNotePartialChange> {
        return flatMapMerge {
            flow {
                when(it) {
                    is AddNoteIntent.LoadNote -> {
                        loadNote(it.noteId)
                    }

                    is AddNoteIntent.UpdateTitle -> {
                        updateTitle(it.title)
                    }

                    is AddNoteIntent.UpdateContent -> {
                        updateContent(it.content)
                    }

                    is AddNoteIntent.SelectImage -> {
                        selectImage(it.uri)
                    }

                    is AddNoteIntent.SaveNote -> {
                        saveNote()
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun FlowCollector<AddNotePartialChange>.loadNote(noteId: Long) {
        emit(AddNotePartialChange.LoadingStarted(noteId))

        getNoteByIdUseCase(noteId)
            .onSuccess { note ->
                emit(AddNotePartialChange.NoteLoaded(note ?: Note(
                    id = -1,
                    title = "",
                    content = "",
                    imagePath = "",
                    createdAt = 0,
                    updatedAt = 0
                )))
            }
            .onFailure { e ->
                emit(AddNotePartialChange.ErrorOccured(e.message ?: "Failed to load note"))
            }
    }

    private suspend fun FlowCollector<AddNotePartialChange>.updateTitle(title: String) {
        emit(AddNotePartialChange.UpdateTitle(title))
    }

    private suspend fun FlowCollector<AddNotePartialChange>.updateContent(content: String) {
        emit(AddNotePartialChange.UpdateContent(content))
    }

    private suspend fun FlowCollector<AddNotePartialChange>.selectImage(uri: Uri?) {
        emit(AddNotePartialChange.SelectImage(uri))
    }

    private suspend fun FlowCollector<AddNotePartialChange>.saveNote() {
        val currentState = viewState.value

        if(currentState.title.isBlank()) {
            emit(AddNotePartialChange.ErrorOccured("Title cannot be empty"))
            return
        }

        emit(AddNotePartialChange.SavingStarted)

        try {
            // Handle image - either new selection or keep existing
            val finalImagePath = when {
                currentState.selectedImageUri != null && currentState.isCurrentImageChanged -> {
                    val fileName = "note_image_${System.currentTimeMillis()}.jpg"
                    val newImagePath = ImageUtils.saveImageToInternalStorage(
                        context,
                        currentState.selectedImageUri,
                        fileName
                    )

                    if (newImagePath == null) {
                        emit(AddNotePartialChange.ErrorOccured("Image size exceeds 2MB limit"))
                        return
                    }

                    // Delete old image if exists (only in edit mode)
                    if (currentState.isEditMode && currentState.currentImagePath != null) {
                        ImageUtils.deleteImageFile(currentState.currentImagePath.toString())
                    }

                    newImagePath
                }
                else -> currentState.selectedImageUri
            }

            val result = if (currentState.isEditMode && currentState.noteId != null) {
                val existingNote = getNoteByIdUseCase(currentState.noteId).getOrNull()
                val updatedNote = Note(
                    id = currentState.noteId,
                    title = currentState.title.trim(),
                    content = currentState.content.trim(),
                    imagePath = if(finalImagePath == "null" || finalImagePath == null) null else finalImagePath.toString(),
                    createdAt = existingNote?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                updateNoteUseCase(updatedNote)
            } else {
                val newNote = Note(
                    title = currentState.title.trim(),
                    content = currentState.content.trim(),
                    imagePath = if(finalImagePath == "null" || finalImagePath == null) null else finalImagePath.toString()
                )
                addNoteUseCase(newNote)
            }

            result
                .onSuccess {
                    emit(AddNotePartialChange.NoteSaved)
                }
                .onFailure {e ->
                    emit(AddNotePartialChange.ErrorOccured(e.message ?: "Failed to save note"))
                }
        } catch (e: Exception) {
            emit(AddNotePartialChange.ErrorOccured(e.message ?: "An error occurred"))
        }
    }
}