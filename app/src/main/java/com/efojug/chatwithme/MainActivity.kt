package com.efojug.chatwithme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efojug.chatwithme.error.FailedToConnectServer
import com.efojug.chatwithme.ui.theme.ChatWithMeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

var connectionState = "None"

//message data model
data class Message(
    val userId: Int, val content: String, val time: Long = System.currentTimeMillis()
)

//viewmodel
class ChatViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(State())
    val state: StateFlow<State> = mutableState
    var currentUserId = 0

    init {
        viewModelScope.launch {
            try {
                ChatWebSocketManager.connect(BuildConfig.CHAT_SERVER_URL)
            } catch (e: FailedToConnectServer) {
                mutableState.update {
                    it.copy(
                        isLoading = false, isOffline = true
                    )
                }
                return@launch
            }

            launch {
                ChatWebSocketManager.messageChannel.collect { text ->
                    val incomingMessage = Message(content = text, userId = 2)
                    mutableState.getAndUpdate {
                        it.copy(
                            message = it.message + incomingMessage
                        )
                    }
                }
            }

            launch {
                ChatWebSocketManager.errorFlow.collect {
                    mutableState.getAndUpdate {
                        val newMessage = it.message.dropLast(1).toMutableList()
                        newMessage.add(
                            it.message.last()
                                .copy(content = "Send Failed: ${it.message.last().content}")
                        )

                        it.copy(
                            message = newMessage
                        )
                    }
                }
            }
        }
    }

    //simulate message send
    fun sendMessage(message: String) {
        val userMessage = Message(content = message, userId = currentUserId)
        mutableState.getAndUpdate {
            it.copy(
                message = it.message + userMessage
            )
        }
        ChatWebSocketManager.send(message)

        //reply
        simulateMessage(connectionState)
    }

    //simulate reply
    fun simulateMessage(message: String, userId: Int = -1) {
        viewModelScope.launch {
            delay(1000L)
            val reply = Message(content = "got it: $message", userId = userId)
            mutableState.getAndUpdate {
                it.copy(
                    message = it.message + reply
                )
            }
        }
    }

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val isOffline: Boolean = false,
        val message: List<Message> = emptyList()
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            var loggedIn by remember { mutableStateOf(false) }
            var currentUserId by remember { mutableStateOf(0) }
            var currentUsername by remember { mutableStateOf("") }
            var currentToken by remember { mutableStateOf("") }
            ChatWithMeTheme {
                if (!loggedIn) {
                    LoginScreen(onLoginSuccess = { userId, username, token ->
                        currentUserId = userId
                        currentUsername = username
                        currentToken = token
                        loggedIn = true
                    })
                } else {
                    ChatScreen(
                        userId = currentUserId, username = currentUsername, token = currentToken
                    )
                }
            }
        }
    }
}