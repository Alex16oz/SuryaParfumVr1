package com.surya.parfum

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

/**
 * Data class untuk merepresentasikan sebuah pesanan di Firestore.
 */
data class Order(
    @DocumentId
    @get:Exclude
    var id: String = "",

    // Informasi Pelanggan
    val customerName: String = "",
    val userId: String? = null,
    val phone: String? = null,
    val address: String? = null,

    // LOKASI GPS
    val customerLocation: GeoPoint? = null,

    // Informasi Pesanan
    val orderDate: Timestamp? = null,
    val totalAmount: Long = 0,

    // Status Pesanan (Penting untuk Aksi Admin)
    val status: String = "Disetujui", // Diubah default ke 'Disetujui' berdasarkan screenshot terbaru

    // Detail Pemenuhan (Fulfillment)
    val fulfillmentMethod: String = "Ambil di Toko",

    // Daftar item yang dipesan
    val items: List<OrderItem> = emptyList()
)

/**
 * Data class untuk merepresentasikan setiap item produk di dalam pesanan.
 * Menggunakan @PropertyName untuk menyelaraskan dengan nama field yang mungkin berbeda di database.
 */
data class OrderItem(
    val productId: String = "",
    val productName: String = "",

    // KEMUNGKINAN NAMA FIELD DI DB ADALAH 'qty' ATAU 'itemQuantity'
    // Coba tambahkan @PropertyName untuk 'quantity'
    @get:PropertyName("quantity") // Asumsi nama field di DB adalah "quantity" (huruf kecil)
    val quantity: Long = 0,

    // KEMUNGKINAN NAMA FIELD DI DB ADALAH 'itemPrice' ATAU 'unitPrice'
    @get:PropertyName("price") // Asumsi nama field di DB adalah "price"
    val price: Long = 0,

    val selectedSize: Long? = null, // Kita biarkan Long agar tidak FC lagi
    val totalPrice: Long = 0
)