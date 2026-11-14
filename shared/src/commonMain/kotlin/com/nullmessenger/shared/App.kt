package com.nullmessenger.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090F0D)
private val Panel = Color(0xFF121B18)
private val Mint = Color(0xFF72E6B1)
private val Muted = Color(0xFF91A49C)
private val White = Color(0xFFEAF4EF)
private val Border = Color(0xFF263630)

@Composable
fun NullMessengerApp() {
    var phone by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Mint, onPrimary = Ink,
        onBackground = White, onSurface = White, secondary = Muted
    )) {
        BoxWithConstraints(
            Modifier.fillMaxSize().background(Ink).padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            val compact = maxWidth < 600.dp
            Column(
                Modifier.widthIn(max = 440.dp).fillMaxWidth()
                    .background(Panel, RoundedCornerShape(28.dp))
                    .padding(if (compact) 22.dp else 34.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(Mint, RoundedCornerShape(16.dp)), Alignment.Center) {
                        Text("N", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(13.dp))
                    Column {
                        Text("NULL", color = White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("MESSENGER", color = Muted, fontSize = 10.sp, letterSpacing = 2.4.sp)
                    }
                }
                Spacer(Modifier.height(34.dp))
                Text("Твой номер телефона", color = White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Войди или создай аккаунт NULL Messenger. Мы будем использовать номер для подтверждения входа.",
                    color = Muted, fontSize = 14.sp, lineHeight = 21.sp
                )
                Spacer(Modifier.height(24.dp))
                Text("НОМЕР ТЕЛЕФОНА", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' || ch == '-' || ch == '(' || ch == ')' }
                        notice = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("+7 900 000-00-00", color = Muted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Mint, unfocusedBorderColor = Border,
                        focusedTextColor = White, unfocusedTextColor = White, cursorColor = Mint
                    )
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        notice = if (phone.filter(Char::isDigit).length < 7) "Проверь номер телефона."
                        else "SMS пока не подключены. Нужен SMS-провайдер и настройка Supabase Auth."
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink)
                ) { Text("Продолжить", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                if (notice.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(notice, color = if (notice.startsWith("Проверь")) Color(0xFFFFC27A) else Muted, fontSize = 12.sp, lineHeight = 18.sp)
                }
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(16.dp))
                Text("После подтверждения номера ты сможешь:", color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                FeatureLine("создать уникальный @username")
                FeatureLine("добавить имя и аватар")
                FeatureLine("находить друзей и отправлять подарки")
                Spacer(Modifier.height(18.dp))
                Text(
                    "SMS-подтверждение ещё не настроено. Приложение не отправляет фиктивные коды и не создаёт аккаунт без проверки номера.",
                    color = Muted, fontSize = 11.sp, lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun FeatureLine(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("•", color = Mint, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(9.dp))
        Text(label, color = Muted, fontSize = 13.sp)
    }
}
