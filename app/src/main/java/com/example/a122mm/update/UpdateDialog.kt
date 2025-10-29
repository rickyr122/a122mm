package com.example.a122mm.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun UpdateDialog(
    visible: Boolean,
    progress: Int,
    cancellable: Boolean = false,
    onCancel: (() -> Unit)? = null
) {
    if (!visible) return
    Dialog(onDismissRequest = { /* block closing to make it "uninterruptable" in-app */ }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Downloading update…", color = Color.White)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = (progress.coerceIn(0, 100)) / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text("$progress%", color = Color(0xFFB3B3B3))
            if (cancellable && onCancel != null) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}
