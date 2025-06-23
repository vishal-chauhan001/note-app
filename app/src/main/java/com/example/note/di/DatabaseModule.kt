package com.example.note.di

import android.content.Context
import androidx.room.Room
import com.example.note.data.database.NoteDao
import com.example.note.data.database.NotesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNotesDatabase(@ApplicationContext context: Context) : NotesDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = NotesDatabase::class.java,
            name = "notes_database"
        ).build()
    }

    @Provides
    fun provideNoteDao(database: NotesDatabase) : NoteDao {
        return database.noteDao()
    }
}