package com.surya.parfum

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth // Tambahkan variabel untuk Auth

    private var currentProduct: Product? = null
    private var calculatedPrice: Long = 0
    private var selectedSize: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() // Inisialisasi Auth

        val productId = intent.getStringExtra("PRODUCT_ID")

        if (productId == null) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchProductDetails(productId)

        // ===== LOGIKA BARU UNTUK TOMBOL TAMBAH KE KERANJANG =====
        binding.btnAddToCart.setOnClickListener {
            val currentUser = auth.currentUser
            // Cek apakah pengguna sudah login
            if (currentUser == null) {
                // Jika belum, arahkan ke halaman Login
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                // Jika sudah login, tambahkan produk ke keranjang
                addProductToCart(currentUser.uid)
            }
        }
    }

    // ===== FUNGSI BARU UNTUK MENYIMPAN KE FIRESTORE =====
    private fun addProductToCart(userId: String) {
        // Pastikan produk dan ukuran sudah dipilih
        if (currentProduct != null && selectedSize > 0) {
            binding.btnAddToCart.isEnabled = false // Nonaktifkan tombol sementara
            binding.btnAddToCart.text = "Menambahkan..."

            val cartItem = hashMapOf(
                "userId" to userId,
                "productId" to currentProduct!!.id,
                "productName" to currentProduct!!.name,
                "selectedSize" to selectedSize,
                "quantity" to 1, // Untuk sekarang, kita set kuantitas default 1
                "totalPrice" to calculatedPrice,
                "imageUrl" to currentProduct!!.imageUrl // Simpan juga URL gambar untuk ditampilkan di keranjang
            )

            db.collection("carts").add(cartItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "${currentProduct!!.name} berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    binding.btnAddToCart.isEnabled = true // Aktifkan kembali tombol
                    binding.btnAddToCart.text = "Tambah ke Keranjang"
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnAddToCart.isEnabled = true // Aktifkan kembali tombol
                    binding.btnAddToCart.text = "Tambah ke Keranjang"
                }
        } else {
            Toast.makeText(this, "Gagal menambahkan produk.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchProductDetails(productId: String) {
        // ... (kode ini tidak berubah)
        db.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentProduct = document.toObject(Product::class.java)
                    currentProduct?.id = document.id
                    populateUi()
                } else {
                    Toast.makeText(this, "Gagal memuat detail produk.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: Gagal terhubung ke database.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateUi() {
        // ... (kode ini tidak berubah)
        currentProduct?.let { product ->
            binding.tvDetailName.text = product.name
            binding.tvDetailDesc.text = product.description

            Glide.with(this)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.ivDetailImage)

            setupSizeSpinner(product)
        }
    }

    private fun setupSizeSpinner(product: Product) {
        // ... (kode ini tidak berubah)
        val sizes = product.availableSizes
        val sizeStrings = sizes.map { "$it ml" }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sizeStrings)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBottleSize.adapter = adapter

        binding.spinnerBottleSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedSize = sizes[position]
                calculatedPrice = product.pricePerMl * selectedSize
                binding.tvDetailPrice.text = "Rp $calculatedPrice"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                binding.tvDetailPrice.text = ""
            }
        }
    }
}