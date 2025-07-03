package com.example.note.mvi

interface PartialChange<State> {
    fun reduce(oldState: State): State
}