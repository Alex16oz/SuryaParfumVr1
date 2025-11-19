package com.surya.parfum

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    @get:Exclude
    var id: String = "",

    val userId: String = "",
    val productId: String = "",

    val productName: String = "",

    // Selected size bisa Int atau String tergantung data Anda.
    // Jika di OrderItem kita pakai Long?, di sini Int aman (akan dikonversi otomatis).
    val selectedSize: Int = 0,

    val quantity: Int = 0,

    // === TAMBAHAN PENTING ===
    // Harga Satuan (agar bisa dibaca Admin sebagai 'Harga Satuan')
    val price: Long = 0,

    // Harga Total (price * quantity)
    val totalPrice: Long = 0,

    val imageUrl: String = "",

    @get:Exclude
    var isSelected: Boolean = true

) : Parcelable