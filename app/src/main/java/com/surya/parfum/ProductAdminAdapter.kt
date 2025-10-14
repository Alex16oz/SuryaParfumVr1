package com.surya.parfum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surya.parfum.databinding.ItemProductAdminBinding
import kotlin.apply


class ProductAdminAdapter(
    private val productList: List<Product>,
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
            tvProductPrice.text = "Rp ${product.pricePerMl} / ml"

            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder) // Gambar saat loading
                .error(R.drawable.ic_image_placeholder)       // Gambar jika URL error atau kosong
                .into(ivProductImage)

            // Listener untuk klik hapus
            ivDeleteProduct.setOnClickListener {
                onDeleteClick(product)
            }

            // Listener untuk klik seluruh item (untuk edit)
            holder.itemView.setOnClickListener {
                onEditClick(product)
            }
        }
    }

    override fun getItemCount(): Int = productList.size
}