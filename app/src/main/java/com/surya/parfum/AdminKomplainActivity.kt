package com.surya.parfum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminKomplainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: KomplainAdapter
    private val listKomplain = mutableListOf<Komplain>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ HARUS layout activity, BUKAN item
        setContentView(R.layout.activity_admin_komplain)

        recyclerView = findViewById(R.id.rvKomplain)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = KomplainAdapter(listKomplain)
        recyclerView.adapter = adapter

        // 🔥 Ambil data komplain dari Firestore
        loadKomplain()
    }

    private fun loadKomplain() {
        db.collection("complaints")
            .orderBy("createdAt")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                listKomplain.clear()

                snapshots?.forEach { document ->
                    val komplain = document.toObject(Komplain::class.java)
                    listKomplain.add(komplain)
                }

                adapter.notifyDataSetChanged()
            }
    }
}
