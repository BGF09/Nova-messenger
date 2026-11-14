package com.nullmessenger.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.nullmessenger.shared.NullMessengerApp

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "NULL Messenger") {
        NullMessengerApp()
    }
}
