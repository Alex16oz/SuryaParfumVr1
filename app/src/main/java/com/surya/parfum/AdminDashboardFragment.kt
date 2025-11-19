package com.surya.parfum

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.FragmentAdminDashboardBinding
import java.util.*

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var productAdapter: ProductAdminAdapter

    // List untuk menyimpan Data Asli (Master Data)
    private val allProducts = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupSearchListener() // Tambahkan Listener Pencarian
        fetchProducts()

        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(requireActivity(), AddEditProductActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan list kosong
        productAdapter = ProductAdminAdapter(
            mutableListOf(), // Mulai dengan list kosong
            onEditClick = { product ->
                val intent = Intent(requireActivity(), AddEditProductActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onDeleteClick = { product ->
                showDeleteConfirmationDialog(product)
            }
        )
        binding.rvAdminProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    // === LOGIKA PENCARIAN ===
    private fun setupSearchListener() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        if (query != null) {
            val filteredList = ArrayList<Product>()
            for (i in allProducts) {
                // Cari berdasarkan nama (case insensitive)
                if (i.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))) {
                    filteredList.add(i)
                }
            }

            if (filteredList.isEmpty()) {
                // Opsional: Tampilkan toast jika tidak ada hasil
                // Toast.makeText(requireContext(), "Tidak ada data ditemukan", Toast.LENGTH_SHORT).show()
            }
            // Update adapter dengan data hasil filter
            productAdapter.updateList(filteredList)
        } else {
            // Jika query kosong, tampilkan semua data
            productAdapter.updateList(allProducts)
        }
    }

    private fun fetchProducts() {
        db.collection("products").addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            snapshots?.let {
                allProducts.clear() // Reset data master
                for (document in it.documents) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        product.id = document.id
                        allProducts.add(product)
                    }
                }
                // Tampilkan semua data saat pertama kali load (atau saat ada perubahan di DB)
                productAdapter.updateList(allProducts)
            }
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(requireContext())
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
                Toast.makeText(requireContext(), "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal menghapus produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}