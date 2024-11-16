package com.example.tudohorrorosoapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.example.tudohorrorosoapp.data.Recipe
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EditRecipeScreen(navController: NavController, recipeId: String) {
    val db = FirebaseFirestore.getInstance()

    val auth = FirebaseAuth.getInstance()

    var title by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    val userId = auth.currentUser?.uid ?: ""
    var ingredients by remember { mutableStateOf(listOf<String>()) }
    var instructions by remember { mutableStateOf("") }

    LaunchedEffect(recipeId) {
        db.collection("recipes").document(recipeId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val recipe = document.toObject<Recipe>()
                    recipe?.let {
                        title = it.title
                        user = it.user
                        ingredients = it.ingredients.split(", ")
                        instructions = it.instructions
                    }
                } else {
                    Log.d("EditRecipeScreen", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("EditRecipeScreen", "Error getting document: ", exception)
            }
    }

    fun saveUpdatedRecipeToFirestore(
        updatedRecipe: Recipe,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("recipes").document(recipeId)
            .set(updatedRecipe)
            .addOnSuccessListener {
                onSuccess()
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
                text = "Editar Receita",
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
                onClick = {
                    ingredients = ingredients + ""
                },
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
                        Toast.makeText(navController.context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val updatedRecipe = Recipe(
                        id = recipeId,
                        title = title,
                        user = user,
                        userId = userId,
                        ingredients = ingredients.joinToString(", "),
                        instructions = instructions
                    )

                    saveUpdatedRecipeToFirestore(
                        updatedRecipe = updatedRecipe,
                        onSuccess = {
                            Toast.makeText(navController.context, "Receita atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onFailure = { exception ->
                            Toast.makeText(navController.context, "Erro ao salvar: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                Text("Salvar Receita")
            }

            Spacer(modifier = Modifier.height(16.dp))

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
