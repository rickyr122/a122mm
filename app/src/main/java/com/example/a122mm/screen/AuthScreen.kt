package com.example.a122mm.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.a122mm.R

@Composable
fun AuthScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    Scaffold(
        containerColor = Color.Black,
        // Sets the background color for the entire app.
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment =  Alignment.CenterHorizontally
                ){
                    Image(
                        painter = painterResource(id = R.drawable.a122mm_logo),
                        contentDescription = "Banner",
                        modifier= Modifier.fillMaxWidth()
                            .height(175.dp)
                    )
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(text = "Watch vast collection of movies & TV shows",
                        color = Color.LightGray,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = {
                        navController.navigate("login")
                    },
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text(text = "Sign In", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Or",
                        fontSize = 20.sp,
                        color = Color.White,
                        style = TextStyle(
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(onClick = {
                        navController.navigate("signup")
                    },
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text(
                            text = "Sign Up",
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }

                }
            }
        }
    )
}

