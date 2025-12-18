package com.surya.parfum

data class Komplain(
    val nama: String = "",
    val noOrder: String = "",
    val alasan: String = "",
    val fotoUrl: String = "",
    val status: String = "Menunggu"
)
