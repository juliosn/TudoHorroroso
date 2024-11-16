package com.example.tudohorrorosoapp.data

data class Recipe(
    val id: String = "",
    val title: String = "",
    val user: String = "",
    val userId: String = "",
    val ingredients: String = "",
    val instructions: String = ""
) {
    constructor() : this("", "", "", "", "", "")
}
