package com.example.note.mvi.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.note.domain.model.Note
import com.example.note.domain.usecase.DeleteNoteUseCase
import com.example.note.domain.usecase.GetNotesUseCase
import com.example.note.mvi.BaseViewModel
import com.example.note.presentation.notes.NotesIntent
import com.example.note.presentation.notes.NotesPartialChange
import com.example.note.presentation.notes.NotesSideEffect
import com.example.note.presentation.notes.NotesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModelV2 @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : BaseViewModel<NotesIntent, NotesState, NotesSideEffect, NotesPartialChange>() {
    override fun initialViewState(): NotesState = NotesState()

    override fun changeSideToEffect(change: NotesPartialChange): List<NotesSideEffect> {
        val sideEffect = when (change) {
            is NotesPartialChange.LoadingStarted -> {
                NotesSideEffect.ShowToast("Loading notes...")
            }
            is NotesPartialChange.NotesLoaded -> {
                NotesSideEffect.ShowToast("Notes Loaded!!")
            }
            is NotesPartialChange.ErrorOccurred -> {
                NotesSideEffect.ShowToast(change.error)
                null
            }
            is NotesPartialChange.NoteDeleted -> {
                NotesSideEffect.ShowToast("Note deleted")
            }
        }

        return mutableListOf<NotesSideEffect>().apply {
            sideEffect?.let { add(it) }
        }
    }

    fun loadNotesOnStart() {
        viewModelScope.launch {
            processIntent(NotesIntent.LoadNotes)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun Flow<NotesIntent>.toPartialChanges(): Flow<NotesPartialChange> {
        return flatMapMerge {
            flow {
                when(it) {
                    is NotesIntent.LoadNotes -> {
                        loadNotes()
                    }
                    is NotesIntent.DeleteNote -> {
                        deleteNote(it.note)
                    }
                }
            }
        }
    }

    private suspend fun FlowCollector<NotesPartialChange>.loadNotes() {
        emit(NotesPartialChange.LoadingStarted)

        getNotesUseCase()
            .catch { e ->
                emit(NotesPartialChange.ErrorOccurred(e.message ?: "Unknown error occurred"))
            }
            .collect { notes ->
                emit(NotesPartialChange.NotesLoaded(notes))
            }
    }

    private suspend fun FlowCollector<NotesPartialChange>.deleteNote(note: Note) {
        try {
            val result = deleteNoteUseCase(note)
            result.fold(
                onSuccess = {
                    emit(NotesPartialChange.NoteDeleted(note))
                },
                onFailure = { e ->
                    emit(NotesPartialChange.ErrorOccurred(e.message ?: "Failed to delete note"))
                }
            )
        } catch (e: Exception) {
            emit(NotesPartialChange.ErrorOccurred(e.message ?: "Failed to delete note"))
        }
    }
}