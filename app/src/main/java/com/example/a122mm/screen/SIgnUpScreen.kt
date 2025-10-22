package com.example.a122mm.screen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.example.a122mm.R
import com.example.a122mm.auth.AuthRepository
import com.example.a122mm.auth.SignUpViewModel
import com.example.a122mm.auth.TokenStore
import com.example.a122mm.dataclass.AuthNetwork
import com.example.a122mm.helper.setScreenOrientation

@Composable
fun SignUpScreen(modifier: Modifier = Modifier, navController: NavHostController) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        context.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    var email by remember {
        mutableStateOf("")
    }

    var name by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember { mutableStateOf(false) }

    val customColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedLabelColor = Color.Gray,
        unfocusedLabelColor = Color.Gray,
        focusedBorderColor = Color.Gray,
        unfocusedBorderColor = Color.Gray,
        cursorColor = Color.Black,
        errorTextColor = Color.Black,
        errorLabelColor = Color.Red,
        errorBorderColor = Color.Red,
        errorCursorColor = Color.Black
    )

    val repo = remember {
        AuthRepository(
            publicApi = AuthNetwork.publicAuthApi,           // signup uses public API
            authedApi = AuthNetwork.authedAuthApi(context),  // ready for protected later
            store = TokenStore(context)
        )
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val vm = remember { SignUpViewModel(repo) }
    val ui = vm.ui.collectAsState().value

    val view = LocalView.current
    val activity = LocalContext.current as Activity

    SideEffect {
        val window = activity.window

        // Edge-to-edge with transparent bars
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.BLACK // or any color you want

        val insetsController = WindowCompat.getInsetsController(window, view)

        // Status bar icons
        insetsController.isAppearanceLightStatusBars = true  // dark icons

        // Navigation bar icons
        insetsController.isAppearanceLightNavigationBars = false // light icons for dark nav bar
    }

    LaunchedEffect(ui) {
        when (ui) {
            is com.example.a122mm.auth.SignUpUiState.Success -> {
                // go back to Login on successful signup
                navController.navigate("login") {
                    popUpTo("signup") { inclusive = true }
                }
                Toast.makeText(context, "Account created. Please sign in.", Toast.LENGTH_SHORT).show()
            }
            is com.example.a122mm.auth.SignUpUiState.Error -> {
                // repo now surfaces server messages (e.g., "email already registered")
                Toast.makeText(context, ui.msg, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold (
        containerColor = Color.White,
        content = { padding ->
            Box (modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .windowInsetsPadding(WindowInsets.systemBars)
            ) {
                // "X" button on top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // ⬅️ this aligns it to the top right
                        .border(1.dp, Color.White, CircleShape)
                        .clip(CircleShape)
                        .clickable { navController.popBackStack() }
                        .padding(horizontal = 12.dp, vertical = 2.dp), // small inner padding
                    contentAlignment = Alignment.Center
                ) {
                    Text("X", color = Color.Black, fontSize = if (isTablet) 24.sp else 20.sp, fontWeight = FontWeight.Bold)
                }

                Column (
                    modifier = modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement =  Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.a122mm_logo_bt),
                        contentDescription = "Login Banner",
                        modifier= Modifier
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Ready to watch?",
                        color = Color.Black,
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Left
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Enter your email, name and password to create your account",
                        color = Color.Black,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Left
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    var emailError by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = !isValidEmail(it) && it.isNotEmpty()
                        },
                        label = { Text("Email") },
                        singleLine = true,
                        colors = customColors,
                        isError = emailError, // highlight border red
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    if (emailError) {
                        Text(
                            text = "Please enter a valid email address",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField (
                        value = name,
                        maxLines = 1,
                        onValueChange = {
                            name = it
                        },
                        label = {
                            Text(text = "Name")
                        },
                        colors = customColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        maxLines = 1,
                        singleLine = true,
                        colors = customColors,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color.DarkGray
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    val isLoading = ui is com.example.a122mm.auth.SignUpUiState.Loading

                    Button(
                        onClick = {
                            vm.doSignUp(email.trim(), name.trim(), password)
                        },
                        enabled = !isLoading &&
                                    email.isNotBlank() &&
                                    name.isNotBlank() &&
                                    !emailError &&
                                    password.isNotBlank(),
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Red,   // keep red even when disabled
                            disabledContentColor = Color.White    // keep white text/spinner
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "START YOUR JOURNEY",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Navigate back to login on success
                    if (ui is com.example.a122mm.auth.SignUpUiState.Success) {
                        LaunchedEffect(Unit) {
                            navController.navigate("login") {
                                popUpTo("signup") { inclusive = true }
                            }
                        }
                    }

                }
            }

        }
    )
}
