package com.efojug.chatwithme

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

//TODO all support

@Serializable
data class RegisterRequest(val username: String, val password: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val userId: Int, val username: String, val token: String)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(
        val userId: Int, val username: String, val token: String, val serverAddress: String
    ) : AuthState()

    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val client = HttpManager.getInstance()
    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set
    private val json = Json { ignoreUnknownKeys = true }

    fun register(username: String, password: String, address: String) {
        authState = AuthState.Loading
        if (username.isBlank() || password.isBlank() || address.isBlank()) {
            authState = AuthState.Error("Username/Password/ServerAddress cannot be empty")
            return
        } else if (username.length < 3) {
            authState = AuthState.Error("Username must be at least 3 characters long")
            return
        } else if (password.length < 8) {
            authState = AuthState.Error("Password must be at least 8 characters long")
            return
        }
        viewModelScope.launch {
            try {
                val requestBody = json.encodeToString(RegisterRequest(username, password))
                    .toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url("${address}/register").post(requestBody).build()
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                if (response.isSuccessful) {
                    authState = AuthState.Success(
                        userId = 1,
                        username = username,
                        token = "registered",
                        serverAddress = address
                    )
                    Toast.makeText(MyApplication.context, "Register success", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    authState = AuthState.Error("Username already exists")
                    Toast.makeText(
                        MyApplication.context, "Username already exists", Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                authState = AuthState.Error("Failed to register: ${e.toString()}")
            }
        }
    }

    fun login(username: String, password: String, address: String) {
        authState = AuthState.Loading
        if (username.isBlank() || password.isBlank() || address.isBlank()) {
            authState = AuthState.Error("Username/Password/ServerAddress cannot be empty")
            return
        }
        viewModelScope.launch {
            try {
                val requestBody = json.encodeToString(LoginRequest(username, password))
                    .toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url("${address}/login").post(requestBody).build()
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        val loginResponse = json.decodeFromString<LoginResponse>(body)
                        authState = AuthState.Success(
                            userId = loginResponse.userId,
                            username = loginResponse.username,
                            token = loginResponse.token,
                            serverAddress = address
                        )
                        Toast.makeText(MyApplication.context, "Login success", Toast.LENGTH_SHORT)
                            .show()
                    } ?: run {
                        authState = AuthState.Error("Login failed: ${response.message}")
                    }
                } else {
                    authState = AuthState.Error("Invalid username or password")
                    Toast.makeText(
                        MyApplication.context, "Invalid username or password", Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                authState = AuthState.Error("Failed to login: ${e.toString()}")
            }
        }
    }
}