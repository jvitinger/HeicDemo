package com.example.heicdemo

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.heifwriter.HeifWriter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

/**
 * https://developer.android.com/jetpack/androidx/releases/heifwriter
 */
class MainActivity : AppCompatActivity() {

    private val OUTPUT_FILE_NAME =
        "${Environment.getExternalStorageDirectory().path}/dem_heic_output.heic"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_save_heic.setOnClickListener { saveHeic() }
    }

    private fun saveHeic() {
        val sourceBitmap =
            BitmapFactory.decodeResource(resources, R.drawable.mushroom, BitmapFactory.Options())

        // Just have some reasonable size - set the width 1600 and calculate appropriate height
        val outputWidth = 1600
        val outputHeight = sourceBitmap.height * outputWidth / sourceBitmap.width

        try {
            File(OUTPUT_FILE_NAME).run {
                if (exists()) {
                    delete()
                }
            }

            val heifWriter =
                HeifWriter.Builder(
                    OUTPUT_FILE_NAME,
                    outputWidth,
                    outputHeight,
                    HeifWriter.INPUT_MODE_BITMAP
                )
                    .setQuality(90)
                    .setMaxImages(1)
                    .build()

            heifWriter.start()
            heifWriter.addBitmap(sourceBitmap)

            // Warning: this should not be called on the main thread in the real app!
            heifWriter.stop(30000)
            heifWriter.close()

            Toast.makeText(this, "File saved $OUTPUT_FILE_NAME", Toast.LENGTH_LONG).show()
            showPicture()
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "It looks like the device doesn't support HEIC: $ex",
                Toast.LENGTH_LONG
            ).show()
            Log.e("HEIC", "Failed to save HEIC", ex)
        }
    }

    private fun showPicture() {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.action = Intent.ACTION_VIEW
        val fileURI = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".FileProvider", File(OUTPUT_FILE_NAME)
        )
        intent.setDataAndType(fileURI, "image/*")
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(
                this,
                permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
    }
}