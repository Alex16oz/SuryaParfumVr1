package com.surya.parfum

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityFormKomplainBinding
import java.util.*

class FormKomplainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormKomplainBinding
    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()

    // ================= PILIH FOTO =================
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            binding.imgBukti.setImageURI(uri) // hanya menampilkan foto
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormKomplainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPilihFoto.setOnClickListener {
            openGallery()
        }

        binding.btnKirimKomplain.setOnClickListener {
            kirimKomplain()
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    // ================= KIRIM KOMPLAIN =================
    private fun kirimKomplain() {
        val nama = binding.etNama.text.toString().trim()
        val noOrder = binding.etNoOrder.text.toString().trim()
        val alasan = binding.etAlasan.text.toString().trim()

        if (nama.isEmpty() || noOrder.isEmpty() || alasan.isEmpty()) {
            Toast.makeText(this, "Semua data wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        simpanKeFirestore(nama, noOrder, alasan)
    }

    // ================= SIMPAN FIRESTORE =================
    private fun simpanKeFirestore(
        nama: String,
        noOrder: String,
        alasan: String
    ) {
        val komplain = hashMapOf(
            "nama" to nama,
            "noOrder" to noOrder,
            "alasan" to alasan,
            "status" to "Menunggu",
            "createdAt" to Date()
            // Jika mau, bisa simpan imageUri.toString() sebagai "fotoUri" tapi hanya berlaku di device user
        )

        db.collection("complaints")
            .add(komplain)
            .addOnSuccessListener {
                Toast.makeText(this, "Komplain berhasil dikirim", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                // Menampilkan error sebenarnya dari Firestore
                Toast.makeText(this, "Gagal mengirim komplain: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
    }
}
