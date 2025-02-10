package com.efojug.chatwithme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

//TODO need login page to get userId
val currentUserId = 0
var connectionState = "None"

//message data model
data class Message(
    val content: String, val userId: Int, val time: Long = System.currentTimeMillis()
)

//viewmodel
class ChatViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(State())
    val state: StateFlow<State> = mutableState

    init {
        viewModelScope.launch {
            try {
                ChatWebSocketManager.connect(BuildConfig.CHAT_SERVER_BACKEND_URL)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = ChatViewModel()) {
    //check message list
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "Chat") },
        )
    }) { contentPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Connecting...")
            }
            return@Scaffold
        }

        if (state.isOffline) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Failed to connect server !!!", color = Color.Red)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            //message list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(state.message) { message ->
                    MessageBubble(message = message)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            ChatInput(onSend = { text -> viewModel.sendMessage(text) })
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val bubbleColor =
        if (message.userId == currentUserId) MaterialTheme.colorScheme.primary else Color.LightGray
    val alignment = if (message.userId == currentUserId) Arrangement.End else Arrangement.Start

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = alignment) {
        Box(
            modifier = Modifier
                .background(bubbleColor, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
        ) {
            Text(
                text = message.content,
                color = if (message.userId == currentUserId) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun ChatInput(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f))
        IconButton(onClick = {
            if (text.isNotBlank()) {
                onSend(text)
                text = ""
            }
        }) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatWithMeTheme {
                ChatScreen()
            }
        }
    }
}