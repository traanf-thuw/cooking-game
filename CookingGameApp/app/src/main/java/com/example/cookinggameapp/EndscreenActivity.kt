package com.example.cookinggameapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
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

        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraPreview = findViewById(R.id.cameraPreview)

        setupPermissionsAndCamera()
        setupUIButtons()
        setupCameraTapToCapture()
    }

    private fun setupUIButtons() {
        val buttonActions = mapOf(
            R.id.btnHome to { navigateToMain() },
            R.id.btnShareWhatsapp to { openUrl("https://wa.me/") },
            R.id.btnShareInstagram to { openUrl("https://www.instagram.com/") },
            R.id.btnShareTwitter to { openUrl("https://x.com/home") },
            R.id.btnShareFacebook to { openUrl("https://www.facebook.com/") }
        )

        buttonActions.forEach { (buttonId, action) ->
            findViewById<ImageButton>(buttonId).setOnClickListener { action() }
        }
    }

    private fun setupCameraTapToCapture() {
        cameraPreview.setOnClickListener {
            takePhoto()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun setupPermissionsAndCamera() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera()
        } else {
            showToast("Permissions not granted by the user.")
            finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = buildPreview()
                imageCapture = buildImageCapture()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                handleError("Camera initialization failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun buildPreview(): Preview = Preview.Builder()
        .build()
        .apply {
            setSurfaceProvider(cameraPreview.surfaceProvider)
        }

    private fun buildImageCapture(): ImageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    private fun takePhoto() {
        val imageCapture = this.imageCapture ?: return
        val fileName = buildFileName("selfie_", ".jpg")

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

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), getCallback())
    }

    private fun savePhotoWithMediaStore(imageCapture: ImageCapture, fileName: String) {
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

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), getCallback())
    }

    private fun savePhotoToFileSystem(imageCapture: ImageCapture, fileName: String) {
        val photoFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), getCallback(photoFile))
    }

    private fun getCallback(photoFile: File? = null): ImageCapture.OnImageSavedCallback {
        return object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                handleError("Capture failed", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                showToast("Selfie saved to gallery! 📸")
                photoFile?.let {
                    MediaScannerConnection.scanFile(
                        this@EndscreenActivity,
                        arrayOf(it.absolutePath),
                        arrayOf("image/jpeg"),
                        null
                    )
                }
                vibrate(100)
            }
        }
    }

    private fun buildFileName(prefix: String, extension: String): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "$prefix$timeStamp$extension"
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun vibrate(duration: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleError(msg: String, e: Exception) {
        Log.e(TAG, msg, e)
        showToast(msg)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
    }

    override fun onPause() {
        super.onPause()
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(this).get()
            cameraProvider.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding camera in onPause", e)
        }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) startCamera()
    }

    companion object {
        private const val TAG = "EndscreenActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
