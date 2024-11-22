package com.example.recognizenumber.pages

import Extensions.initPython
import Extensions.makeShortText
import Extensions.pythonAction
import GlobalSettings.FILENAME_FORMAT
import GlobalSettings.FILE_PATH
import GlobalSettings.FILE_TYPE
import GlobalSettings.TAG
import Pythonl
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.recognizenumber.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.util.concurrent.ListenableFuture
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {

    private lateinit var analyze_btn: FloatingActionButton
    private lateinit var settings_btn: FloatingActionButton
    private lateinit var progress_bar: ProgressBar
    private lateinit var viewFinder: PreviewView

    private var imageCapture: ImageCapture? = null
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewFinder = findViewById(R.id.viewFinder)
        analyze_btn = findViewById(R.id.takeApicture)
        progress_bar = findViewById(R.id.CameraLoad)
        settings_btn = findViewById(R.id.Settings)
        initPython(this)
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_0)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .build()
        progress_bar.animate().alpha(0.0f)
        progress_bar.animate().setDuration(750)

        analyze_btn.setOnClickListener { takePhoto() }
        settings_btn.setOnClickListener { startActivity(Intent(this, Settings::class.java)) }
        cameraExecutor = Executors.newSingleThreadExecutor()
        makeShortText(applicationContext, pythonAction(Pythonl.I_AM_ALIVE).toString())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageCapture, preview
                )
            } catch (exc: Exception) {
                makeShortText(
                    applicationContext,
                    "Use case binding failed"
                )
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val name = SimpleDateFormat(
            FILENAME_FORMAT,
            Locale.US
        ).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, FILE_TYPE)
            put(MediaStore.Images.Media.RELATIVE_PATH, FILE_PATH)
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()
        imageCapture?.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    makeShortText(applicationContext, "Error in photo capture")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo capture succeeded: ${output.savedUri}")
                }
            }) ?: makeShortText(applicationContext, "error in imageCapture")
        try {
            neuro_analyze("/storage/emulated/0/$FILE_PATH/$name.jpeg")
        } catch (e: UninitializedPropertyAccessException) {
            Log.d("MainActivity", "error in take photo, because path is null")
        }
    }

    private fun neuro_analyze(path_to_img: String = "/storage/emulated/0/$FILE_PATH/") {
        viewFinder.visibility = GONE
        progress_bar.animate().start()
        val returnedVal = pythonAction(Pythonl.PREDICT_NUMBER, path_to_img)?.toInt()
        viewFinder.visibility = VISIBLE
        makeShortText(this, "Predicted num is $returnedVal")
    }

    override fun onDestroy() {
        imageCapture = null
        cameraExecutor.shutdown()
        super.onDestroy()
    }
}