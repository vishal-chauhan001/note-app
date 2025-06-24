package com.example.note.domain.usecase

import com.example.note.data.repository.NotesRepository
import com.example.note.domain.model.Note
import javax.inject.Inject

class GetNoteByIdUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(id: Long) : Result<Note?> {
        return try {
            val note = notesRepository.getNoteById(id)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}