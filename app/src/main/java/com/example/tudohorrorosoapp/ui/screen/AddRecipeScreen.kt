package com.example.tudohorrorosoapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tudohorrorosoapp.data.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddRecipeScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf(listOf("")) }
    var instructions by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("Carregando...") }

    val context = LocalContext.current

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: "Unknown User"

    LaunchedEffect(userId) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                userName = document.getString("nome") ?: "Usuário Desconhecido"
            }
            .addOnFailureListener {
                userName = "Erro ao carregar nome"
            }
    }

    fun saveRecipeToFirestore(
        recipe: Recipe,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("recipes")
            .add(recipe)
            .addOnSuccessListener { documentReference ->
                documentReference.update("id", documentReference.id)
                    .addOnSuccessListener {
                        onSuccess(documentReference.id)
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Adicionar Receita",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFFF6F00),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título da Receita") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            LazyColumn(modifier = Modifier.fillMaxHeight(0.5f)) {
                itemsIndexed(ingredients) { index, ingredient ->
                    OutlinedTextField(
                        value = ingredient,
                        onValueChange = { newIngredient ->
                            ingredients = ingredients.toMutableList().apply {
                                this[index] = newIngredient
                            }
                        },
                        label = { Text("Ingrediente ${index + 1}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (ingredients.size > 1) {
                                IconButton(onClick = {
                                    ingredients = ingredients.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir ingrediente"
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Button(
                onClick = { ingredients = ingredients + "" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6F00),
                    contentColor = Color.White
                ),
            ) {
                Text("Adicionar Ingrediente")
            }

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instruções") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(150.dp)
                    .verticalScroll(rememberScrollState()),
                singleLine = false
            )

            Button(
                onClick = {
                    if (title.isBlank() || ingredients.isEmpty() || instructions.isBlank()) {
                        Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    val newRecipe = Recipe(
                        title = title,
                        userId = userId,
                        user = userName,
                        ingredients = ingredients.joinToString(", "),
                        instructions = instructions
                    )

                    saveRecipeToFirestore(
                        recipe = newRecipe,
                        onSuccess = { documentId ->
                            Toast.makeText(context, "Receita adicionada com sucesso!", Toast.LENGTH_SHORT)
                                .show()

                            title = ""
                            ingredients = listOf("")
                            instructions = ""
                            navController.popBackStack()
                        },
                        onFailure = { exception ->
                            Toast.makeText(
                                context,
                                "Erro ao salvar: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6F00),
                    contentColor = Color.White
                ),
            ) {
                Text("Adicionar Receita")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão para voltar
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = "Voltar para a lista de receitas",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

