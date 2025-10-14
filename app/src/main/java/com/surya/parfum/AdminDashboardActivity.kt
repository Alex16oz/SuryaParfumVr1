package com.surya.parfum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityAdminDashboardBinding
import kotlin.apply
import kotlin.jvm.java
import kotlin.let

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var productAdapter: ProductAdminAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        fetchProducts()

        binding.fabAddProduct.setOnClickListener {
            // Buka halaman tambah produk
            val intent = Intent(this, AddEditProductActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdminAdapter(
            productList,
            onEditClick = { product ->
                // Buka halaman edit produk dan kirim ID produk
                val intent = Intent(this, AddEditProductActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onDeleteClick = { product ->
                showDeleteConfirmationDialog(product)
            }
        )
        binding.rvAdminProducts.apply {
            layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
            adapter = productAdapter
        }
    }

    private fun fetchProducts() {
        db.collection("products").addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            snapshots?.let {
                productList.clear()
                for (document in it.documents) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        product.id = document.id // Penting: simpan ID dokumen
                        productList.add(product)
                    }
                }
                productAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Anda yakin ingin menghapus '${product.name}'?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                deleteProduct(product.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteProduct(productId: String) {
        db.collection("products").document(productId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}