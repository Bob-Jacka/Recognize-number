package com.example.recognizenumber.pages

import Extensions.initPython
import Extensions.makeShortText
import Extensions.pythonAction
import GlobalSettings.FILENAME_FORMAT
import GlobalSettings.FILE_PATH
import GlobalSettings.FILE_TYPE
import GlobalSettings.LOAD_DUR
import GlobalSettings.TAG
import PythonL
import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.view.Surface
import android.view.View
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
import java.io.IOException
import java.io.OutputStream
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var analyze_btn: FloatingActionButton
    private lateinit var settings_btn: FloatingActionButton
    private lateinit var mask_btn: FloatingActionButton
    private lateinit var progress_bar: ProgressBar
    private lateinit var viewFinder: PreviewView
    private lateinit var image_mask: View

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var isMaskOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewFinder = findViewById(R.id.viewFinder)
        analyze_btn = findViewById(R.id.takeApicture)
        progress_bar = findViewById(R.id.CameraLoad)
        settings_btn = findViewById(R.id.Settings)
        mask_btn = findViewById(R.id.OnMask)
        image_mask = findViewById(R.id.image_mask)
        initPython(this)
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_0)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .build()

        progress_bar.animate().alpha(0.0f).setDuration(LOAD_DUR).start()
        Thread.sleep(LOAD_DUR + 1500).also { viewFinder.visibility = VISIBLE }

        analyze_btn.setOnClickListener { takePhoto() }
        settings_btn.setOnClickListener { startActivity(Intent(this, Settings::class.java)) }
        mask_btn.setOnClickListener {
            if (isMaskOn) {
                isMaskOn = false
                image_mask.visibility = GONE
                viewFinder.visibility = VISIBLE
            } else {
                isMaskOn = true
                image_mask.visibility = VISIBLE
                viewFinder.visibility = GONE
            }
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        makeShortText(applicationContext, pythonAction(PythonL.I_AM_ALIVE).toString())
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

    override fun onDestroy() {
        imageCapture = null
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val preview = Preview.Builder()
            .build().also {
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
            put(Media.RELATIVE_PATH, FILE_PATH)
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()
        imageCapture?.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.d(TAG, "Error in photo capture!!", exc.cause)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo capture succeeded: ${output.savedUri}")
                    neuro_analyze()
                }
            })
    }

    private fun neuro_analyze() {
        val file_name = proceed_photo()
        val python_returnedVal = pythonAction(PythonL.PREDICT_NUMBER, file_name)?.toInt()
        makeShortText(this, "Predicted num is $python_returnedVal")
    }

    /*
     * Функция для обработки фото и ужатия изображения до 28х28
     */
    private fun proceed_photo(): String {
        val contentResolver: ContentResolver = contentResolver
        val inputStream = getLastImageURI().let { contentResolver.openInputStream(it) }

        val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        val resized = Bitmap.createScaledBitmap(bitmap, 28, 28, true)

        val files_values = ContentValues()
        val return_filename = "resized_${System.currentTimeMillis()}" //Resized picture name
        files_values.apply {
            put(Media.DISPLAY_NAME, return_filename)
            put(Media.MIME_TYPE, FILE_TYPE)
            put(Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(Media.IS_PENDING, 1)
        }
        val uri: Uri? = contentResolver
            .insert(Media.EXTERNAL_CONTENT_URI, files_values)
        try {
            val outputStream: OutputStream? = uri?.let { contentResolver.openOutputStream(it) }
            resized.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
            outputStream.flush()
            outputStream.close()
            files_values.clear()
            files_values.put(Media.IS_PENDING, 0)
            contentResolver.update(uri, files_values, null, null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return return_filename
    }

    private fun getLastImageURI(context: Context = this): Uri {
        val contentResolver = context.contentResolver
        val collection = Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(Media._ID)
        var imageUri: Uri? = null
        contentResolver.query(collection, projection, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val count = cursor.count - 1
                cursor.moveToPosition(count)
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(Media._ID))
                imageUri = Uri.withAppendedPath(collection, id.toString())
            }
        }
        return imageUri!!
    }
}