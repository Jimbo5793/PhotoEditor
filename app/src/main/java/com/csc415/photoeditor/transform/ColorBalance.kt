package com.csc415.photoeditor.transform

import android.graphics.Bitmap
import android.graphics.Color
import com.csc415.photoeditor.util.findWhitestPixel
import kotlin.math.max

object ColorBalance : ITransformation
{
	override fun doTransformation(input: Bitmap): Bitmap
	{
		val whitestPixel = findWhitestPixel(input)
		return doTransformation(input, whitestPixel.first, whitestPixel.second)
	}
	
	private fun doTransformation(input: Bitmap, pixelX: Int, pixelY: Int): Bitmap
	{
		var red = Color.red(input.getPixel(pixelX, pixelY))
		var blue = Color.blue(input.getPixel(pixelX, pixelY))
		var green = Color.green(input.getPixel(pixelX, pixelY))

		val width = input.width
		val height = input.height
		val pixels = IntArray(input.width * input.height)


		if (red == blue && blue == green) return input

		var lowColor = red

		if (red > blue) lowColor = blue
		if (red > green) lowColor = green

		red -= lowColor
		blue -= lowColor
		green -= lowColor

		input.getPixels(pixels, 0, width, 0, 0, width, height)

		var currentRed: Int
		var currentBlue: Int
		var currentGreen: Int


		for (i in 0 until input.width * input.height)
		{

			currentRed = max(Color.red(pixels[i]) - red, 0)
			currentBlue = max(Color.blue(pixels[i]) - blue, 0)
			currentGreen = max(Color.green(pixels[i]) - green, 0)

			pixels[i] = (Color.alpha(pixels[i]) shl 24) or (currentRed shl 16) or (currentGreen shl 8) or currentBlue

		}

		input.setPixels(pixels, 0, width, 0, 0, width, height)

		return input
	}
}