package com.surya.parfum

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.surya.parfum.databinding.FragmentOrderListBinding

class OrderListFragment : Fragment() {

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var orderAdapter: OrderHistoryAdapter
    private val orderList = mutableListOf<Order>()

    private var filterType: String = "active"

    companion object {
        fun newInstance(filterType: String): OrderListFragment {
            val fragment = OrderListFragment()
            val args = Bundle()
            args.putString("FILTER_TYPE", filterType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterType = it.getString("FILTER_TYPE", "active")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        fetchOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderHistoryAdapter(orderList) { order ->
            val intent = Intent(requireContext(), UserOrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }
        binding.rvOrderList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun fetchOrders() {
        val userId = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        var query: Query = db.collection("orders")
            .whereEqualTo("userId", userId)

        // === LOGIKA STATUS DIPERBARUI DI SINI ===
        if (filterType == "active") {
            // Tab "Dalam Proses" sekarang mencakup:
            // 1. "Diproses" (Baru masuk)
            // 2. "Disetujui" (Sedang disiapkan/dikirim)
            query = query.whereIn("status", listOf("Diproses", "Disetujui"))
        } else {
            // Tab "Riwayat" hanya mencakup yang sudah final:
            // 1. "Ditolak"
            // 2. "Selesai"
            query = query.whereIn("status", listOf("Ditolak", "Selesai"))
        }

        // Sorting
        query = query.orderBy("orderDate", Query.Direction.DESCENDING)

        query.addSnapshotListener { snapshots, error ->
            // Cek binding agar tidak crash jika fragment sudah hancur saat data masuk
            if (_binding == null) return@addSnapshotListener

            binding.progressBar.visibility = View.GONE

            if (error != null) {
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

                if (orderList.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvOrderList.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvOrderList.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}