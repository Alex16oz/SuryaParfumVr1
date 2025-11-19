package com.surya.parfum

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.surya.parfum.databinding.FragmentAdminOrderListBinding

class AdminOrderListFragment : Fragment(), OrderAdminAdapter.OrderActionListener {

    private var _binding: FragmentAdminOrderListBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var orderAdapter: OrderAdminAdapter
    private val orderList = mutableListOf<Order>()

    // Variabel untuk menyimpan tipe filter (default: active)
    private var filterType: String = "active"

    companion object {
        // Fungsi helper untuk membuat instance fragment dengan filter tertentu
        fun newInstance(filterType: String): AdminOrderListFragment {
            val fragment = AdminOrderListFragment()
            val args = Bundle()
            args.putString("FILTER_TYPE", filterType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ambil argumen filter saat fragment dibuat
        arguments?.let {
            filterType = it.getString("FILTER_TYPE", "active")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        fetchOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdminAdapter(orderList, this)
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun fetchOrders() {
        // Base Query
        var query: Query = db.collection("orders")

        // Terapkan filter berdasarkan tab
        if (filterType == "active") {
            // Tab "Perlu Diproses": Hanya status 'Diproses'
            query = query.whereEqualTo("status", "Diproses")
        } else {
            // Tab "Riwayat": Status selain 'Diproses' (Disetujui, Ditolak, Selesai)
            query = query.whereIn("status", listOf("Disetujui", "Ditolak", "Selesai"))
        }

        // Urutkan berdasarkan tanggal terbaru
        // PENTING: Jika query ini error di Logcat, klik link yang muncul untuk membuat Index di Firebase Console!
        query = query.orderBy("orderDate", Query.Direction.DESCENDING)

        query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                // Jangan tampilkan toast error permission jika sedang loading awal (opsional)
                // Tapi print log agar developer tahu
                error.printStackTrace()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                orderList.clear()
                for (document in snapshots.documents) {
                    val order = document.toObject(Order::class.java)
                    if (order != null) {
                        order.id = document.id
                        orderList.add(order)
                    }
                }
                orderAdapter.notifyDataSetChanged()

                // Tampilkan pesan kosong jika tidak ada data
                if (orderList.isEmpty()) {
                    // Anda bisa menambahkan TextView "Data Kosong" di XML dan mengaturnya di sini
                }
            }
        }
    }

    override fun onApproveClick(order: Order) {
        updateOrderStatus(order.id, "Disetujui")
    }

    override fun onRejectClick(order: Order) {
        updateOrderStatus(order.id, "Ditolak")
    }

    override fun onItemClick(order: Order) {
        val intent = Intent(requireContext(), AdminOrderDetailActivity::class.java).apply {
            putExtra("ORDER_ID", order.id)
        }
        startActivity(intent)
    }

    private fun updateOrderStatus(orderId: String, newStatus: String) {
        val updateMap = mapOf("status" to newStatus)

        db.collection("orders").document(orderId)
            .update(updateMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Status diperbarui: $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal update: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}