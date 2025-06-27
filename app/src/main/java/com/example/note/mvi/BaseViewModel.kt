package com.example.note.mvi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

abstract class BaseViewModel<ViewIntent, ViewState, ViewSideEffect, Change: PartialChange<ViewState>>() :
    ViewModel() {

    abstract fun initialViewState() : ViewState
    abstract fun Flow<ViewIntent>.toPartialChanges() : Flow<Change>
    abstract fun changeSideToEffect(change: Change): List<ViewSideEffect>

    private val _intentFlow = MutableSharedFlow<ViewIntent>()
    private val sideEffectChannel = Channel<ViewSideEffect>(Channel.BUFFERED)

    val viewState: StateFlow<ViewState>
    val sideEffect: Flow<ViewSideEffect> = sideEffectChannel.receiveAsFlow()

    init {
        val initialVS = initialViewState()
        viewState = _intentFlow
            .onEach { Log.d("INTENT", it.toString()) }
            .toPartialChanges()
            .catch { Log.d("ERROR", it.toString()) }
            .onEach { Log.d("PARTIAL CHANGE", it.toString()) }
            .toSideEffect()
            .scan(initialVS) { vs, change ->
                change.reduce(vs).also {
                    Log.d("UPDATED STATE", it.toString())
                }
            }
            .catch { Log.d("ERROR", it.toString()) }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    suspend fun processIntent(intent: ViewIntent) {
        _intentFlow.emit(intent)
    }
        fun Flow<Change>.toSideEffect(): Flow<Change> {
        return onEach { change ->
            changeSideToEffect(change).forEach {
                Log.d("SIDE EFFECT", it.toString())
                sideEffectChannel.send(it)
            }
        }
    }
}