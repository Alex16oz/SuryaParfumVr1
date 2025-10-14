package com.surya.parfum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityAddEditProductBinding
import kotlin.collections.joinToString
import kotlin.collections.mapNotNull
import kotlin.jvm.java
import kotlin.let
import kotlin.text.isEmpty
import kotlin.text.split
import kotlin.text.toIntOrNull
import kotlin.text.toLongOrNull
import kotlin.text.trim
import kotlin.to

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProductBinding
    private lateinit var db: FirebaseFirestore
    private var productId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        productId = intent.getStringExtra("PRODUCT_ID")

        if (productId != null) {
            // Mode Edit
            binding.tvFormTitle.text = "Edit Produk"
            binding.btnSaveProduct.text = "Update Produk"
            loadProductData(productId!!)
        } else {
            // Mode Tambah
            binding.tvFormTitle.text = "Tambah Produk Baru"
        }

        binding.btnSaveProduct.setOnClickListener {
            saveProduct()
        }
    }

    private fun loadProductData(id: String) {
        db.collection("products").document(id).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val product = document.toObject(Product::class.java)
                    product?.let {
                        binding.etProductName.setText(it.name)
                        binding.etProductDesc.setText(it.description)
                        binding.etPricePerMl.setText(it.pricePerMl.toString())
                        binding.etImageUrl.setText(it.imageUrl)
                        // Mengubah List<Int> menjadi String yang dipisahkan koma
                        binding.etAvailableSizes.setText(it.availableSizes.joinToString(","))
                        binding.etStock.setText(it.stock.toString())
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data produk", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val desc = binding.etProductDesc.text.toString().trim()
        val pricePerMl = binding.etPricePerMl.text.toString().toLongOrNull() ?: 0
        val imageUrl = binding.etImageUrl.text.toString().trim()
        val stock = binding.etStock.text.toString().toIntOrNull() ?: 0
        val sizesString = binding.etAvailableSizes.text.toString()
        val availableSizes = sizesString.split(",").mapNotNull { it.trim().toIntOrNull() }

        if (name.isEmpty() || desc.isEmpty() || availableSizes.isEmpty()) {
            Toast.makeText(this, "Nama, deskripsi, dan ukuran wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val productMap = hashMapOf(
            "name" to name,
            "description" to desc,
            "pricePerMl" to pricePerMl,
            "availableSizes" to availableSizes,
            "imageUrl" to imageUrl,
            "stock" to stock
        )

        val task = if (productId != null) {
            // Mode Edit: update dokumen yang ada
            db.collection("products").document(productId!!).set(productMap)
        } else {
            // Mode Tambah: buat dokumen baru
            db.collection("products").add(productMap)
        }

        task.addOnSuccessListener {
            val message = if (productId != null) "Produk berhasil diupdate" else "Produk berhasil ditambahkan"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            finish() // Kembali ke Dashboard Admin
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}