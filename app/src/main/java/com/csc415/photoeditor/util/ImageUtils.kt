package com.csc415.photoeditor.util

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.provider.MediaStore
import android.util.Pair
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

fun findWhitestPixel(input: Bitmap): Pair<Int, Int>
{
	var pixelCoordinates = Pair(0, 0)
	var average = 0.0

	val width = input.width
	val height = input.height
	val pixels = IntArray(input.width * input.height)

	input.getPixels(pixels, 0, width, 0, 0, width, height)

	// Go through each pixel in the image to find the whitest pixel.
	for (i in 0 until input.width * input.height)
	{
		val red = Color.red(pixels[i])
		val green = Color.green(pixels[i])
		val blue = Color.blue(pixels[i])

		// Whiter pixel is found.
		if (average < (red + green + blue) / 3.0)
		{
			val x = (i % width) + 1
			val y = (i / width) + 1
			average = (red + green + blue) / 3.0
			pixelCoordinates = Pair(x, y)
		}
	}

	return pixelCoordinates
}


fun compressImage(stream: InputStream, maxWidth: Int, maxHeight: Int): Bitmap
{
	return compressImage(BitmapFactory.decodeStream(stream), maxWidth, maxHeight)
}

fun compressImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap
{
	if (maxHeight < 1 || maxWidth < 1) throw IllegalArgumentException("Height and width must be at least 1.")

	var image = bitmap
	val ratioBitmap = image.width.toFloat() / image.height.toFloat()
	val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
	var finalWidth = maxWidth
	var finalHeight = maxHeight

	if (ratioMax > ratioBitmap) finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
	else finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()

	image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)

	val outputStream = ByteArrayOutputStream()
	image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

	return BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))
}

fun insertImage(cr: ContentResolver, source: Bitmap, title: String, description: String): String?
{
	return try
	{
		val url = cr.insert(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			setContentValues(title, description)
		)!!

		cr.openOutputStream(url).use {
			source.compress(Bitmap.CompressFormat.PNG, 100, it)
		}
		url.toString()
	}
	catch (e: Exception)
	{
		null
	}
}
private fun setContentValues(title: String?, description: String?): ContentValues
{
	return ContentValues().apply {
		put(MediaStore.Images.Media.TITLE, title)
		put(MediaStore.Images.Media.DISPLAY_NAME, title)
		put(MediaStore.Images.Media.DESCRIPTION, description)
		put(MediaStore.Images.Media.MIME_TYPE, "image/png")
		// Add the date meta data to ensure the image is added at the front of the gallery
		put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
		put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
	}
}