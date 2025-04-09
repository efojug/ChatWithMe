package com.efojug.chatwithme

import com.efojug.chatwithme.error.ConnectionClosed
import com.efojug.chatwithme.error.FailedToConnectServer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object ChatWebSocketManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val mutableMessageChannel = MutableSharedFlow<String>()
    val messageChannel: SharedFlow<String> = mutableMessageChannel.asSharedFlow()

    private val mutableErrorFlow = MutableSharedFlow<Exception>()
    val errorFlow: SharedFlow<Exception> = mutableErrorFlow.asSharedFlow()

    suspend fun connect(url: String) = suspendCoroutine { continuation ->
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                continuation.resume(Unit)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                mutableMessageChannel.tryEmit(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                mutableErrorFlow.tryEmit(ConnectionClosed(code, reason))
                webSocket.close(code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
                continuation.resumeWithException(FailedToConnectServer(t.toString()))
            }
        })
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnected")
    }
}