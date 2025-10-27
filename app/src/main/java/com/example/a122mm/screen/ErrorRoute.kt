package com.example.a122mm.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ErrorRoute(nav: NavHostController) {
    val backStackEntry = nav.previousBackStackEntry
    val errMsg = backStackEntry
        ?.arguments
        ?.getString("err_msg")
        ?: "A network or unexpected error occurred."

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔻 Icon
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 12.dp)
            )

            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = errMsg,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    nav.navigate("splash") {
                        popUpTo("error") { inclusive = true }
                    }
                }
            ) {
                Text("Try again")
            }
        }
    }
}


