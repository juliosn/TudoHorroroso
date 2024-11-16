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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tudohorrorosoapp.R
import com.google.firebase.auth.FirebaseAuth
import android.util.Patterns

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun loginUser() {
        if (email.isEmpty()) {
            Toast.makeText(
                navController.context,
                "Por favor, insira seu e-mail.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(
                navController.context,
                "Por favor, insira sua senha.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!isEmailValid(email)) {
            Toast.makeText(
                navController.context,
                "E-mail e/ou senha incorretos.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        loading = true
        errorMessage = null
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loading = false
                if (task.isSuccessful) {
                    navController.navigate("home")
                } else {
                    val error = task.exception?.message ?: "Erro ao fazer login, verifique suas credenciais e tente novamente."

                    when (task.exception) {
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(
                                navController.context,
                                "E-mail e/ou senha incorretos.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                            Toast.makeText(
                                navController.context,
                                "Erro ao fazer login, verifique suas credenciais e tente novamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is com.google.firebase.FirebaseNetworkException -> {
                            Toast.makeText(
                                navController.context,
                                "Erro de conexão. Verifique sua rede e tente novamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                navController.context,
                                error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
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
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 16.dp)
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
                onValueChange = { password = it },
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

            Button(
                onClick = { loginUser() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6F00),
                    contentColor = Color.White
                ),
                enabled = !loading
            ) {
                Text(if (loading) "Carregando..." else "Entrar")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("sign_up") }) {
                Text(text = "Não possui uma conta? Cadastre-se")
            }
        }
    }
}
