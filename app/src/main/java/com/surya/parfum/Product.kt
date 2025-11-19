package com.surya.parfum

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    // Menggunakan @DocumentId agar ID dokumen otomatis terisi saat diambil dari Firestore
    @DocumentId
    var id: String = "",

    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",

    // === DATA DARI STRUKTUR ASLI ANDA ===
    val pricePerMl: Long = 0,
    val availableSizes: List<Int> = emptyList(),
    val stock: Int = 0,

    // === TAMBAHAN PENTING ===
    // Field 'price' ditambahkan agar ProductAdminAdapter tidak error.
    // Nilainya default 0, karena Anda menggunakan pricePerMl.
    val price: Long = 0,

    // Tambahan opsional (bisa dikosongkan jika tidak dipakai)
    val category: String = ""
) : Parcelable