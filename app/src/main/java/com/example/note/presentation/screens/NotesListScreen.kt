package com.example.note.presentation.screens

import CustomSearchBar
import NoteCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NotesListScreen(
    onAddNoteClick: () -> Unit
) {
    val notes = listOf(
        NoteCardData("Balance", "When a design is unbalanced...When a design is unbalanced...When a design is unbalanced...", "Design", "Sun, 16:32", null),
        NoteCardData("Tasks", "✓ Learn new things\n✓ Design things\n◻ Share work", "Todo", "Sun, 16:32",2),
        NoteCardData("Travel", "Perfect time to finally create a list.\n✓ Canada\n✓ Finland", "Travel", "Sun, 8:48", 3),
        NoteCardData("Similarity", "Elements that share similar properties...", "Design", "Sun, 17:00", null),
        NoteCardData("Symmetry", "Can occur in any orientation...", "Theory", "Sun, 17:15", 5),
        NoteCardData("Balance", "When a design is unbalanced...When a design is unbalanced...When a design is unbalanced...", "Design", "Sun, 16:32", 1),
        NoteCardData("Tasks", "✓ Learn new things\n✓ Design things\n◻ Share work", "Todo", "Sun, 16:32",2),
        NoteCardData("Travel", "Perfect time to finally create a list.\n✓ Canada\n✓ Finland", "Travel", "Sun, 8:48", null),
        NoteCardData("Similarity", "Elements that share similar properties...", "Design", "Sun, 17:00", 4),
        NoteCardData("Symmetry", "Can occur in any orientation...", "Theory", "Sun, 17:15", 5),
    )

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp, vertical = 25.dp),
    ) {
        Text(
            text = "Notes",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 15.dp)
        )

        CustomSearchBar()

        Spacer(modifier = Modifier.height(15.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp,
                contentPadding = PaddingValues(0.dp)
            ) {
                items(notes) { note ->
                    NoteCardItem(note)
                }
            }

            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = Color(0xFF3269FF),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note"
                )
            }
        }
    }
}

data class NoteCardData(
    val title: String,
    val description: String,
    val category: String,
    val timestamp: String,
    val imageResourceId: Int? = null
)

@Composable
fun NoteCardItem(note: NoteCardData) {
    NoteCard(
        title = note.title,
        description = note.description,
        category = note.category,
        timestamp = note.timestamp,
        imageResourceId = note.imageResourceId,
    )
}
