package com.example.a122mm

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.a122mm.auth.RequireAuth
import com.example.a122mm.components.ViewMovieDetail
import com.example.a122mm.dataclass.HomeViewModel
import com.example.a122mm.screen.AccountSettingsScreen
import com.example.a122mm.screen.AllSetPage
import com.example.a122mm.screen.AuthScreen
import com.example.a122mm.screen.ChangePasswordScreen
import com.example.a122mm.screen.ChooseIconScreen
import com.example.a122mm.screen.ErrorRoute
import com.example.a122mm.screen.HomeScreen
import com.example.a122mm.screen.LoginScreen
import com.example.a122mm.screen.MainPlayerScreen
import com.example.a122mm.screen.SignUpScreen
import com.example.a122mm.screen.SplashRoute
import com.example.a122mm.screen.VideoPlayerScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier) {

    //val navController = rememberNavController()

    NavHost(navController = navController  , startDestination = "splash") {
        composable("splash") {
            SplashRoute(navController)
        }

        composable("auth") {
            AuthScreen(modifier,navController)
        }

        composable("login") {
            LoginScreen(modifier, navController)
        }

        composable("signup") {
            SignUpScreen(modifier, navController)
        }

        composable("home") {
            HomeScreen(modifier, navController)
        }

        composable("settings") {
            AccountSettingsScreen(navController)
        }

        composable("change_password") {
            ChangePasswordScreen(navController)
        }

        composable("choose_icon") {
            ChooseIconScreen(navController)
        }

        composable("error") {
            ErrorRoute(navController)
        }

        composable("movie/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            // We grab the same ViewModel instance as in HomePage
            val homeViewModel: HomeViewModel = viewModel(navController.getBackStackEntry("home"))
            ViewMovieDetail(
                modifier,
                movieId,
                navController,
                onMyListChanged = { homeViewModel.triggerRefresh() }
            )
        }

       composable(
            route = "playvideo/{videoUrl}/{subtitleUrl}/{title}",
            arguments = listOf(
                navArgument("videoUrl") { type = NavType.StringType },
                navArgument("subtitleUrl") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val subtitleUrl = backStackEntry.arguments?.getString("subtitleUrl")
            val title = backStackEntry.arguments?.getString("title") ?: ""

           RequireAuth(navController) {
               VideoPlayerScreen(
                   videoUrl = videoUrl,
                   subtitleUrl = subtitleUrl,
                   tTitle = title, // 👈 Add this
                   navController = navController
               )
           }
        }

        composable(
            route = "playmovie/{mId}",
            arguments = listOf(
                navArgument("mId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoCode = backStackEntry.arguments?.getString("mId") ?: ""

            RequireAuth(navController) {
                MainPlayerScreen(
                    videoCode = videoCode,
                    navController = navController
                )
            }
        }

//        composable("scan") {
//            ScanPage(navController = navController)
//        }

//        composable(
//            route = "scan_confirm/{pairCode}/{deviceId}/{deviceName}",
//            arguments = listOf(
//                navArgument("pairCode") { type = NavType.StringType },
//                navArgument("deviceId") { type = NavType.StringType },
//                navArgument("deviceName") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val pairCode = backStackEntry.arguments?.getString("pairCode") ?: ""
//            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
//            val deviceName = backStackEntry.arguments?.getString("deviceName")?.let { Uri.decode(it) } ?: ""
//
//            ConfirmPairPage(
//                navController = navController,
//                pairCode = pairCode,
//                deviceId = deviceId,
//                deviceName = deviceName
//            )
//        }
//
//        composable("scan_done") {
//            AllSetPage(navController = navController)
//        }


    }
}

