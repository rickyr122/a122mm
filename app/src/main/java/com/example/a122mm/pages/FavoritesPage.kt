package com.example.a122mm.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavoritesPage(modifier: Modifier = Modifier) {
    Column (
        modifier = modifier // ✅ use the passed-in modifier
            .fillMaxSize()
            //.background(Color.Red)
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Favorites Page")
    }
}