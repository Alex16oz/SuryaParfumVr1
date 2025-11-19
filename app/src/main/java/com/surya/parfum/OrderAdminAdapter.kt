package com.surya.parfum

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.surya.parfum.databinding.ItemOrderAdminBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderAdminAdapter(
    private val orderList: List<Order>,
    // 1. Tambahkan listener sebagai parameter konstruktor
    private val listener: OrderActionListener
) : RecyclerView.Adapter<OrderAdminAdapter.ViewHolder>() {

    // Interface Callback untuk mengirim aksi ke Fragment
    interface OrderActionListener {
        fun onApproveClick(order: Order)
        fun onRejectClick(order: Order)
        // Tambahkan fungsi untuk menangani klik item (navigasi)
        fun onItemClick(order: Order)
    }

    inner class ViewHolder(val binding: ItemOrderAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]
        val context = holder.itemView.context

        holder.binding.apply {
            tvCustomerName.text = order.customerName
            tvOrderTotal.text = formatCurrency(order.totalAmount)
            tvOrderStatus.text = order.status
            tvFulfillment.text = "Metode: ${order.fulfillmentMethod}"

            // Atur UI Status (Warna latar belakang)
            updateStatusUI(tvOrderStatus, order.status, context)

            // Format tanggal agar mudah dibaca
            order.orderDate?.toDate()?.let { date ->
                val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
                tvOrderDate.text = format.format(date)
            }

            // Logika Visibilitas Tombol Aksi
            // Tombol hanya ditampilkan jika status masih "Diproses"
            if (order.status == "Diproses") {
                llActionButtons.visibility = View.VISIBLE
                btnApprove.visibility = View.VISIBLE
                btnReject.visibility = View.VISIBLE
            } else {
                llActionButtons.visibility = View.GONE
            }

            // 2. Menangani Klik Tombol Aksi
            btnApprove.setOnClickListener {
                listener.onApproveClick(order)
            }

            btnReject.setOnClickListener {
                listener.onRejectClick(order)
            }
        }

        // 3. Menangani Klik Item (untuk Navigasi ke Detail)
        holder.itemView.setOnClickListener {
            listener.onItemClick(order)
        }
    }


    override fun getItemCount(): Int = orderList.size

    // --- Utility Functions ---

    /** Memformat angka menjadi format mata uang Rupiah. */
    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    /** Fungsi untuk mengubah warna latar belakang status. */
    private fun updateStatusUI(view: View, status: String, context: Context) {
        // NOTE: Anda perlu mendefinisikan color resource di colors.xml atau
        // menggunakan warna bawaan Android seperti di bawah.
        val colorResId = when (status) {
            "Disetujui" -> android.R.color.holo_green_dark
            "Ditolak" -> android.R.color.holo_red_dark
            "Selesai" -> android.R.color.holo_blue_dark
            else -> android.R.color.holo_orange_dark // Diproses/Default
        }
        // Asumsi Anda menggunakan MaterialCardView, kita menggunakan backgroundTintList
        view.backgroundTintList = ContextCompat.getColorStateList(context, colorResId)
    }
}