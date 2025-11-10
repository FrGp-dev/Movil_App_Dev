package com.dadm.chatdadm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputMessage by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chatbot DADM - Gemini") }) },

        bottomBar = {
            InputBar(
                inputMessage = inputMessage,
                onMessageChange = { inputMessage = it },
                onSend = {
                    val messageToSend = inputMessage
                    viewModel.sendMessage(messageToSend)
                    inputMessage = ""
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                isSending = isLoading
            )
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            reverseLayout = true,
            state = listState
        ) {
            items(messages.reversed()) { message ->
                // Aplica el padding horizontal a cada burbuja
                MessageBubble(message = message, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

// Burbuja de mensaje
@Composable
fun MessageBubble(message: Message, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.widthIn(min = 70.dp, max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

// Barra de entrada de texto
@Composable
fun InputBar(
    inputMessage: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)

            .windowInsetsPadding(
                WindowInsets.navigationBars
                    .union(WindowInsets.ime)
                    .only(WindowInsetsSides.Bottom)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputMessage,
            onValueChange = onMessageChange,
            label = { Text("Escribe tu mensaje...") },
            modifier = Modifier.weight(1f),
            maxLines = 5,
            enabled = !isSending
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onSend,
            enabled = inputMessage.isNotBlank() && !isSending,
            modifier = Modifier.height(56.dp)
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Enviar mensaje"
                )
            }
        }
    }
}
