package com.example.a122mm.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a122mm.R

@Composable
fun ProfileHeader(
    modifier: Modifier = Modifier,
    onDominantColorExtracted: (Color) -> Unit,
    onLogoutClicked: () -> Unit   // 🔹 add this callback
) {
    // Always tell HomeScreen to switch to black
    LaunchedEffect(Unit) {
        onDominantColorExtracted(Color.Black)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile picture
        Image(
            painter = painterResource(id = R.drawable.spiderman), // ✅ your drawable
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape) // circular crop
                .border(2.dp, Color.White, CircleShape) // white border
        )

        // Name below the picture
        Text(
            text = "Ricky R",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp)
        )

        // 🔹 Logout button
        Button(
            onClick = { onLogoutClicked() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            modifier = Modifier
                .padding(top = 12.dp)
                .height(36.dp)
        ) {
            Text(
                text = "LOG OUT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
