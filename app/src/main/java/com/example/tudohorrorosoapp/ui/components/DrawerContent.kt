package com.example.tudohorrorosoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DrawerContent(navController: NavController, isDrawerOpen: Boolean) {
    val backgroundColor = if (isDrawerOpen) Color.White else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Cor muda dependendo do estado
            .padding(16.dp)
    ) {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black) // Título
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Itens do Drawer
        DrawerItem(text = "Início", onClick = { navController.navigate("home") })
        DrawerItem(text = "Receitas", onClick = { navController.navigate("receitas") })
        DrawerItem(text = "Perfil", onClick = { navController.navigate("view_profile") })
    }
}
