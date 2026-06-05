package com.example.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.fragment.app.FragmentActivity
import android.net.Uri

@Composable
fun JournalApp(activity: FragmentActivity, viewModel: JournalViewModel, openNewEntry: Boolean = false) {
    val navController = rememberNavController()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val isAppLockEnabled by viewModel.appLockEnabled.collectAsState()

    if (isAppLockEnabled && !isAuthenticated) {
        BiometricAuthScreen(
            activity = activity,
            onAuthSuccess = { viewModel.setAuthenticated(true) }
        )
        return
    }

    LaunchedEffect(isAuthenticated, isAppLockEnabled, openNewEntry) {
        if ((!isAppLockEnabled || isAuthenticated) && openNewEntry && navController.currentDestination?.route == "home") {
            navController.navigate("add_edit")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + androidx.compose.animation.fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + androidx.compose.animation.fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + androidx.compose.animation.fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + androidx.compose.animation.fadeOut(animationSpec = tween(300))
        }
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onAddEntry = { navController.navigate("add_edit") },
                onEntryClick = { entryId -> navController.navigate("add_edit?id=$entryId") },
                onPromptClick = { prompt -> navController.navigate("add_edit?prompt=${Uri.encode(prompt)}") }
            )
        }
        composable(
            route = "add_edit?id={id}&prompt={prompt}",
            deepLinks = listOf(
                androidx.navigation.navDeepLink { uriPattern = "journal://entry/{id}" },
                androidx.navigation.navDeepLink { uriPattern = "journal://new_entry?prompt={prompt}" }
            ),
            arguments = listOf(
                navArgument("id") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("prompt") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val idArg = backStackEntry.arguments?.getInt("id") ?: -1
            val entryId = if (idArg != -1) idArg else null
            val promptArg = backStackEntry.arguments?.getString("prompt") ?: ""
            AddEditEntryScreen(
                viewModel = viewModel,
                entryId = entryId,
                initialPrompt = promptArg,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEntry = { destinationId -> 
                    navController.navigate("add_edit?id=$destinationId") 
                }
            )
        }
    }
}
