package com.surya.parfum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var cartAdapter: CartAdapter
    private val cartItemList = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Fungsi untuk tombol kembali di toolbar
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        fetchCartItems()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItemList) { cartItem ->
            // Logika saat tombol delete di klik
            deleteCartItem(cartItem)
        }
        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun fetchCartItems() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Ambil data dari koleksi 'carts' yang userId-nya sama dengan pengguna saat ini
        db.collection("carts")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    cartItemList.clear()
                    for (document in snapshots.documents) {
                        val cartItem = document.toObject(CartItem::class.java)
                        if (cartItem != null) {
                            cartItem.id = document.id // Simpan ID dokumen
                            cartItemList.add(cartItem)
                        }
                    }
                    cartAdapter.notifyDataSetChanged()
                    updateTotal()
                    checkIfEmpty()
                }
            }
    }

    private fun deleteCartItem(cartItem: CartItem) {
        db.collection("carts").document(cartItem.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${cartItem.productName} dihapus dari keranjang", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTotal() {
        var total: Long = 0
        for (item in cartItemList) {
            total += item.totalPrice
        }
        binding.tvTotalPrice.text = "Rp $total"
    }

    private fun checkIfEmpty() {
        if (cartItemList.isEmpty()) {
            binding.tvEmptyCart.visibility = View.VISIBLE
            binding.rvCartItems.visibility = View.GONE
        } else {
            binding.tvEmptyCart.visibility = View.GONE
            binding.rvCartItems.visibility = View.VISIBLE
        }
    }
}