package com.example.note.domain.usecase

import com.example.note.data.repository.NotesRepository
import com.example.note.domain.model.Note
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val notesRepository: NotesRepository
) {
    suspend operator fun invoke(note: Note) : Result<Unit> {
        return try {
            notesRepository.deleteNote(note)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}