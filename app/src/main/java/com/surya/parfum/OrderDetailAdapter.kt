package com.surya.parfum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.surya.parfum.databinding.ItemOrderDetailBinding
import java.text.NumberFormat
import java.util.*

class OrderDetailAdapter(private val itemList: List<OrderItem>) :
    RecyclerView.Adapter<OrderDetailAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        holder.binding.apply {
            // 1. Nama Produk
            var nameText = item.productName
            item.selectedSize?.let { sizeValue ->
                nameText = "${item.productName} ($sizeValue)"
            }
            tvItemName.text = nameText

            // 2. Logika Cerdas untuk Data Kosong
            // Jika quantity 0 (karena tidak ada di DB), kita asumsikan 1 agar tampilan tidak aneh
            val realQuantity = if (item.quantity > 0) item.quantity else 1

            // Jika price 0 (karena tidak ada di DB), kita hitung dari totalPrice
            val realPrice = if (item.price > 0) item.price else (item.totalPrice / realQuantity)

            // 3. Tampilkan Data
            tvItemQuantity.text = "Jumlah: $realQuantity"

            val formattedPrice = formatCurrency(realPrice)
            val formattedSubtotal = formatCurrency(item.totalPrice)

            tvItemPrice.text = "Harga Satuan: $formattedPrice | Subtotal: $formattedSubtotal"
        }
    }

    override fun getItemCount(): Int = itemList.size

    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}