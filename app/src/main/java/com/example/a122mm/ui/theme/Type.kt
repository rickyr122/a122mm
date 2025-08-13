package com.example.a122mm.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.a122mm.R

// Set of Material typography styles to start with
val NetflixFont = FontFamily(
    Font(R.font.netflixsansregular)
)

// Set it as the default typography
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = NetflixFont,
        fontSize = 36.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NetflixFont,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NetflixFont,
        fontSize = 14.sp
    )
    // Add other styles if needed
)