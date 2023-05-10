package com.example.handtracker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import com.example.handtracker.databinding.ActivityMainBinding
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.WindowManager
import androidx.core.content.FileProvider
import java.util.concurrent.TimeUnit

class MainActivity : Activity(), SensorEventListener  {

    private lateinit var binding: ActivityMainBinding
    private lateinit var textView: TextView
    private lateinit var startButton: View
    private lateinit var stopButton: View

    private var seconds = 0
    private var minutes = 0
    private var isTimerRunning = false
    private lateinit var handler: Handler

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var isLoggingData = false
    private val sensorData = mutableListOf<String>()

    private val REQUEST_PERMISSION_CODE = 100

    private val handPosition = HandPosition()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textView = binding.textView
        startButton = binding.bStart
        stopButton = binding.bStop

        handler = Handler(Looper.getMainLooper())

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    fun startTimer(view: View) {
        isLoggingData = true;
        if (!isTimerRunning) {
            isTimerRunning = true
            handler.postDelayed(timerRunnable, 0)
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    fun stopTimer(view: View) {
        isLoggingData = false;
        writeLogToFile()
        isTimerRunning = false
        isLoggingData = false
        handler.removeCallbacks(timerRunnable)
        sensorManager.unregisterListener(this)
        seconds = 0
        minutes = 0
        updateTimerText()
//        sendEmailWithAttachment()
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            seconds++
            if (seconds >= 60) {
                seconds = 0
                minutes++
            }
            updateTimerText()
            handler.postDelayed(this, 1000)
        }
    }

    private fun updateTimerText() {
        val minutesString = minutes.toString().padStart(2, '0')
        val secondsString = seconds.toString().padStart(2, '0')
        textView.text = "$minutesString:$secondsString"
//        if (minutes == 0 && seconds == 5) {
//            isLoggingData = true
//        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isLoggingData && event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val view = findViewById<View>(R.id.bStart)
            if (handPosition.isWriting(x.toDouble(), y.toDouble(), z.toDouble())) {
                view.setBackgroundColor(Color.GREEN)
            } else {
                view.setBackgroundColor(Color.RED)
            }

            val dataLine = "$currentTime,$x,$y,$z"
            sensorData.add(dataLine)
        }
    }

    private fun writeLogToFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/accelerometer_data.csv"
            val file = File(path)

            file.writeText(sensorData.joinToString(separator = "\n"))
            Log.d("AppDebug", "File saved at: $path")
        } else {
            Log.d("AppDebug", "Permission to write to external storage not granted.")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}