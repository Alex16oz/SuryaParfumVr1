package com.surya.parfum

import com.google.firebase.firestore.Exclude

data class CartItem(
    @get:Exclude
    var id: String = "", // Untuk menyimpan ID dokumen dari Firestore

    val productName: String = "",
    val selectedSize: Int = 0,
    val quantity: Int = 0,
    val totalPrice: Long = 0,
    val imageUrl: String = ""
)