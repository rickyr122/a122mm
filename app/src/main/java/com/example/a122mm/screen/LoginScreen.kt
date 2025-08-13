package com.example.a122mm.screen

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.example.a122mm.R
import com.example.a122mm.helper.setScreenOrientation

@Composable
fun LoginScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    val customColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.Gray,
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.Gray,
        cursorColor = Color.White
    )

    val view = LocalView.current
    val activity = LocalContext.current as Activity

    SideEffect {
        val window = activity.window

        // Edge-to-edge with transparent bars
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.BLACK // or any color you want

        val insetsController = WindowCompat.getInsetsController(window, view)

        // Status bar icons
        insetsController.isAppearanceLightStatusBars = false  // dark icons

        // Navigation bar icons
        insetsController.isAppearanceLightNavigationBars = false // light icons for dark nav bar
    }

    Scaffold (
        //modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
        containerColor = Color.Black,
        content = { padding ->
            Box (modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                        .windowInsetsPadding(WindowInsets.systemBars)
            ) {
                Column (
                    modifier = modifier //.fillMaxSize()
                        .padding(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.a122mm_logo),
                        contentDescription = "Login Banner",
                      modifier= Modifier
                           .height(70.dp)
                    )
                }
                Column (
                    modifier = modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement =  Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField (
                        value = email,
                        maxLines = 1,
                        onValueChange = {
                            email = it
                        },
                        label = {
                            Text(text = "Email")
                        },
                        colors = customColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField (
                        value = password,
                        maxLines = 1,
                        onValueChange = {
                            password = it
                        },
                        label = {
                            Text(text = "Password")
                        },
                        colors = customColors,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = {
                        navController.navigate("home")
                    },
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(text = "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = "OR",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        style = TextStyle(
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(15.dp))
                    Button(onClick = {
                        navController.navigate("signup")
                    },
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(text = "Sign Up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }

        }
    )
}