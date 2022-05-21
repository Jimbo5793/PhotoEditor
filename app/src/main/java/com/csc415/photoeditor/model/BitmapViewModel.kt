package com.csc415.photoeditor.model

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

class BitmapViewModel : ViewModel()
{
	var bitmap: Bitmap? = null
	var originalImage: Bitmap? = null
}