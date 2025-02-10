package com.efojug.chatwithme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType

@Serializable
data class RegisterRequest(val username: String, val password: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val userId: Int, val username: String, val token: String)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: Int, val username: String, val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val client = OkHttpClient()
    var authState: AuthState = AuthState.Idle
        private set
    private val json = Json { ignoreUnknownKeys = true }

    fun register(username: String, password: String) {
        authState = AuthState.Loading
        viewModelScope.launch {
            val requestBody = RequestBody.create(
                "application/json".toMediaType(),
                json.encodeToString(RegisterRequest(username, password))
            )
            val request =
                Request.Builder().url(BuildConfig.REGISTER_SERVER_URL).post(requestBody).build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    authState =
                        AuthState.Success(userId = 1, username = username, token = "registered")
                } else {
                    authState = AuthState.Error("Failed to register: ${response.message}")
                }
            } catch (e: Exception) {
                authState = AuthState.Error("Failed to register: ${e.message}")
            }
        }
    }

    fun login(username: String, password: String) {
        authState = AuthState.Loading
        viewModelScope.launch {
            val requestBody = RequestBody.create(
                "application/json".toMediaType(),
                json.encodeToString(LoginRequest(username, password))
            )
            val request =
                Request.Builder().url(BuildConfig.LOGIN_SERVER_URL).post(requestBody).build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        val loginResponse = json.decodeFromString<LoginResponse>(body)
                        authState = AuthState.Success(
                            userId = loginResponse.userId,
                            username = loginResponse.username,
                            token = loginResponse.token
                        )
                    } ?: run {
                        authState = AuthState.Error("Empty response")
                    }
                } else {
                    authState = AuthState.Error("Login Failed: ${response.message}")
                }
            } catch (e: Exception) {
                authState = AuthState.Error("Login Failed: ${e.message}")
            }
        }
    }
}