package com.example.note.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.note.presentation.add_note.AddNoteIntent
import com.example.note.presentation.add_note.AddNoteViewModel
import com.example.note.presentation.components.ImagePicker

@Composable
fun AddNoteScreen(
    onBackClick: () -> Unit,
    viewModel: AddNoteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isNoteSaved) {
        if(state.isNoteSaved) {
            onBackClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp, vertical = 25.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .size(45.dp)
                    .background(Color(0xFF171C26))
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }
            Text(
                text = if (state.isEditMode) "Edit Note" else "Add Note",
                modifier = Modifier.padding(horizontal = 15.dp),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.weight(1f))
//            if(state.isEditMode) {
//                IconButton(
//                    onClick = onBackClick,
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(100.dp))
//                        .size(45.dp)
//                        .background(Color(0xFF171C26))
//                ) {
//                    Icon(
//                        imageVector = Icons.Outlined.Delete,
//                        contentDescription = "delete",
//                        tint = Color.White,
//                        modifier = Modifier.size(25.dp)
//                    )
//                }
//            }
        }

        if(state.isLoadingNote) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column (
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.handleIntent(AddNoteIntent.UpdateTitle(it)) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.content,
                    onValueChange = { viewModel.handleIntent(AddNoteIntent.UpdateContent(it)) },
                    label = {Text("Content")},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    minLines = 8,
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ImagePicker(
                    selectedImageUri = state.selectedImageUri,
                    onImageSelected = { uri ->
                        viewModel.handleIntent(AddNoteIntent.SelectImage(uri))
                    }
                )

                IconButton(
                    onClick = { viewModel.handleIntent(AddNoteIntent.SaveNote) },
                    enabled = !state.isLoading && state.title.isNotBlank()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Note"
                        )
                    }
                }

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}