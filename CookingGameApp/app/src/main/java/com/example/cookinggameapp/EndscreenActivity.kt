package com.example.cookinggameapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EndscreenActivity : BaseActivity() {

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraPreview: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_endscreen)

        try {
            // Initialize camera executor first
            cameraExecutor = Executors.newSingleThreadExecutor()

            // Initialize camera preview
            cameraPreview = findViewById(R.id.cameraPreview)

            // Check and request permissions
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error initializing camera", Toast.LENGTH_SHORT).show()
        }

        // Button: Home
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        // Social media sharing buttons
        findViewById<ImageButton>(R.id.btnShareWhatsapp).setOnClickListener {
            openUrl("https://wa.me/")
        }

        findViewById<ImageButton>(R.id.btnShareInstagram).setOnClickListener {
            openUrl("https://www.instagram.com/")
        }

        findViewById<ImageButton>(R.id.btnShareTwitter).setOnClickListener {
            openUrl("https://x.com/home")
        }

        findViewById<ImageButton>(R.id.btnShareFacebook).setOnClickListener {
            openUrl("https://www.facebook.com/")
        }

        // Add click listener for camera preview to take selfie
        cameraPreview.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        try {
            Log.d(TAG, "Starting camera...")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    Log.d(TAG, "Camera provider obtained")

                    // Preview
                    val preview = Preview.Builder()
                        .build()
                        .also {
                            Log.d(TAG, "Setting surface provider for preview")
                            it.setSurfaceProvider(cameraPreview.surfaceProvider)
                        }

                    // ImageCapture
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    // Select front camera
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    Log.d(TAG, "Using front camera selector")

                    try {
                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll()
                        Log.d(TAG, "Unbinding previous use cases")

                        // Bind to lifecycle with both preview and imageCapture
                        val camera = cameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                        Log.d(TAG, "Camera bound to lifecycle successfully")

                    } catch (exc: Exception) {
                        Log.e(TAG, "Use case binding failed", exc)
                        Toast.makeText(this, "Camera binding failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (exc: Exception) {
                    Log.e(TAG, "Failed to get camera provider", exc)
                    Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show()
                }
            }, ContextCompat.getMainExecutor(this))
        } catch (e: Exception) {
            Log.e(TAG, "Error in startCamera: ${e.message}", e)
            Toast.makeText(this, "Unable to start camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create file name with timestamp
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "selfie_$timeStamp.jpg"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, use MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val outputOptions = ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Toast.makeText(this@EndscreenActivity, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Image capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri
                        Toast.makeText(this@EndscreenActivity, "Selfie saved to gallery! 📸", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Photo saved: $savedUri")

                        // Optional: Add vibration feedback
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(100)
                        }
                    }
                }
            )
        } else {
            // For older Android versions
            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val photoFile = File(storageDir, fileName)

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Toast.makeText(this@EndscreenActivity, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Image capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        Toast.makeText(this@EndscreenActivity, "Selfie saved to gallery! 📸", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Photo saved to: ${photoFile.absolutePath}")

                        // Make the image appear in gallery
                        MediaScannerConnection.scanFile(
                            this@EndscreenActivity,
                            arrayOf(photoFile.absolutePath),
                            arrayOf("image/jpeg"),
                            null
                        )

                        // Optional: Add vibration feedback
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(100)
                        }
                    }
                }
            )
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            // Shutdown camera executor
            if (::cameraExecutor.isInitialized) {
                cameraExecutor.shutdown()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}", e)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            // Unbind camera when activity is paused
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding camera in onPause: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Restart camera when activity resumes
        if (allPermissionsGranted()) {
            startCamera()
        }
    }

    companion object {
        private const val TAG = "EndscreenActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }
}