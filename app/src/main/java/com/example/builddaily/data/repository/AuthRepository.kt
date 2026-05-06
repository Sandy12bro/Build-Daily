package com.example.builddaily.data.repository

import com.example.builddaily.data.network.NetworkResult
import com.example.builddaily.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow


interface AuthRepository {
    suspend fun signIn(email: String, password: String): NetworkResult<UserInfo>
    suspend fun signUp(email: String, password: String): NetworkResult<UserInfo>
    suspend fun signOut(): NetworkResult<Unit>
    suspend fun getCurrentUser(): Flow<UserInfo?>
}

class AuthRepositoryImpl : AuthRepository {
    override suspend fun signIn(email: String, password: String): NetworkResult<UserInfo> {
        return try {
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = SupabaseClient.client.auth.currentSessionOrNull()?.user
            NetworkResult.Success(user?.let { UserInfo(id = it.id, email = it.email ?: "") } ?: UserInfo(id = "", email = email))
        } catch (e: Exception) {
            NetworkResult.Error(
                message = "Sign in failed: ${e.message}",
                exception = e
            )
        }
    }
    
    override suspend fun signUp(email: String, password: String): NetworkResult<UserInfo> {
        return try {
            SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val user = SupabaseClient.client.auth.currentSessionOrNull()?.user
            NetworkResult.Success(user?.let { UserInfo(id = it.id, email = it.email ?: "") } ?: UserInfo(id = "", email = email))
        } catch (e: Exception) {
            NetworkResult.Error(
                message = "Sign up failed: ${e.message}",
                exception = e
            )
        }
    }
    
    override suspend fun signOut(): NetworkResult<Unit> {
        return try {
            SupabaseClient.client.auth.signOut()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(
                message = "Sign out failed: ${e.message}",
                exception = e
            )
        }
    }
    
    override suspend fun getCurrentUser(): Flow<UserInfo?> {
        return SupabaseClient.client.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    user?.let { UserInfo(id = it.id, email = it.email ?: "") }
                }
                else -> null
            }
        }
    }
}

data class UserInfo(
    val id: String,
    val email: String
)
