package com.example.note.presentation.notes

import com.example.note.domain.model.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesPartialChangeTest {

    @Test
    fun `LoadingStarted should set loading to true and clear error`() {
        val initialState = NotesState(
            notes = listOf(Note(id = 1, title = "Test", content = "Content")),
            isLoading = false,
            error = "Some error"
        )

        val result = NotesPartialChange.LoadingStarted.reduce(initialState)

        assertTrue(result.isLoading)
        assertNull(result.error)
        assertEquals(initialState.notes, result.notes) // Notes should remain unchanged
    }

    @Test
    fun `NotesLoaded should set notes and clear loading and error`() {
        val newNotes = listOf(
            Note(id = 1, title = "Note 1", content = "Content 1"),
            Note(id = 2, title = "Note 2", content = "Content 2")
        )
        val initialState = NotesState(
            notes = emptyList(),
            isLoading = true,
            error = "Some error"
        )

        val result = NotesPartialChange.NotesLoaded(newNotes).reduce(initialState)

        assertEquals(newNotes, result.notes)
        assertFalse(result.isLoading)
        assertNull(result.error)
    }

    @Test
    fun `ErrorOccurred should set error and clear loading`() {
        val errorMessage = "Failed to load notes"
        val initialState = NotesState(
            notes = listOf(Note(id = 1, title = "Test", content = "Content")),
            isLoading = true,
            error = null
        )

        val result = NotesPartialChange.ErrorOccurred(errorMessage).reduce(initialState)

        assertEquals(errorMessage, result.error)
        assertFalse(result.isLoading)
        assertEquals(initialState.notes, result.notes) // Notes should remain unchanged
    }

    @Test
    fun `NoteDeleted should remove note from list`() {
        val note1 = Note(id = 1, title = "Note 1", content = "Content 1")
        val note2 = Note(id = 2, title = "Note 2", content = "Content 2")
        val note3 = Note(id = 3, title = "Note 3", content = "Content 3")

        val initialState = NotesState(
            notes = listOf(note1, note2, note3),
            isLoading = false,
            error = null
        )

        val result = NotesPartialChange.NoteDeleted(note2).reduce(initialState)

        assertEquals(listOf(note1, note3), result.notes)
        assertEquals(initialState.isLoading, result.isLoading)
        assertEquals(initialState.error, result.error)
    }
}