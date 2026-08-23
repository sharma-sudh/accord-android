package com.sudh.accord.auth

import com.sudh.accord.dto.RefreshRequest
import com.sudh.accord.network.AccordApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

// Handles 401s by refreshing the access token and retrying the original
// request once. An Authenticator (not an interceptor) is the correct OkHttp
// mechanism for this — it's specifically invoked on auth failure and given
// the failed request/response to build a retry from.
//
// `refreshApi` must be built from a plain OkHttpClient with no Authenticator
// attached, so a failing refresh call can't recursively trigger another
// refresh attempt.
class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val refreshApi: AccordApi
) : Authenticator {

    // Guards the refresh call itself. The app fires concurrent authenticated
    // requests (e.g. HomeViewModel loads tasks + balance in parallel), so more
    // than one request can 401 around the same moment. Without this lock each
    // would independently rotate the refresh token — and since the backend
    // revokes ALL sessions when it sees a refresh token reused, a second,
    // unsynchronized rotation would look exactly like theft and log the user
    // out everywhere. The lock ensures only one refresh happens; any request
    // that arrives while it's in flight waits, then reuses its result instead
    // of calling refresh again.
    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Only ever retry once per original request chain.
        if (responseCount(response) >= 2) return null

        val failedAuthHeader = response.request.header("Authorization")

        synchronized(refreshLock) {
            // Another thread may have refreshed while we were waiting on the lock.
            // If the stored access token has already changed, just retry with it.
            val currentAuthHeader = tokenManager.getToken()?.let { "Bearer $it" }
            if (currentAuthHeader != null && currentAuthHeader != failedAuthHeader) {
                return response.request.newBuilder()
                    .header("Authorization", currentAuthHeader)
                    .build()
            }

            val refreshToken = tokenManager.getRefreshToken() ?: return null

            return try {
                val newAuth = runBlocking { refreshApi.refresh(RefreshRequest(refreshToken)) }

                tokenManager.saveToken(newAuth.accessToken)
                tokenManager.saveRefreshToken(newAuth.refreshToken)

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newAuth.accessToken}")
                    .build()
            } catch (e: Exception) {
                // Refresh token itself is expired/revoked (or unreachable) —
                // the session can't be salvaged. Clear local state and let the
                // UI layer send the user back to LoginScreen.
                tokenManager.clearAll()
                SessionManager.notifySessionExpired()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}