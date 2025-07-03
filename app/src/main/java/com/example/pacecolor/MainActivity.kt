package com.example.pacecolor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var layout: RelativeLayout
    private lateinit var paceInput: EditText
    private lateinit var toleranceInput: EditText

    private var lastLocation: Location? = null
    private var desiredPace: Double = 0.0
    private var tolerance: Int = 0

    private lateinit var paceDisplay: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layout = findViewById(R.id.mainLayout)
        paceInput = findViewById(R.id.paceInput)
        toleranceInput = findViewById(R.id.toleranceInput)
        val startButton: Button = findViewById(R.id.startButton)
        paceDisplay = findViewById(R.id.paceDisplay)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        startButton.setOnClickListener {
            val paceText = paceInput.text.toString()
            val toleranceText = toleranceInput.text.toString()

            if (paceText.isNotEmpty() && toleranceText.isNotEmpty()) {
                desiredPace = paceText.replace(",", ".").toDouble()
                tolerance = toleranceText.toInt()
                startLocationUpdates()
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(this, "GPS-Berechtigung erforderlich", Toast.LENGTH_SHORT).show()

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val newLocation = result.lastLocation ?: return

            lastLocation?.let { oldLocation ->
                val distance = oldLocation.distanceTo(newLocation) // meters
                val timeDiff = (newLocation.time - oldLocation.time) / 1000.0 // seconds

                if (timeDiff > 0) {
                    val speedMps = distance / timeDiff
                    val paceMinPerKm = (1000 / speedMps) / 60
                    val paceText = String.format("%.2f min/km", paceMinPerKm)
                    paceDisplay.text = "Aktuelle Pace: $paceText"

                    updateBackgroundColor(paceMinPerKm)
                }
            }

            lastLocation = newLocation
        }
    }

    private fun updateBackgroundColor(pace: Double) {
        val lower = desiredPace - (tolerance / 60.0)
        val upper = desiredPace + (tolerance / 60.0)

        val color = when {
            pace < lower -> Color.parseColor("#0000FF") // Blau
            pace > upper -> Color.parseColor("#FF0000") // Rot
            else -> Color.parseColor("#00FF00") // Grün
        }

        layout.setBackgroundColor(color)
    }
}
