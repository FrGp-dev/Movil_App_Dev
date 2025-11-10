package com.dadm.chatdadm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow(listOf<Message>())
    val messages: StateFlow<List<Message>> = _messages

    private val model: GenerativeModel = Firebase.ai.generativeModel("gemini-2.5-flash")
    private val chat = model.startChat()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _isLoading.value) return

        val userMessage = Message(prompt, isUser = true)
        _messages.update { it + userMessage }

        _isLoading.value = true

        viewModelScope.launch {
            try {

                val response = chat.sendMessage(prompt)

                val modelResponse = Message(response.text ?: "Error: No se pudo generar la respuesta.", isUser = false)
                _messages.update { it + modelResponse }

            } catch (e: Exception) {
                val errorMessage = Message("Error de IA: ${e.localizedMessage ?: "Error desconocido"}", isUser = false)
                _messages.update { it + errorMessage }
            } finally {
                _isLoading.value = false
            }
        }
    }
}