package com.surya.parfum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.surya.parfum.databinding.ItemOrderUserBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(
    private val orderList: List<Order>,
    private val onItemClick: (Order) -> Unit // Callback untuk klik
) : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOrderUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]
        val context = holder.itemView.context

        holder.binding.apply {
            // Format Harga
            tvOrderTotal.text = "Total: ${formatCurrency(order.totalAmount)}"

            // Status
            tvOrderStatus.text = order.status

            // Ubah Warna Status agar lebih informatif
            val colorResId = when (order.status) {
                "Disetujui" -> android.R.color.holo_green_dark
                "Ditolak" -> android.R.color.holo_red_dark
                "Selesai" -> android.R.color.holo_blue_dark
                else -> android.R.color.holo_orange_dark // Diproses
            }
            tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, colorResId)

            // Format Tanggal
            order.orderDate?.toDate()?.let { date ->
                val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
                tvOrderDate.text = format.format(date)
            }

            // Event Klik
            root.setOnClickListener {
                onItemClick(order)
            }
        }
    }

    override fun getItemCount(): Int = orderList.size

    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}