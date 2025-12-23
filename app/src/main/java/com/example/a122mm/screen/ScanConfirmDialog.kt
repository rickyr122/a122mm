package com.example.a122mm.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ScanConfirmDialog(
    tvName: String,
    pairCode: String,
    onConfirm: suspend () -> Boolean,
    onCancel: () -> Unit,
    onSuccess: () -> Unit
) {
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Dialog(onDismissRequest = onCancel) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(420.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF1C1C1C))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "To sign in, please confirm the code\nbelow matches this TV: $tvName",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = if (isTablet) 18.sp else 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    text = pairCode,
                    color = Color.White,
                    fontSize = if (isTablet) 32.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (isTablet) 6.sp else 4.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(22.dp))

                if (error != null) {
                    Text(
                        error!!,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // SIGN IN
                Button(
                    onClick = {
                        loading = true
                        error = null
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = if (loading) "Signing in…" else "Sign In to TV",
                        fontSize = if (isTablet) 18.sp else 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(10.dp))

                // CANCEL
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3A3A3A)
                    )
                ) {
                    Text("Cancel", color = Color.White, fontSize = if (isTablet) 18.sp else 15.sp)
                }
            }
        }
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        val ok = onConfirm()
        loading = false
        if (ok) onSuccess()
        else error = "Failed to confirm TV: Code expired please try again"
    }
}