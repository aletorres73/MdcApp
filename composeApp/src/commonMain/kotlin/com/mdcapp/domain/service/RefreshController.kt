package com.mdcapp.domain.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RefreshController {
    private val _refreshFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshFlow = _refreshFlow.asSharedFlow()

    fun triggerRefresh() {
        _refreshFlow.tryEmit(Unit)
    }
}
