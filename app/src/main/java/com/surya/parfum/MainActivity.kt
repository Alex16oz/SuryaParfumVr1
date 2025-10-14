package com.surya.parfum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.surya.parfum.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductListFragment())
                .commit()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // --- PASTIKAN BLOK INI ADA ---
                R.id.action_cart -> {
                    // Cek dulu apakah pengguna sudah login
                    if (auth.currentUser != null) {
                        startActivity(Intent(this, CartActivity::class.java))
                    } else {
                        Toast.makeText(this, "Silakan login untuk melihat keranjang", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true // event ditangani
                }

                R.id.action_profile -> {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        showPopupMenu()
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true // event ditangani
                }

                else -> false
            }
        }
    }

    private fun showPopupMenu() {
        val anchorView = findViewById<View>(R.id.action_profile)
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.profile_popup_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_popup_logout) {
                auth.signOut()
                Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }
        popup.show()
    }
}