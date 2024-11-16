package com.example.tudohorrorosoapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tudohorrorosoapp.ui.screen.*

@Composable
fun SetupNavigation(navController: NavHostController) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash"){
            SplashScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("sign_up") {
            SignUpScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("view_profile") {
            ViewProfileScreen(navController = navController)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController)
        }
        composable("receitas"){
            MyRecipesScreen(navController)
        }
        composable("add_recipe") {
            AddRecipeScreen(navController = navController)
        }
        composable("recipe_detail/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            RecipeDetailScreen(navController = navController, recipeId = recipeId.toString())
        }
        composable("manage_recipe/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            ManageRecipeScreen(navController = navController, recipeId = recipeId.toString())
        }
        composable("edit_recipe/{recipeId}"){backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            EditRecipeScreen(navController = navController, recipeId = recipeId.toString())
        }
    }

}
