package com.efojug.chatwithme.error

class FailedToConnectServer(exceptionMessage: String) : Exception("Failed to connect to server:${exceptionMessage}")

class FailedToSendMessage : Exception("Failed to send message")

class ConnectionClosed(val code: Int, val reason: String): Exception()