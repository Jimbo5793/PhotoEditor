package com.csc415.photoeditor

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csc415.photoeditor.model.BitmapViewModel
import com.csc415.photoeditor.transform.ColorBalance
import com.csc415.photoeditor.transform.ColorInvert
import com.csc415.photoeditor.transform.Exposure
import com.csc415.photoeditor.util.*
import java.io.*
import java.util.*

class PhotoEditorActivity : AppCompatActivity()
{
	private val tag = this::class.java.simpleName
	private lateinit var imageUri: String
	private lateinit var bitmapModel: BitmapViewModel

	private val mEditorButtonList = LinkedList<EditorButtonEnum>()
	private lateinit var mRecyclerView: RecyclerView
	private lateinit var mAdapter: ListAdapter

	@Suppress("DEPRECATION")
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photo_editor)

		bitmapModel = ViewModelProviders.of(this).get(BitmapViewModel::class.java)

		if (intent.extras!!.containsKey(PHOTO_URI))
		{

			imageUri = intent.getStringExtra(PHOTO_URI)!!
			Log.d(tag, imageUri)

			try
			{
				if (bitmapModel.bitmap == null)
				{

					val stream: InputStream = if (imageUri.contains("content:")) contentResolver.openInputStream(
						Uri.parse(imageUri)
					)!!
					else FileInputStream(File(imageUri))

					val matrix = Matrix()

					val display = windowManager.defaultDisplay
					var bitmap = compressImage(stream, display.width, display.height)

					bitmap = Bitmap.createBitmap(
						bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
					)
					findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
					bitmapModel.originalImage = bitmap
					bitmapModel.bitmap = bitmap
				}
				else findViewById<ImageView>(R.id.photo).setImageBitmap(bitmapModel.bitmap)
			}
			catch (e: FileNotFoundException)
			{
				Log.e(
					tag,
					"Image file not found or couldn't be opened. Falling back to MainActivity",
					e
				)
				startActivity(Intent(this, MainActivity::class.java))
				Toast.makeText(applicationContext, "Could not open image.", Toast.LENGTH_LONG)
					.show()
			}
		}
		else
		{
			Log.w(tag, "No IMAGE_URI was sent with Intent. Falling back to MainActivity")
			startActivity(Intent(this, MainActivity::class.java))
		}
		createButtons()
		mRecyclerView = findViewById(R.id.recyclerview)
		mAdapter = ListAdapter(this,
			mEditorButtonList,object : ClickListener {
				override fun onPositionClicked(mPosition: Int) {
					when (mEditorButtonList[mPosition]){
						EditorButtonEnum.BALANCE -> colorBalance()
						EditorButtonEnum.EXPOSE -> exposure()
						EditorButtonEnum.INVERT_COLORS -> invertColors()
						EditorButtonEnum.SAVE -> save()
						EditorButtonEnum.SHARE -> share()
						EditorButtonEnum.EXIT -> exit()
						EditorButtonEnum.UNDO -> undo()
					}
				}
			})

		mRecyclerView.adapter = mAdapter
		mRecyclerView.layoutManager = LinearLayoutManager(
			this, LinearLayoutManager.HORIZONTAL,false)
	}


	private fun createButtons() {
		EditorButtonEnum.values().forEach { mEditorButtonList.add(it) }
	}

	private fun undo() {
		findViewById<ImageView>(R.id.photo).setImageBitmap(bitmapModel.originalImage)
//		findViewById<Button>(R.id.button_editor).setTextColor(Color.YELLOW)
//		findViewById<Button>(R.id.expose).setTextColor(Color.YELLOW)
//		findViewById<Button>(R.id.balance).setTextColor(Color.YELLOW)
	}


	private fun invertColors()
	{
		//findViewById<Button>(R.id.button_editor).setTextColor(Color.CYAN)
		var bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
		bitmap = bitmap.copy(bitmap.config, true)
		bitmap = ColorInvert.doTransformation(bitmap)
		findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
		bitmap.also { bitmapModel.bitmap = it }
	}

	private fun save()
	{
		val bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
		insertImage(contentResolver, bitmap, "image", "description")
		finish()
	}


	private fun exit()
	{
		startActivity(Intent(this, MainActivity::class.java))
	}

	private fun share()
	{
		//shareButton.setTextColor(Color.CYAN)
		// Save file to disk in a temp file.
		val file = File(
			getExternalFilesDir(Environment.DIRECTORY_PICTURES),
			"temp_${System.currentTimeMillis()}.png"
		)
		val stream = FileOutputStream(file)
		val bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
		stream.close()

		val uri = FileProvider.getUriForFile(
			applicationContext, "${applicationContext.packageName}.fileprovider", file
		)

		val intent = Intent().apply {
			type = "image/*"
			action = Intent.ACTION_SEND
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			putExtra(Intent.EXTRA_STREAM, uri)
		}


		val chooser = Intent.createChooser(intent, "Share to")


		packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
			.forEach {
				grantUriPermission(
					it.activityInfo.packageName,
					uri,
					Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
				)
			}
		startActivity(chooser)
	}


	private fun exposure()
	{

		//exposeButton.setTextColor(Color.CYAN)
		var bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
		bitmap = bitmap.copy(bitmap.config, true)
		bitmap = Exposure.doTransformation(bitmap)
		findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
		bitmap.also { bitmapModel.bitmap = it }
	}


	private fun colorBalance()
	{
		//colorBalanceButton.setTextColor(Color.CYAN)
		var bitmap = (findViewById<ImageView>(R.id.photo).drawable as BitmapDrawable).bitmap
		bitmap = bitmap.copy(bitmap.config, true)
		bitmap = ColorBalance.doTransformation(bitmap)
		findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)
		bitmap.also { bitmapModel.bitmap = it }
	}
}