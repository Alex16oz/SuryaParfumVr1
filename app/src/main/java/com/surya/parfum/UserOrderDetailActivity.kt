package com.surya.parfum

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityUserOrderDetailBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class UserOrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserOrderDetailBinding
    private lateinit var db: FirebaseFirestore
    private var orderId: String? = null
    // Kita bisa menggunakan kembali OrderDetailAdapter yang sudah kita buat
    private lateinit var itemsAdapter: OrderDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.topAppBar.setNavigationOnClickListener { finish() }

        orderId = intent.getStringExtra("ORDER_ID")
        if (orderId == null) {
            finish()
            return
        }

        binding.tvOrderId.text = "ID Pesanan: $orderId"
        fetchOrderDetail()
    }

    private fun fetchOrderDetail() {
        db.collection("orders").document(orderId!!)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    Toast.makeText(this, "Gagal memuat detail", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val order = snapshot.toObject(Order::class.java)
                if (order != null) {
                    populateUi(order)
                }
            }
    }

    private fun populateUi(order: Order) {
        binding.tvOrderStatus.text = order.status
        binding.tvOrderTotal.text = formatCurrency(order.totalAmount)
        binding.tvFulfillmentMethod.text = "Metode: ${order.fulfillmentMethod}"

        // Detail Pengiriman (Alamat hanya muncul jika metode Antar)
        if (order.fulfillmentMethod == "Antar ke Alamat") {
            binding.tvShippingAddress.visibility = View.VISIBLE
            binding.tvShippingAddress.text = "Alamat Pengiriman:\n${order.address}\n(${order.phone})"
        } else {
            binding.tvShippingAddress.visibility = View.GONE
        }

        order.orderDate?.toDate()?.let { date ->
            val format = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            binding.tvOrderDate.text = "Tanggal: ${format.format(date)}"
        }

        // Warna Status
        val colorResId = when (order.status) {
            "Disetujui" -> android.R.color.holo_green_dark
            "Ditolak" -> android.R.color.holo_red_dark
            "Selesai" -> android.R.color.holo_blue_dark
            else -> android.R.color.holo_orange_dark
        }
        binding.tvOrderStatus.setTextColor(ContextCompat.getColor(this, colorResId))

        // Setup RecyclerView Item (Gunakan OrderDetailAdapter yang sama dengan Admin!)
        itemsAdapter = OrderDetailAdapter(order.items)
        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(this@UserOrderDetailActivity)
            adapter = itemsAdapter
        }
    }

    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}