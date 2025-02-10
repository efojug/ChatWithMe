package com.efojug.chatwithme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MessageBubble(message: Message, currentUserId: Int) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: Int,
    username: String,
    token: String,
    viewModel: ChatViewModel = viewModel<ChatViewModel>()
) {
    LaunchedEffect(userId) { viewModel.currentUserId = userId }
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
                    MessageBubble(message = message, 999)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            ChatInput(onSend = { text -> viewModel.sendMessage(text) })
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