package com.example.linarkursach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
class ChatRoom : ComponentActivity() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var messageRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val intent = intent
            if (intent.hasExtra("Название")) {
                val name = intent.getStringExtra("Название")
                messageRef = database.reference.child(name!!)
            } else {
            }
            ChatScreen()
        }
    }

    @Composable
    fun ChatScreen() {
        var messageText by remember { mutableStateOf("") }
        var chatMessages by remember { mutableStateOf(listOf<Pair<String, String>>()) }
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser!!.email
        val listState = rememberLazyListState()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f)
            ) {
                items(chatMessages) { (sender, message) ->
                    val isOwnMessage = sender == currentUser.toString() // Замените на имя вашего отправителя
                    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .align(alignment)
                            .widthIn(max = 250.dp) // Максимальная ширина сообщения
                    ) {
                        Text(
                            text = "$sender: $message",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.isNotBlank()) {
                                sendMessage(messageText)
                                messageText = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            sendMessage(messageText)
                            messageText = ""
                        }
                    }
                ) {
                    Text("Отправить")
                }
            }
        }

    // Отслеживаем изменения в Firebase и обновляем сообщения
        messageRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessages = mutableListOf<Pair<String, String>>()
                snapshot.children.forEach { messageSnapshot ->
                    val sender = messageSnapshot.child("sender").value.toString()
                    val message = messageSnapshot.child("text").value.toString()
                    newMessages.add(Pair(sender, message))
                }
                chatMessages = newMessages
            }

            override fun onCancelled(error: DatabaseError) {
                // Обработка ошибок при чтении данных
            }
        })
    }

    private fun sendMessage(message: String) {
        val messageKey = messageRef!!.push().key
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser!!.email
        messageKey?.let {
            val sender = currentUser.toString() // Замените на имя отправителя
            val messageData = mapOf(
                "text" to message,
                "sender" to sender
            )
            messageRef!!.child(it).setValue(messageData)
        }
    }
}
