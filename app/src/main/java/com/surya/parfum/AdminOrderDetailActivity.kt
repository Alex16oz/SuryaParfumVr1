package com.surya.parfum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint // IMPORT PENTING UNTUK GeoPoint
import com.surya.parfum.databinding.ActivityAdminOrderDetailBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminOrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminOrderDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var orderRef: DocumentReference
    private lateinit var itemsAdapter: OrderDetailAdapter

    // Variabel untuk menyimpan ID pesanan
    private var orderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Setup Toolbar dan Navigasi
        binding.topAppBar.setNavigationOnClickListener { finish() }

        orderId = intent.getStringExtra("ORDER_ID")

        if (orderId == null || orderId!!.isEmpty()) {
            Toast.makeText(this, "ID Pesanan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvOrderId.text = "ID Pesanan: $orderId"

        // Setup RecyclerView untuk item pesanan
        setupRecyclerView()

        // Ambil dan dengarkan detail pesanan secara real-time
        orderRef = db.collection("orders").document(orderId!!)
        listenToOrderDetail()
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan list kosong
        itemsAdapter = OrderDetailAdapter(emptyList())
        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(this@AdminOrderDetailActivity)
            adapter = itemsAdapter
            isNestedScrollingEnabled = false
        }
    }

    /** Mendengarkan perubahan detail pesanan secara real-time dari Firestore. */
    private fun listenToOrderDetail() {
        orderRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(this, "Error mendengarkan data: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val order = snapshot.toObject(Order::class.java)
                if (order != null) {
                    populateUi(order)
                    setupActionButtons(order)
                }
            } else {
                Toast.makeText(this, "Data pesanan tidak ditemukan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /** Mengisi semua elemen UI dengan data dari objek Order */
    private fun populateUi(order: Order) {

        // Detail Pelanggan
        binding.tvCustomerName.text = "Nama: ${order.customerName}"
        binding.tvCustomerAddress.text = "Alamat: ${order.address ?: "Tidak tersedia"}"
        binding.tvCustomerPhone.text = "Telepon: ${order.phone ?: "Tidak tersedia"}"
        binding.tvFulfillmentMethod.text = "Metode: ${order.fulfillmentMethod}"

        // Detail Pesanan
        binding.tvOrderStatus.text = order.status
        updateStatusUI(order.status)
        binding.tvTotalAmount.text = "Total: ${formatCurrency(order.totalAmount)}"

        order.orderDate?.toDate()?.let { date ->
            val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
            binding.tvOrderDate.text = "Tanggal: ${format.format(date)}"
        }

        // Setup RecyclerView Item
        itemsAdapter = OrderDetailAdapter(order.items)
        binding.rvOrderItems.adapter = itemsAdapter


        // Logika Tombol Lihat Lokasi
        // Tombol hanya ditampilkan jika pesanan diantar dan GeoPoint tersedia
        if (order.fulfillmentMethod == "Antar ke Alamat" && order.customerLocation != null) {
            binding.btnViewCustomerLocation.visibility = View.VISIBLE
            binding.btnViewCustomerLocation.setOnClickListener {
                val lat = order.customerLocation.latitude
                val lng = order.customerLocation.longitude
                // Buka Google Maps untuk navigasi
                val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
        } else {
            binding.btnViewCustomerLocation.visibility = View.GONE
        }
    }

    /** Menyiapkan tombol Setujui dan Tolak dari halaman Detail */
    private fun setupActionButtons(order: Order) {
        // Tombol aksi hanya ditampilkan jika status masih 'Diproses'
        if (order.status == "Diproses") {
            binding.llActionButtonsDetail.visibility = View.VISIBLE

            binding.btnApproveDetail.setOnClickListener {
                updateOrderStatus(order.id, "Disetujui")
            }

            binding.btnRejectDetail.setOnClickListener {
                updateOrderStatus(order.id, "Ditolak")
            }
        } else {
            binding.llActionButtonsDetail.visibility = View.GONE
        }
    }

    /** Fungsi Logika Update Status di Firestore */
    private fun updateOrderStatus(orderId: String, newStatus: String) {
        val updateMap = mapOf("status" to newStatus)

        orderRef.update(updateMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Status pesanan berhasil diubah menjadi: $newStatus", Toast.LENGTH_SHORT).show()
                // onSnapshot akan otomatis memanggil populateUi lagi
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengubah status: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /** Memformat angka menjadi format mata uang Rupiah. */
    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    /** Fungsi untuk mengubah warna latar belakang status. */
    private fun updateStatusUI(status: String) {
        val context = this
        val colorResId = when (status) {
            "Disetujui" -> android.R.color.holo_green_dark
            "Ditolak" -> android.R.color.holo_red_dark
            "Selesai" -> android.R.color.holo_blue_dark
            else -> android.R.color.holo_orange_dark // Diproses/Default
        }
        // Pastikan tvOrderStatus memiliki backgroundTintList untuk mengubah warnanya
        binding.tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, colorResId)
    }
}