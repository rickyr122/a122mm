package com.example.a122mm.utility

import androidx.navigation.NavHostController

fun NavHostController.navigateToError(message: String?, retryRoute: String) {
    this.navigate("error") {
        popUpTo(this@navigateToError.graph.startDestinationId) { inclusive = false }
    }
    this.currentBackStackEntry?.arguments?.apply {
        putString("err_msg", message ?: "An unexpected error occurred.")
        putString("retry_route", retryRoute)
    }
}