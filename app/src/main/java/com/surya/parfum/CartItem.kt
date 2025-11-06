package com.surya.parfum

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    @get:Exclude
    var id: String = "",

    // TAMBAHKAN DUA BARIS INI
    val userId: String = "",
    val productId: String = "",

    val productName: String = "",
    val selectedSize: Int = 0,
    val quantity: Int = 0,
    val totalPrice: Long = 0,
    val imageUrl: String = "",

    @get:Exclude
    var isSelected: Boolean = true

) : Parcelable