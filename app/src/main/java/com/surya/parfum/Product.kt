package com.surya.parfum

import com.google.firebase.firestore.Exclude

data class Product(
    @get:Exclude // Agar ID ini tidak ikut tersimpan sebagai field di Firestore
    var id: String = "",

    val name: String = "",
    val description: String = "",
    val pricePerMl: Long = 0,
    val availableSizes: List<Int> = emptyList(),
    val imageUrl: String = "",
    val stock: Int = 0
)