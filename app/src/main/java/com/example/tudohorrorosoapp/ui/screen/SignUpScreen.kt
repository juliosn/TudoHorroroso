package com.example.tudohorrorosoapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tudohorrorosoapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(navController: NavController) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    fun saveUserData(uid: String?, email: String) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "nome" to nome,
            "email" to email,
        )
        uid?.let {
            db.collection("usuarios").document(it)
                .set(user)
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->
                }
        }
    }

    fun signUpUser() {
        if (nome.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(
                navController.context,
                "Todos os campos são obrigatórios",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(
                navController.context,
                "As senhas não coincidem",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (password.length < 6) {
            passwordError = true
            Toast.makeText(
                navController.context,
                "A senha deve ter pelo menos 6 caracteres",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        loading = true
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loading = false
                if (task.isSuccessful) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    saveUserData(uid, email)
                    navController.navigate("login")
                } else {
                    Toast.makeText(
                        navController.context,
                        task.exception?.message ?: "Erro ao criar conta",
                        Toast.LENGTH_LONG
                    ).show()
                }
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
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_cadastro),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .size(150.dp)
                    .padding(bottom = 10.dp)
            )

            Text(
                text = "Cadastro de Usuário",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFFF6F00),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.length < 6
                },
                label = { Text("Senha") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    if (password.isNotEmpty()) {
                        val image = if (passwordVisible)
                            painterResource(id = R.drawable.ic_visibility_off)
                        else
                            painterResource(id = R.drawable.ic_visibility)

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Image(painter = image, contentDescription = "Mostrar Senha")
                        }
                    }
                }
            )

            if (passwordError) {
                Text(
                    text = "A senha deve ter pelo menos 6 caracteres",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Senha") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = { signUpUser() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6F00),
                    contentColor = Color.White
                ),
                enabled = !loading
            ) {
                Text("Cadastrar")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("login") }) {
                Text(text = "Já possui uma conta? Faça login")
            }
        }
    }
}
