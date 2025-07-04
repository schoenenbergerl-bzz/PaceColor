// DarkModeActivity.kt
package com.example.pacecolor

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class StartActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var paceInput: EditText
    private lateinit var toleranceInput: EditText
    private lateinit var layout: RelativeLayout
    private lateinit var paceDisplay: TextView
    private lateinit var startButton: Button
    private lateinit var switchToLightButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var desiredPace = 0.0
    private var tolerance = 0
    private val LOCATION_PERMISSION_REQUEST_CODE = 1002
    private var lastLocation: Location? = null

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var stepCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dark_mode)

        layout = findViewById(R.id.darkLayout)
        paceInput = findViewById(R.id.paceInput)
        toleranceInput = findViewById(R.id.toleranceInput)
        paceDisplay = findViewById(R.id.paceDisplay)
        startButton = findViewById(R.id.startButton)
        switchToLightButton = findViewById(R.id.backButton)

        layout.setBackgroundColor(0xFF000000.toInt())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        checkLocationPermission()

        startButton.setOnClickListener {
            val paceText = paceInput.text.toString()
            val toleranceText = toleranceInput.text.toString()

            if (paceText.isNotEmpty() && toleranceText.isNotEmpty()) {
                desiredPace = paceText.replace(",", ".").toDouble()
                tolerance = toleranceText.toInt()
                startLocationUpdates()
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        switchToLightButton.text = "Light Mode"
        switchToLightButton.setOnClickListener {
            finish() // zurück zur MainActivity
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000
        ).setMinUpdateIntervalMillis(1000).build()

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val newLocation = result.lastLocation ?: return
                lastLocation?.let { oldLocation ->
                    val distance = oldLocation.distanceTo(newLocation)
                    val timeDiff = (newLocation.time - oldLocation.time) / 1000.0

                    if (timeDiff > 0) {
                        val speedMps = distance / timeDiff
                        val paceMinPerKm = (1000 / speedMps) / 60
                        paceDisplay.text = "Aktuelle Pace: %.2f min/km".format(paceMinPerKm)
                        updateBackgroundColor(paceMinPerKm)
                    }
                }
                lastLocation = newLocation
            }
        }, mainLooper)
    }

    private fun updateBackgroundColor(pace: Double) {
        val lower = desiredPace - (tolerance / 60.0)
        val upper = desiredPace + (tolerance / 60.0)
        val color = when {
            pace < lower -> 0xFF4444AA.toInt() // dunkelblau
            pace > upper -> 0xFFAA4444.toInt() // dunkelrot
            else -> 0xFF228822.toInt() // dunkelgrün
        }
        layout.setBackgroundColor(color)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Standortberechtigung abgelehnt", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount = event.values[0].toInt()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
