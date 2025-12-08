package com.example.tpsqlite

data class Report(
    var id: Int = 0,
    var titre: String = "",
    var description: String = "",
    var categorie: String = "",
    var priorite: Int = 3,
    var photoUri: String = ""
)