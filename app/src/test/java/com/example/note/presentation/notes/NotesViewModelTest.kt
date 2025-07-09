package com.example.note.presentation.notes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.note.domain.model.Note
import com.example.note.domain.usecase.DeleteNoteUseCase
import com.example.note.domain.usecase.GetNotesUseCase
import com.example.note.mvi.viewmodels.NotesViewModelV2
import com.example.note.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var getNotesUseCase: GetNotesUseCase

    @Mock
    private lateinit var deleteNoteUseCase: DeleteNoteUseCase

    private lateinit var viewModel: NotesViewModelV2

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `diagnostic - check everything step by step`() = runTest {
        println("=== DIAGNOSTIC TEST START ===")

        // Step 1: Check if mocking works
        println("Step 1: Setting up mock...")
        val testNotes = listOf(Note(id = 1, title = "Test", content = "Content"))
        whenever(getNotesUseCase()).thenReturn(flowOf(testNotes))
        println("Mock setup complete")

        // Step 2: Test use case directly
        println("Step 2: Testing use case directly...")
        try {
            getNotesUseCase().collect { notes ->
                println("Use case returned: $notes")
                assert(notes == testNotes)
            }
            println("Use case works directly!")
        } catch (e: Exception) {
            println("Use case failed: ${e.message}")
            e.printStackTrace()
        }

        // Step 3: Try creating ViewModel
        println("Step 3: Creating ViewModel...")
        try {
            val viewModel = NotesViewModelV2(getNotesUseCase, deleteNoteUseCase)
            println("ViewModel created successfully")

            val initialState = viewModel.viewState.value
            println("Initial state: $initialState")

        } catch (e: Exception) {
            println("ViewModel creation failed: ${e.message}")
            e.printStackTrace()
        }

        println("=== DIAGNOSTIC TEST END ===")
    }

    @Test
    fun `when ViewModel is initialized, should emit initial state and load notes`() = runTest {
        val testNotes = listOf(
            Note(id = 1, title = "Note 1", content = "testing testing testing"),
            Note(id = 2, title = "Note 2", content = "testing testing testing testing testing testing testing testing testing testing testing testing testing testing testing testing testing testing")
        )

        whenever(getNotesUseCase()).thenReturn(flowOf(testNotes))

        viewModel = NotesViewModelV2(getNotesUseCase, deleteNoteUseCase)

        viewModel.processIntent(NotesIntent.LoadNotes)
        advanceUntilIdle()

//        viewModel.viewState.test {
//            val initialState = awaitItem()
//            assertEquals(NotesState(), initialState)
//
//            val loadingState = awaitItem()
//            assertTrue(loadingState.isLoading)
//            assertNull(loadingState.error)
//
//            val loadedState = awaitItem()
//            assertFalse(loadedState.isLoading)
//            assertEquals(testNotes, loadedState.notes)
//            assertNull(loadedState.error)
//        }
    }

    @Test
    fun `when loading notes fail, should emit error state`() = runTest {
        val errorMessage = "Failed to load notes"
        whenever(getNotesUseCase()).thenThrow(RuntimeException(errorMessage))

        viewModel = NotesViewModelV2(getNotesUseCase, deleteNoteUseCase)

        viewModel.viewState.test {
            awaitItem()
            awaitItem()

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(errorState.error, errorMessage)
            assertTrue(errorState.notes.isEmpty())
        }
    }

    @Test
    fun `when delete note intent is processed, should emit updated state and side effect`() = runTest {
        val initialNotes = listOf(
            Note(id = 1, title = "Note 1", content = "testing testing testing"),
            Note(id = 2, title = "Note 2", content = "testing testing testing testing testing testing testing testing testing testing testing testing testing testing testing testing testing testing")
        )

        val noteToDelete = initialNotes[0]

        whenever(getNotesUseCase()).thenReturn(flowOf(initialNotes))
        whenever(deleteNoteUseCase(noteToDelete)).thenReturn(Result.success(Unit))

        viewModel = NotesViewModelV2(getNotesUseCase, deleteNoteUseCase)

        viewModel.viewState.test {
            awaitItem()  // Initial state
            awaitItem()  // Loading
            awaitItem()  // Loaded

            viewModel.processIntent(NotesIntent.DeleteNote(noteToDelete))

            val updatedState = awaitItem()
            assertEquals(initialNotes.size - 1, updatedState.notes.size)
            assertEquals(listOf(initialNotes[1]), updatedState.notes)
            assertFalse(updatedState.isLoading)
            assertNull(updatedState.error)
        }

        viewModel.sideEffect.test {
            val sideEffect = awaitItem()
            assertTrue(sideEffect is NotesSideEffect.ShowToast)
            assertEquals("Note deleted successfully", (sideEffect as NotesSideEffect.ShowToast).message)
        }
    }

    @Test
    fun `when delete note fails, should emit error side effect`() = runTest {
        val initialNotes = listOf(
            Note(id = 1, title = "Note 1", content = "testing testing testing"),
        )
        val noteToDelete = initialNotes[0]
        val errorMessage = "Failed to delete note"

        whenever(getNotesUseCase()).thenReturn(flowOf(initialNotes))
        whenever(deleteNoteUseCase(noteToDelete)).thenReturn(Result.failure(RuntimeException(errorMessage)))

        viewModel = NotesViewModelV2(getNotesUseCase, deleteNoteUseCase)

        viewModel.viewState.test {
            awaitItem()  // Initial state
            awaitItem()  // Loading
            awaitItem()  // Loaded

            viewModel.processIntent(NotesIntent.DeleteNote(noteToDelete))

            expectNoEvents()
        }

        viewModel.sideEffect.test {
            val sideEffect = awaitItem()
            assertTrue(sideEffect is NotesSideEffect.ShowToast)
            assertEquals(errorMessage, (sideEffect as NotesSideEffect.ShowToast).message)
        }
    }

    @Test
    fun `when load notes is processed, should reload notes`() = runTest {
        val initialNotes = listOf(Note(id = 1, title = "Note 1", content = "Content 1"))
        val updatedNotes = listOf(
            Note(id = 1, title = "Note 1", content = "Content 1"),
            Note(id = 2, title = "Note 2", content = "Content 2")
        )

        whenever(getNotesUseCase()).thenReturn(flowOf(initialNotes)).thenReturn(flowOf(updatedNotes))

        viewModel = NotesViewModelV2(getNotesUseCase, deleteNoteUseCase)

        viewModel.viewState.test {
            awaitItem() // Initial
            awaitItem() // Loading
            val firstLoadedState = awaitItem() // First load
            assertEquals(initialNotes, firstLoadedState.notes)

            // Process reload intent
            viewModel.processIntent(NotesIntent.LoadNotes)

            awaitItem() // Loading again
            val reloadedState = awaitItem() // Reloaded
            assertEquals(updatedNotes, reloadedState.notes)
        }
    }
}