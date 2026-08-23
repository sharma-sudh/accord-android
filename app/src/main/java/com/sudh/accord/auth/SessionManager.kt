package com.sudh.accord.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// App-wide signal that the user's session is no longer valid (refresh token
// expired or revoked). Emitted from TokenAuthenticator, which runs on an
// OkHttp dispatcher thread with no ViewModel or Compose scope of its own —
// so this is a plain singleton rather than something tied to a ViewModel.
object SessionManager {
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}