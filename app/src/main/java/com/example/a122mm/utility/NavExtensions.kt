package com.example.a122mm.utility

import androidx.navigation.NavController
import androidx.navigation.NavHostController

//fun NavHostController.navigateToError(message: String?, retryRoute: String) {
//    this.navigate("error") {
//        popUpTo(this@navigateToError.graph.startDestinationId) { inclusive = false }
//    }
//    this.currentBackStackEntry?.arguments?.apply {
//        putString("err_msg", message ?: "An unexpected error occurred.")
//        putString("retry_route", retryRoute)
//    }
//}

fun NavHostController.navigateToError(message: String?, retryRoute: String) {
    // 1) Put args on the CURRENT (failing) entry so ErrorRoute can read them via previousBackStackEntry
    this.currentBackStackEntry?.arguments?.apply {
        putString("err_msg", message ?: "An unexpected error occurred.")
        putString("retry_route", retryRoute)
    }

    // 2) Go to error screen and DROP the failing page to avoid stacking
    val failingRoute = this.currentDestination?.route
    this.navigate("error") {
        if (failingRoute != null) {
            popUpTo(failingRoute) { inclusive = true }
        }
        launchSingleTop = true
    }
}

fun NavController.navigateToError(message: String?, retryRoute: String) {
    when (this) {
        is NavHostController -> this.navigateToError(message, retryRoute)
        else -> this.navigate("error")
    }
}