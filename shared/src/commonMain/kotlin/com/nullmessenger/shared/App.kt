package com.nullmessenger.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF0B1110)
private val Panel = Color(0xFF121B19)
private val PanelSoft = Color(0xFF1B2925)
private val Mint = Color(0xFF72E6B1)
private val Muted = Color(0xFF91A49C)
private val White = Color(0xFFEAF4EF)

private data class ChatPreview(val name: String, val message: String, val time: String, val tint: Color)

@Composable
fun NullMessengerApp() {
    val chats = remember {
        listOf(
            ChatPreview("NULL Team", "Добро пожаловать в NULL Messenger", "сейчас", Color(0xFF55C99A)),
            ChatPreview("Друзья", "Увидимся вечером 👋", "21:42", Color(0xFF7899E8)),
            ChatPreview("Избранное", "Твои сохранённые сообщения", "вчера", Color(0xFFD6A86E))
        )
    }
    var selected by remember { mutableIntStateOf(0) }
    var draft by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(listOf<String>()) }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Mint, onPrimary = Ink,
        onBackground = White, onSurface = White, secondary = Muted
    )) {
        Row(Modifier.fillMaxSize().background(Ink).padding(14.dp)) {
            Column(
                Modifier.width(290.dp).fillMaxHeight()
                    .background(Panel, RoundedCornerShape(24.dp)).padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(Mint, RoundedCornerShape(14.dp)), Alignment.Center) {
                        Text("N", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("NULL", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("MESSENGER", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                    }
                }
                Spacer(Modifier.height(28.dp))
                Text("СООБЩЕНИЯ", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(chats) { index, chat ->
                        Row(
                            Modifier.fillMaxWidth()
                                .background(if (selected == index) PanelSoft else Color.Transparent, RoundedCornerShape(16.dp))
                                .clickable { selected = index; sent = emptyList() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(42.dp).background(chat.tint.copy(alpha = .18f), CircleShape), Alignment.Center) {
                                Text(chat.name.take(1), color = chat.tint, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(chat.name, color = White, fontWeight = FontWeight.SemiBold)
                                Text(chat.message, color = Muted, fontSize = 12.sp, maxLines = 1)
                            }
                            Text(chat.time, color = Muted, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier.fillMaxWidth().background(PanelSoft, RoundedCornerShape(16.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(34.dp).background(Mint.copy(alpha = .18f), CircleShape), Alignment.Center) {
                        Text("U", color = Mint, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Твой профиль", color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Пока без авторизации", color = Muted, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.width(14.dp))
            Column(
                Modifier.weight(1f).fillMaxHeight()
                    .background(Panel, RoundedCornerShape(24.dp)).padding(22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(chats[selected].tint.copy(alpha = .18f), CircleShape), Alignment.Center) {
                        Text(chats[selected].name.take(1), color = chats[selected].tint, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(chats[selected].name, color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("NULL Messenger • тестовый экран", color = Muted, fontSize = 11.sp)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 18.dp), color = PanelSoft)
                Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Bottom) {
                    Text("Привет! Это начало NULL Messenger.", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Общий интерфейс для Windows и Android уже заложен. Подключение аккаунта NULL и настоящая доставка сообщений будут следующим этапом.",
                        color = Muted, fontSize = 13.sp, lineHeight = 19.sp
                    )
                    sent.forEach { item ->
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(item, color = Ink, modifier = Modifier.background(Mint, RoundedCornerShape(16.dp)).padding(12.dp))
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Написать сообщение…", color = Muted) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Mint, unfocusedBorderColor = PanelSoft,
                            focusedTextColor = White, unfocusedTextColor = White, cursorColor = Mint
                        )
                    )
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (draft.isNotBlank()) {
                                sent = sent + draft.trim()
                                draft = ""
                            }
                        },
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
                    ) { Text("Отправить", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
