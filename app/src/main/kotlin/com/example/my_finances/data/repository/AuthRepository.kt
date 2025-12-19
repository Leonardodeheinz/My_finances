package com.example.my_finances.data.repository

import android.content.Context
import com.example.my_finances.data.model.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Email/Password Sign Up
    suspend fun signUpWithEmail(email: String, password: String): Flow<AuthResult<FirebaseUser>> = flow {
        try {
            emit(AuthResult.Loading)
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                emit(AuthResult.Success(it))
            } ?: emit(AuthResult.Error("Sign up failed"))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "An error occurred during sign up"))
        }
    }

    // Email/Password Sign In
    suspend fun signInWithEmail(email: String, password: String): Flow<AuthResult<FirebaseUser>> = flow {
        try {
            emit(AuthResult.Loading)
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                emit(AuthResult.Success(it))
            } ?: emit(AuthResult.Error("Sign in failed"))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "An error occurred during sign in"))
        }
    }

    // Google Sign-In Client
    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID") // Replace with your Web Client ID from Firebase Console
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    // Google Sign In with Firebase
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Flow<AuthResult<FirebaseUser>> = flow {
        try {
            emit(AuthResult.Loading)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let {
                emit(AuthResult.Success(it))
            } ?: emit(AuthResult.Error("Google sign in failed"))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "An error occurred during Google sign in"))
        }
    }

    // Sign Out
    fun signOut() {
        auth.signOut()
        getGoogleSignInClient().signOut()
    }

    // Password Reset
    suspend fun sendPasswordResetEmail(email: String): Flow<AuthResult<Unit>> = flow {
        try {
            emit(AuthResult.Loading)
            auth.sendPasswordResetEmail(email).await()
            emit(AuthResult.Success(Unit))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to send reset email"))
        }
    }

    // Delete Account
    suspend fun deleteAccount(): Flow<AuthResult<Unit>> = flow {
        try {
            emit(AuthResult.Loading)
            auth.currentUser?.delete()?.await()
            emit(AuthResult.Success(Unit))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to delete account"))
        }
    }

    // Get Current User
    suspend fun getCurrentUser(): Flow<AuthResult<FirebaseUser>> = flow {
        try {
            emit(AuthResult.Loading)
            val user = auth.currentUser
            if (user != null) {
                emit(AuthResult.Success(user))
            } else {
                emit(AuthResult.Error("No user logged in"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to get current user"))
        }
    }

    // Change Password
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Flow<AuthResult<Unit>> = flow {
        try {
            emit(AuthResult.Loading)
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val email = user.email ?: throw Exception("No email associated with account")

            // Re-authenticate with current password
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            // Update password
            user.updatePassword(newPassword).await()
            emit(AuthResult.Success(Unit))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to change password"))
        }
    }
}
