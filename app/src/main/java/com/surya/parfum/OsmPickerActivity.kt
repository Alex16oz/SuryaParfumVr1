package com.surya.parfum

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.surya.parfum.databinding.ActivityOsmPickerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.Locale

class OsmPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOsmPickerBinding

    // Koordinat Toko (Sesuai CheckoutActivity)
    private val storeLocation = GeoPoint(-7.827650797282661, 112.03274612688416)
    private val MAX_RADIUS_KM = 5.0

    private var selectedLocation: GeoPoint? = null
    private var userMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Penting: Inisialisasi konfigurasi OSMDroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName

        binding = ActivityOsmPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMap()
        setupSearch()
        setupListeners()
    }

    private fun setupMap() {
        val map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        // 1. Set Kamera Awal ke Toko
        val mapController = map.controller
        mapController.setZoom(13.5)
        mapController.setCenter(storeLocation)

        // 2. Tambahkan Marker Toko
        val storeMarker = Marker(map)
        storeMarker.position = storeLocation
        storeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        storeMarker.title = "Toko Surya Parfum"
        storeMarker.snippet = "Pusat Pengiriman (Max 5km)"
        // Jika ingin ganti icon toko: storeMarker.icon = resources.getDrawable(R.drawable.ic_store)
        map.overlays.add(storeMarker)

        // 3. Gambar Radius 5KM (Lingkaran)
        // Polygon.pointsAsCircle membuat titik-titik melingkar
        val circlePoints = Polygon.pointsAsCircle(storeLocation, MAX_RADIUS_KM * 1000) // dalam meter
        val circleOverlay = Polygon(map)
        circleOverlay.points = circlePoints
        circleOverlay.fillPaint.color = 0x150000FF // Biru Transparan
        circleOverlay.fillPaint.style = android.graphics.Paint.Style.FILL
        circleOverlay.outlinePaint.color = Color.BLUE
        circleOverlay.outlinePaint.strokeWidth = 3f
        circleOverlay.title = "Area Pengiriman (5km)"
        map.overlays.add(circleOverlay)

        // 4. Deteksi Klik pada Peta untuk pilih lokasi
        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { handleMapTap(it) }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }
        map.overlays.add(MapEventsOverlay(receiver))
    }

    private fun handleMapTap(geoPoint: GeoPoint) {
        // Hapus marker user sebelumnya
        userMarker?.let { binding.mapView.overlays.remove(it) }

        // Hitung Jarak
        val distanceInMeters = storeLocation.distanceToAsDouble(geoPoint)
        val distanceInKm = distanceInMeters / 1000.0

        // Buat Marker Baru di titik klik
        userMarker = Marker(binding.mapView)
        userMarker?.position = geoPoint
        userMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        userMarker?.title = "Lokasi Pilihan"

        // Refresh peta
        binding.mapView.overlays.add(userMarker)
        binding.mapView.invalidate()

        selectedLocation = geoPoint

        // Validasi Jarak
        if (distanceInKm <= MAX_RADIUS_KM) {
            binding.tvDistanceInfo.text = "Jarak: %.2f km (Dalam Area Layanan)".format(distanceInKm)
            binding.tvDistanceInfo.setTextColor(Color.parseColor("#008800")) // Hijau
            binding.btnConfirmLocation.isEnabled = true
            binding.btnConfirmLocation.text = "Pilih Lokasi Ini"
        } else {
            binding.tvDistanceInfo.text = "Jarak: %.2f km (Diluar Jangkauan > 5km)".format(distanceInKm)
            binding.tvDistanceInfo.setTextColor(Color.RED)
            binding.btnConfirmLocation.isEnabled = false
            binding.btnConfirmLocation.text = "Lokasi Terlalu Jauh"
        }
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener { performSearch() }

        binding.etSearchLocation.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }
    }

    private fun performSearch() {
        val query = binding.etSearchLocation.text.toString().trim()
        if (query.isEmpty()) return

        // Gunakan Coroutine agar UI tidak freeze saat geocoding
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@OsmPickerActivity, Locale.getDefault())
                // Mencari lokasi berdasarkan nama (maksimal 1 hasil)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)

                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val resultPoint = GeoPoint(address.latitude, address.longitude)

                        // Pindahkan kamera peta ke hasil pencarian
                        binding.mapView.controller.animateTo(resultPoint)
                        binding.mapView.controller.setZoom(15.0)

                        // Otomatis tandai lokasi tersebut
                        handleMapTap(resultPoint)
                        Toast.makeText(this@OsmPickerActivity, "Lokasi ditemukan", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@OsmPickerActivity, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OsmPickerActivity, "Error pencarian: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnConfirmLocation.setOnClickListener {
            selectedLocation?.let { loc ->
                val resultIntent = Intent()
                resultIntent.putExtra("LATITUDE", loc.latitude)
                resultIntent.putExtra("LONGITUDE", loc.longitude)

                // Coba ambil nama jalan untuk kenyamanan user
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        resultIntent.putExtra("ADDRESS", addresses[0].getAddressLine(0))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    // Lifecycle methods untuk MapView
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
}