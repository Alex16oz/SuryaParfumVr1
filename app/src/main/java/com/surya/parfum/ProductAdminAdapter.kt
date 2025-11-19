package com.surya.parfum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surya.parfum.databinding.ItemProductAdminBinding
import java.text.NumberFormat
import java.util.*

class ProductAdminAdapter(
    // Menggunakan 'var' agar list bisa diubah saat pencarian
    private var productList: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdminAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.binding.apply {
            tvProductName.text = product.name

            // Logika Harga: Cek apakah pakai harga tetap atau per ml
            // Ini aman karena Product.kt Anda sudah support keduanya
            if (product.price > 0) {
                tvProductPrice.text = formatCurrency(product.price)
            } else {
                tvProductPrice.text = "${formatCurrency(product.pricePerMl)} / ml"
            }

            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(ivProductImage)

            // === PERBAIKAN UTAMA DI SINI ===
            // Menggunakan 'ivDeleteProduct' sesuai dengan file XML asli Anda
            ivDeleteProduct.setOnClickListener {
                onDeleteClick(product)
            }

            // Klik item untuk edit
            holder.itemView.setOnClickListener {
                onEditClick(product)
            }
        }
    }

    override fun getItemCount(): Int = productList.size

    // === FUNGSI PENTING UNTUK PENCARIAN ===
    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }

    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}