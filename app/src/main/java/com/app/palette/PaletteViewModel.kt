package com.app.palette

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette

class PaletteViewModel : ViewModel() {

    private val _bitmap: MutableLiveData<Bitmap> =
        MutableLiveData<Bitmap>()

    val bitmap: LiveData<Bitmap>
        get() = _bitmap

    fun setBitmap(bitmap: Bitmap) {
        _bitmap.value = bitmap
    }

    private val _palette: MutableLiveData<Palette> =
        MutableLiveData<Palette>()

    val palette: LiveData<Palette>
        get() = _palette

    fun setPalette(palette: Palette) {
        _palette.value = palette
    }
}