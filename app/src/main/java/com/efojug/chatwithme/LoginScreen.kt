package com.efojug.chatwithme

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.material3.*
import androidx.compose.ui.Alignment

@Composable
fun LoginScreen(authViewModel: AuthViewModel = viewModel(), onLoginSuccess: (Int, String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState = authViewModel.authState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { authViewModel.login(username, password) }) { Text("Login") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { authViewModel.register(username, password) }) { Text("Register") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (authState) {
            is AuthState.Loading -> { CircularProgressIndicator() }
            is AuthState.Error -> { Text(text = authState.message, color = MaterialTheme.colorScheme.error) }
            is AuthState.Success -> {
                // 登录/注册成功后通知上层切换界面
                LaunchedEffect(authState) {
                    onLoginSuccess(authState.userId, authState.username, authState.token)
                }
            }
            else -> {}
        }
    }
}

//@Composable
//fun TextField(value: String, onValueChange: () -> Unit, label: @Composable () -> Text, modifier: Modifier) {
//    TODO("Not yet implemented")
//}
