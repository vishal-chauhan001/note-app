package com.example.note.domain.usecase

import com.example.note.data.repository.NotesRepository
import com.example.note.domain.model.Note
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class GetNotesUseCaseTest {

    @Mock
    private lateinit var repository: NotesRepository

    private lateinit var useCase: GetNotesUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GetNotesUseCase(repository)
    }

    @Test
    fun `should return all notes from repository`() = runTest {
        // Given
        val expectedNotes = listOf(
            Note(id = 1, title = "Note 1", content = "Content 1"),
            Note(id = 2, title = "Note 2", content = "Content 2")
        )
        whenever(repository.getAllNotes()).thenReturn(flowOf(expectedNotes))

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(expectedNotes, result[0])
    }

    @Test
    fun `should return empty list when no notes exist`() = runTest {
        // Given
        whenever(repository.getAllNotes()).thenReturn(flowOf(emptyList()))

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(emptyList<Note>(), result[0])
    }
}