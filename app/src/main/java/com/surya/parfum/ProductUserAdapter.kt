package com.surya.parfum

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surya.parfum.databinding.ItemProductUserBinding
import kotlin.apply
import kotlin.collections.minOrNull

class ProductUserAdapter(private val productList: List<Product>) :
    RecyclerView.Adapter<ProductUserAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProductUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        holder.binding.apply {
            tvProductName.text = product.name
            // Tampilkan harga termurah
            val minPrice = product.pricePerMl * (product.availableSizes.minOrNull() ?: 0)
            tvProductPrice.text = "Mulai dari Rp $minPrice"

            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder) // Gambar saat loading
                .error(R.drawable.ic_image_placeholder)       // Gambar jika URL error atau kosong
                .into(ivProductImage)
        }
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            // Buat Intent untuk membuka ProductDetailActivity
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                // Kirim ID unik dari produk yang di-klik
                putExtra("PRODUCT_ID", product.id)
            }
            // Mulai Activity baru
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productList.size
}