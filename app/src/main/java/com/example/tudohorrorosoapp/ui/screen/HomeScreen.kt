package com.example.tudohorrorosoapp.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tudohorrorosoapp.data.Recipe
import com.example.tudohorrorosoapp.ui.components.DrawerContent
import com.example.tudohorrorosoapp.ui.components.RecipeCard
import com.example.tudohorrorosoapp.ui.components.SimpleSearchBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val recipes = remember { mutableStateOf<List<Recipe>>(emptyList()) }
    val filteredRecipes = remember { mutableStateOf<List<Recipe>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }  // Para indicar se os dados estão carregando
    val source = "home"
    val searchQuery = remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()

    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    LaunchedEffect(Unit) {
        firestore.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                val fetchedRecipes = result.mapNotNull { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipe.copy(id = document.id)
                }
                recipes.value = fetchedRecipes.filter { it.userId != currentUserId }
                filteredRecipes.value = recipes.value
                isLoading.value = false
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
            }
    }

    fun onSearch(query: String) {
        filteredRecipes.value = if (query.isEmpty()) {
            recipes.value
        } else {
            recipes.value.filter { it.title.contains(query, ignoreCase = true) }
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(navController = navController, isDrawerOpen = drawerState.isOpen)
            },
            content = {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Receitas") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Abrir Menu")
                                }
                            }
                        )
                    },
                    content = { paddingValues ->
                        if (isLoading.value) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Carregando receitas...",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = paddingValues,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            ) {
                                item {
                                    SimpleSearchBar(
                                        searchQuery = searchQuery.value,
                                        onQueryChange = { newQuery ->
                                            searchQuery.value = newQuery
                                        },
                                        onSearch = { query ->
                                            onSearch(query)
                                        }
                                    )
                                }
                                items(filteredRecipes.value) { recipe ->
                                    RecipeCard(
                                        recipe = recipe,
                                        navController = navController,
                                        source = source
                                    )
                                }
                            }
                        }
                    }
                )
            }
        )
    }
}
