package com.surya.parfum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnRegister.setOnClickListener {
            val name = binding.etRegisterName.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                // Buat pengguna baru di Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Jika berhasil, ambil UID pengguna baru
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // Simpan informasi tambahan (nama) ke Firestore
                                saveUserToFirestore(userId, name, email)
                            }
                        } else {
                            // Jika gagal, tampilkan pesan error
                            Toast.makeText(this, "Registrasi Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Semua kolom wajib diisi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserToFirestore(userId: String, name: String, email: String) {
        val userMap = hashMapOf(
            "name" to name,
            "email" to email
        )

        // Simpan data ke koleksi 'users' dengan ID dokumen = UID pengguna
        db.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                // Tutup halaman registrasi dan kembali ke halaman login
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan data pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}