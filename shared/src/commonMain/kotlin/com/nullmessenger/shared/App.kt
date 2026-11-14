package com.nullmessenger.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090F0D)
private val Panel = Color(0xFF121B18)
private val Panel2 = Color(0xFF192520)
private val Mint = Color(0xFF72E6B1)
private val Muted = Color(0xFF91A49C)
private val White = Color(0xFFEAF4EF)
private val Border = Color(0xFF263630)
private val Gold = Color(0xFFFFD477)

private data class DemoChat(val name: String, val username: String, val preview: String, val avatar: String, val unread: Int = 0)

@Composable
fun NullMessengerApp() {
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var authBusy by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    var screen by remember { mutableStateOf("auth") }
    val authScope = rememberCoroutineScope()
    var selectedChat by remember { mutableStateOf<DemoChat?>(null) }
    var profileName by remember { mutableStateOf("NULL User") }
    var profileUsername by remember { mutableStateOf("null_user") }
    var profileBio by remember { mutableStateOf("В мессенджере NULL") }
    var usernameNotice by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val chats = remember {
        listOf(
            DemoChat("Илья", "ilya_dev", "Привет! Как дела?", "И", 2),
            DemoChat("NULL Community", "null_community", "Добро пожаловать в сообщество", "N", 5),
            DemoChat("Алекс", "alex_play", "Скинул тебе подарок 🎁", "А")
        )
    }
    val collectedGifts = remember { mutableStateListOf<String>() }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Mint, onPrimary = Ink,
        onBackground = White, onSurface = White, secondary = Muted
    )) {
        when (screen) {
            "auth" -> EmailAuthScreen(
                email = email, onEmailChange = { email = it; notice = "" },
                otp = otp, onOtpChange = { otp = it.filter(Char::isDigit).take(8); notice = "" },
                codeSent = codeSent, busy = authBusy, notice = notice,
                onSendCode = {
                    if (!email.matches(Regex("^[A-Za-z0-9._%+\\\\-]+@[A-Za-z0-9.\\\\-]+\\\\.[A-Za-z]{2,}$"))) {
                        notice = "Введи корректный адрес электронной почты."
                    } else authScope.launch {
                        authBusy = true
                        try { NullAuth.sendEmailCode(email.trim()); codeSent = true; notice = "Код отправлен на ${email.trim()}. Проверь входящие и папку «Спам»." }
                        catch (e: Exception) { notice = e.message ?: "Не удалось отправить письмо. Попробуй ещё раз." }
                        finally { authBusy = false }
                    }
                },
                onVerifyCode = {
                    if (otp.length < 6) notice = "Введи код из письма."
                    else authScope.launch {
                        authBusy = true
                        try { NullAuth.verifyEmailCode(email.trim(), otp); screen = "chats"; notice = "" }
                        catch (e: Exception) { notice = e.message ?: "Код неверный или истёк. Запроси новый." }
                        finally { authBusy = false }
                    }
                },
                onBack = { codeSent = false; otp = ""; notice = "" },
                onDemo = { screen = "chats" }
            )
            "chats" -> MessengerShell("Чаты", onTab = { screen = it }) {
                Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                    SearchField(searchQuery, onQueryChange = { searchQuery = it })
                    Spacer(Modifier.height(14.dp))
                    Text("НЕДАВНИЕ", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chats.filter { it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery.removePrefix("@"), ignoreCase = true) }) { chat ->
                            ChatRow(chat) { selectedChat = chat; screen = "dialog" }
                        }
                    }
                }
            }
            "dialog" -> {
                val chat = selectedChat
                if (chat != null) DialogScreen(chat, onBack = { screen = "chats" })
                else screen = "chats"
            }
            "gifts" -> MessengerShell("Подарки", onTab = { screen = it }) {
                GiftsScreen(collectedGifts)
            }
            "profile" -> MessengerShell("Профиль", onTab = { screen = it }) {
                ProfileScreen(
                    name = profileName, onName = { profileName = it },
                    username = profileUsername, onUsername = { profileUsername = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' } },
                    bio = profileBio, onBio = { profileBio = it },
                    notice = usernameNotice,
                    onSave = {
                        usernameNotice = if (profileUsername.length < 4) "Username должен содержать минимум 4 символа."
                        else "Изменения сохранены только в этом демо-сеансе."
                    }
                )
            }
        }
    }
}

@Composable
private fun EmailAuthScreen(
    email: String, onEmailChange: (String) -> Unit,
    otp: String, onOtpChange: (String) -> Unit,
    codeSent: Boolean, busy: Boolean, notice: String,
    onSendCode: () -> Unit, onVerifyCode: () -> Unit,
    onBack: () -> Unit, onDemo: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Ink).padding(20.dp), contentAlignment = Alignment.Center) {
        val compact = maxWidth < 600.dp
        Column(
            Modifier.widthIn(max = 440.dp).fillMaxWidth()
                .background(Panel, RoundedCornerShape(28.dp))
                .padding(if (compact) 22.dp else 34.dp)
        ) {
            Brand()
            Spacer(Modifier.height(32.dp))
            Text(if (codeSent) "Проверь почту" else "Вход в NULL", color = White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                if (codeSent) "Мы отправили одноразовый код на $email. Введи его ниже, чтобы подтвердить вход."
                else "Войди или создай аккаунт NULL Messenger. Мы отправим код подтверждения на электронную почту.",
                color = Muted, fontSize = 14.sp, lineHeight = 21.sp
            )
            Spacer(Modifier.height(22.dp))
            if (!codeSent) {
                Text("ЭЛЕКТРОННАЯ ПОЧТА", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email, onValueChange = onEmailChange, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("name@example.com", color = Muted) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Border, focusedTextColor = White, unfocusedTextColor = White, cursorColor = Mint)
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onSendCode, enabled = !busy, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink)) {
                    Text(if (busy) "Отправляем…" else "Получить код", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("КОД ИЗ ПИСЬМА", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = otp, onValueChange = onOtpChange, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("123456", color = Muted) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Border, focusedTextColor = White, unfocusedTextColor = White, cursorColor = Mint)
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onVerifyCode, enabled = !busy, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink)) {
                    Text(if (busy) "Проверяем…" else "Подтвердить и войти", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onBack, enabled = !busy) { Text("Изменить почту", color = Muted) }
            }
            if (notice.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(notice, color = if (notice.startsWith("Код отправлен")) Mint else Gold, fontSize = 12.sp, lineHeight = 18.sp)
            }
            Spacer(Modifier.height(18.dp))
            OutlinedButton(onClick = onDemo, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(15.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                Text("Посмотреть приложение · демо", color = White, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            Text("Вход подтверждается через Supabase Auth. Чаты пока работают в демонстрационном режиме.", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun Brand() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).background(Mint, RoundedCornerShape(15.dp)), Alignment.Center) {
            Text("N", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("NULL", color = White, fontSize = 21.sp, fontWeight = FontWeight.Black)
            Text("MESSENGER", color = Muted, fontSize = 10.sp, letterSpacing = 2.3.sp)
        }
    }
}

@Composable
private fun MessengerShell(title: String, onTab: (String) -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(Ink)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Brand()
            Spacer(Modifier.weight(1f))
            Text(title.uppercase(), color = Muted, fontSize = 11.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.weight(1f)) { content() }
        Row(Modifier.fillMaxWidth().background(Panel).padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavItem("◉", "Чаты", title == "Чаты") { onTab("chats") }
            NavItem("✦", "Подарки", title == "Подарки") { onTab("gifts") }
            NavItem("●", "Профиль", title == "Профиль") { onTab("profile") }
        }
    }
}

@Composable
private fun NavItem(symbol: String, label: String, active: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(symbol, color = if (active) Mint else Muted, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(label, color = if (active) Mint else Muted, fontSize = 11.sp)
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Поиск чатов и людей", color = Muted) }, singleLine = true,
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Border, focusedTextColor = White, unfocusedTextColor = White, cursorColor = Mint)
    )
}

@Composable
private fun ChatRow(chat: DemoChat, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).background(Panel2, CircleShape), Alignment.Center) {
            Text(chat.avatar, color = Mint, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(chat.name, color = White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("@${chat.username}", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(3.dp))
            Text(chat.preview, color = Muted, fontSize = 12.sp, maxLines = 1)
        }
        if (chat.unread > 0) {
            Box(Modifier.background(Mint, CircleShape).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(chat.unread.toString(), color = Ink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DialogScreen(chat: DemoChat, onBack: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val messages = remember(chat.username) {
        mutableStateListOf("Привет! Это демонстрационный чат NULL 👋", "Здесь появятся настоящие сообщения после подключения сервера.")
    }
    Column(Modifier.fillMaxSize().background(Ink)) {
        Row(Modifier.fillMaxWidth().background(Panel).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = Mint, fontSize = 30.sp, modifier = Modifier.clickable(onClick = onBack))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(chat.name, color = White, fontWeight = FontWeight.Bold)
                Text("@${chat.username}", color = Muted, fontSize = 11.sp)
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(messages) { message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(message, Modifier.widthIn(max = 300.dp).background(Panel2, RoundedCornerShape(16.dp)).padding(12.dp), color = White, fontSize = 13.sp)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("Сообщение…", color = Muted) }, singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Border, focusedTextColor = White, unfocusedTextColor = White, cursorColor = Mint)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { if (draft.isNotBlank()) { messages.add(draft.trim()); draft = "" } }, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink)) {
                Text("➤")
            }
        }
        Text("Демо-чат: сообщения хранятся только на этом экране и не доставляются собеседнику.", Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp), color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun GiftsScreen(collected: MutableList<String>) {
    val gifts = listOf(
        Triple("Зелёный кристалл", "Обычный", "💎"),
        Triple("NULL звезда", "Редкий", "🌟"),
        Triple("Призрачный куб", "Эпический", "👻"),
        Triple("Корона NULL", "Легендарный", "👑")
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("Коллекция NULL", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text("Собрано: ${collected.size} из ${gifts.size}", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(gifts) { gift ->
                val owned = gift.first in collected
                Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(gift.third, fontSize = 30.sp)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(gift.first, color = White, fontWeight = FontWeight.SemiBold)
                        Text(gift.second, color = if (gift.second == "Легендарный") Gold else Muted, fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = { if (owned) collected.remove(gift.first) else collected.add(gift.first) }, shape = RoundedCornerShape(12.dp)) {
                        Text(if (owned) "Убрать" else "В коллекцию", color = Mint, fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Это виртуальные демо-подарки. Покупка, передача и блокчейн/NFT не реализованы.", color = Muted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun ProfileScreen(
    name: String, onName: (String) -> Unit,
    username: String, onUsername: (String) -> Unit,
    bio: String, onBio: (String) -> Unit,
    notice: String, onSave: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(22.dp)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(76.dp).background(Panel2, CircleShape), Alignment.Center) {
                Text(name.take(1).uppercase().ifBlank { "N" }, color = Mint, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text(name.ifBlank { "Твоё имя" }, color = White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text("@${username.ifBlank { "username" }}", color = Mint, fontSize = 12.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text("ИМЯ", color = Muted, fontSize = 10.sp, letterSpacing = 1.3.sp)
        OutlinedTextField(name, onName, Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Border, focusedTextColor = White, unfocusedTextColor = White))
        Spacer(Modifier.height(10.dp))
        Text("USERNAME", color = Muted, fontSize = 10.sp, letterSpacing = 1.3.sp)
        OutlinedTextField(username, onUsername, Modifier.fillMaxWidth(), prefix = { Text("@", color = Mint) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Border, focusedTextColor = White, unfocusedTextColor = White))
        Spacer(Modifier.height(10.dp))
        Text("О СЕБЕ", color = Muted, fontSize = 10.sp, letterSpacing = 1.3.sp)
        OutlinedTextField(bio, onBio, Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Border, focusedTextColor = White, unfocusedTextColor = White))
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink)) {
            Text("Сохранить профиль", fontWeight = FontWeight.Bold)
        }
        if (notice.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(notice, color = Gold, fontSize = 11.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text("Пока это локальный профиль демо-режима. Уникальность username проверит сервер после подключения базы.", color = Muted, fontSize = 10.sp, lineHeight = 15.sp)
    }
}
