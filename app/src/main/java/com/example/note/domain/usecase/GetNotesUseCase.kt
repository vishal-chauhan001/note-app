package com.example.note.domain.usecase

import com.example.note.data.repository.NotesRepository
import com.example.note.domain.model.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    operator fun invoke(): Flow<List<Note>> = notesRepository.getAllNotes()
}