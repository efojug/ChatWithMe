package com.efojug.chatwithme.error

class FailedToConnectServer : Exception("Failed to connect to server")

class FailedToSendMessage : Exception("Failed to send message")

class ConnectionClosed(val code: Int, val reason: String): Exception()