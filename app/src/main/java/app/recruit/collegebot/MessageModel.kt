package com.example.collegebot


data class MessageModel(
    val message : String,
    val role : String,
    val isTyping: Boolean = false
)
