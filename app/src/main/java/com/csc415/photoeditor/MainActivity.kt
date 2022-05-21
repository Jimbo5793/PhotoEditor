package com.csc415.photoeditor

import android.content.Intent
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.csc415.photoeditor.util.insertImage
import java.io.File
import java.io.IOException
import java.util.*

const val PHOTO_URI = "com.csc415.photoeditor.photo_uri"

class MainActivity : AppCompatActivity()
{
	private val tag = this::class.java.simpleName
	private lateinit var currentPhotoPath: String

	private val getContent = registerForActivityResult(GetContent()) {
		startActivity(Intent(this, PhotoEditorActivity::class.java).apply {
			putExtra(PHOTO_URI, it.toString())
		})
	}

	private val takePicture = registerForActivityResult(TakePicture()) {
		val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
		val path = insertImage(contentResolver, bitmap, "Image", "Description")

		if (path == null) Toast.makeText(this, "Image missing or invalid!", Toast.LENGTH_SHORT)
			.show()
		else startActivity(Intent(this, PhotoEditorActivity::class.java).apply {
			putExtra(PHOTO_URI, path)
		})
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		setupPickImageButton()
		setupTakePictureButton()
	}


	private fun setupPickImageButton()
	{
		findViewById<Button>(R.id.pick_image).setOnClickListener {
			getContent.launch("image/*")
		}
	}


	private fun setupTakePictureButton()
	{

		findViewById<Button>(R.id.take_picture).setOnClickListener {
			Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
				takePictureIntent.resolveActivity(packageManager)?.also {

					val photoFile: File? = try
					{
						createImageFile()
					}
					catch (e: IOException)
					{
						Toast.makeText(
							applicationContext, "Unable to create file.", Toast.LENGTH_LONG
						).show()
						Log.w(tag, e)
						null
					}

					photoFile?.also {
						val photoURI: Uri = FileProvider.getUriForFile(
							this, BuildConfig.APPLICATION_ID + ".fileprovider", it
						)

						takePicture.launch(photoURI)
					}
				}
			}
		}
	}


	@Throws(IOException::class)
	private fun createImageFile(): File
	{
		val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
		val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

		return File.createTempFile(
			"JPEG_${timeStamp}_", ".jpg", storageDir
		).apply {
			currentPhotoPath = absolutePath
		}
	}
}