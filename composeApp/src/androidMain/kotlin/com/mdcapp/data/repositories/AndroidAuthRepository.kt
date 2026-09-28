package com.mdcapp.data.repositories

import com.mdcapp.domain.model.AppUser
import com.mdcapp.domain.repositories.IAuthRepository
import com.mdcapp.domain.service.ISessionManager
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AndroidAuthRepository(
    private val auth: FirebaseAuth,
    private val sessionManager: ISessionManager
) : IAuthRepository {

    private var isReauthenticating = false

    private suspend fun ensureFirebaseAuthInSync() {
        if (auth.currentUser == null) {
            val session = sessionManager.getSession()
            if (session != null && !session.password.isNullOrEmpty() && !isReauthenticating) {
                isReauthenticating = true
                try {
                    println("🔑 [MDC_SESSION] ensureFirebaseAuthInSync: Auto re-authenticating Firebase Auth for ${session.email}...")
                    auth.signInWithEmailAndPassword(session.email, session.password)
                    println("🔑 [MDC_SESSION] ensureFirebaseAuthInSync: Success! Firebase Auth is now authenticated as ${auth.currentUser?.uid}")
                } catch (e: Exception) {
                    println("❌ [MDC_SESSION] ensureFirebaseAuthInSync failed: ${e.message}")
                } finally {
                    isReauthenticating = false
                }
            }
        }
    }

    override val currentUser: AppUser?
        get() {
            val user = auth.currentUser
            if (user != null) {
                val currentPass = sessionManager.getSession()?.password
                sessionManager.saveSession(user.uid, user.email ?: "", currentPass)
                println("🔑 [MDC_SESSION] AndroidAuthRepository.currentUser from Firebase -> uid: ${user.uid}, email: ${user.email}")
                return AppUser(user.uid, user.email)
            }
            val localSession = sessionManager.getSession()
            if (localSession != null) {
                println("🔑 [MDC_SESSION] AndroidAuthRepository.currentUser from LocalSessionManager Fallback -> uid: ${localSession.uid}, email: ${localSession.email}")
                CoroutineScope(Dispatchers.IO).launch {
                    ensureFirebaseAuthInSync()
                }
                return AppUser(localSession.uid, localSession.email)
            }
            println("🔑 [MDC_SESSION] AndroidAuthRepository.currentUser -> null (both Firebase and Local session are null)")
            return null
        }

    override fun observeAuthState(): Flow<AppUser?> {
        println("🔑 [MDC_SESSION] AndroidAuthRepository.observeAuthState() subscribed")
        return auth.authStateChanged
            .onEach { user ->
                if (user != null) {
                    val currentPass = sessionManager.getSession()?.password
                    sessionManager.saveSession(user.uid, user.email ?: "", currentPass)
                } else {
                    val session = sessionManager.getSession()
                    if (session != null && !session.password.isNullOrEmpty()) {
                        println("🔑 [MDC_SESSION] authStateChanged emitted null. Triggering auto re-authentication in background...")
                        CoroutineScope(Dispatchers.IO).launch {
                            ensureFirebaseAuthInSync()
                        }
                    }
                }
                println("🔑 [MDC_SESSION] authStateChanged emitted -> uid: ${user?.uid}, email: ${user?.email}")
            }
            .map { user ->
                user?.let { AppUser(it.uid, it.email) } ?: run {
                    val local = sessionManager.getSession()
                    local?.let { AppUser(it.uid, it.email) }
                }
            }
    }

    override suspend fun signIn(email: String, password: String): AppUser? {
        println("🔑 [MDC_SESSION] AndroidAuthRepository.signIn() starting for: $email")
        val result = auth.signInWithEmailAndPassword(email, password).user?.let {
            AppUser(it.uid, it.email)
        }
        if (result != null) {
            sessionManager.saveSession(result.uid, result.email ?: "", password)
        }
        println("🔑 [MDC_SESSION] AndroidAuthRepository.signIn() result -> uid: ${result?.uid}")
        return result
    }

    override suspend fun signUp(email: String, password: String): AppUser? {
        println("🔑 [MDC_SESSION] AndroidAuthRepository.signUp() starting for: $email")
        val result = auth.createUserWithEmailAndPassword(email, password).user?.let {
            AppUser(it.uid, it.email)
        }
        if (result != null) {
            sessionManager.saveSession(result.uid, result.email ?: "", password)
        }
        println("🔑 [MDC_SESSION] AndroidAuthRepository.signUp() result -> uid: ${result?.uid}")
        return result
    }

    override suspend fun signOut() {
        println("🚨 [MDC_SESSION] AndroidAuthRepository.signOut() CALLED! Clearing local session and calling auth.signOut().")
        sessionManager.clearSession()
        auth.signOut()
        println("🔑 [MDC_SESSION] AndroidAuthRepository.signOut() finished. currentUser is now: ${auth.currentUser?.uid}")
    }

    override fun isUserLoggedIn(): Boolean {
        val fbUser = auth.currentUser
        if (fbUser != null) {
            println("🔑 [MDC_SESSION] AndroidAuthRepository.isUserLoggedIn() -> true (Firebase uid: ${fbUser.uid})")
            return true
        }
        val localValid = sessionManager.isSessionValid()
        println("🔑 [MDC_SESSION] AndroidAuthRepository.isUserLoggedIn() -> $localValid (LocalSessionManager fallback)")
        return localValid
    }

    override suspend fun reauthenticate(password: String) {
        println("🔑 [MDC_SESSION] AndroidAuthRepository.reauthenticate() starting")
        val email = auth.currentUser?.email
            ?: sessionManager.getSession()?.email
            ?: throw Exception("User not authenticated")
        auth.signInWithEmailAndPassword(email, password)
    }

    override suspend fun updatePassword(newPassword: String) {
        println("🔑 [MDC_SESSION] AndroidAuthRepository.updatePassword() starting")
        auth.currentUser?.updatePassword(newPassword)
    }
}
