package com.efojug.chatwithme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (Int, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var serverAddress by remember { mutableStateOf("") }
    var saveCredentialsChecked by remember { mutableStateOf(false) }
    var autoLoginChecked by remember { mutableStateOf(false) }
    val authState = authViewModel.authState

    fun saveCredential() {
        coroutineScope.launch {
            dataStoreManager.saveUsername(username)
            dataStoreManager.saveServerAddress(serverAddress)
            if (saveCredentialsChecked) {
                dataStoreManager.saveSaveCredentials(true)
                dataStoreManager.savePassword(password)
                dataStoreManager.saveAutoLogin(autoLoginChecked)
            } else {
                dataStoreManager.clearPassword()
            }
        }
    }

    LaunchedEffect(Unit) {
        dataStoreManager.saveCredentialsFlow.collect { value ->
            saveCredentialsChecked = value
        }
    }

    LaunchedEffect(Unit) {
        dataStoreManager.usernameFlow.collect { storedUsername ->
            if (storedUsername != null) {
                username = storedUsername
            }
        }
    }

    LaunchedEffect(Unit) {
        dataStoreManager.serverAddressFlow.collect { storedServerAddress ->
            if (storedServerAddress != null) {
                serverAddress = storedServerAddress
            }
        }
    }

    if (saveCredentialsChecked) {
        LaunchedEffect(Unit) {
            dataStoreManager.passwordFlow.collect { storedPassword ->
                if (storedPassword != null) {
                    password = storedPassword
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        dataStoreManager.autoLoginFlow.collect { storedAutoLogin ->
            autoLoginChecked = storedAutoLogin
            if (saveCredentialsChecked && autoLoginChecked) {
                delay(500L)
                authViewModel.login(username, password, serverAddress)
                saveCredential()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = serverAddress,
            onValueChange = { serverAddress = it },
            label = { Text("Server Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(checked = saveCredentialsChecked, onCheckedChange = {
                saveCredentialsChecked = it
                coroutineScope.launch {
                    dataStoreManager.saveSaveCredentials(it)
                }
            })
            Spacer(modifier = Modifier.height(2.dp))
            Text("Save Credentials")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = autoLoginChecked, onCheckedChange = {
                    autoLoginChecked = it
                    coroutineScope.launch {
                        dataStoreManager.saveAutoLogin(it)
                    }
                }, enabled = saveCredentialsChecked
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text("Auto Login")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                authViewModel.login(username, password, serverAddress)
                saveCredential()
            }) { Text("Login") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                authViewModel.register(username, password, serverAddress)
                saveCredential()
            }) { Text("Register") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (authState) {
            is AuthState.Loading -> {
                CircularProgressIndicator()
            }

            is AuthState.Error -> {
                Text(text = authState.message, color = MaterialTheme.colorScheme.error)
            }

            is AuthState.Success -> {
                // 登录/注册成功后通知上层切换界面
                LaunchedEffect(authState) {
                    onLoginSuccess(
                        authState.userId, authState.username, authState.token, serverAddress
                    )
                }
            }

            else -> {}
        }
    }
}