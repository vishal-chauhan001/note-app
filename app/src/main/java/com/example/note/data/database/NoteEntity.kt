package com.example.note.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.note.domain.model.Note


@Entity(tableName = "notes")
data class NoteEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

fun NoteEntity.toDomain() : Note {
    return Note(
        id = id,
        title = title,
        content = content,
        imagePath = imagePath,
        createdAt = createdAt,
    )
}

fun Note.toEntity() : NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        imagePath = imagePath,
        createdAt = createdAt,
    )
}