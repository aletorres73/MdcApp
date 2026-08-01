package com.mdcapp.data.service

import com.mdcapp.domain.entities.UserModel
import dev.gitlive.firebase.firestore.FirebaseFirestore

class UserService(
    private val db: FirebaseFirestore,
    private val authService: AuthService
) {
    private val userId: String
        get() = authService.currentUser?.uid ?: throw Exception("User not logged in")

    private val userDocument
        get() = db.collection("users").document(userId)

    suspend fun getUserProfile(): UserModel? {
        return try {
            val snapshot = userDocument.get()
            if (snapshot.exists) {
                snapshot.data<UserModel>()
            } else {
                UserModel(uid = userId, email = authService.currentUser?.email ?: "")
            }
        } catch (e: Exception) {
            println("Error fetching user profile: ${e.message}")
            null
        }
    }

    suspend fun saveUserProfile(user: UserModel) {
        userDocument.set(user.copy(uid = userId))
    }
}
