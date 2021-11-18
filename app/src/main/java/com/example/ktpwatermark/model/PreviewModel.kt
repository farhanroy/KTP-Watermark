package com.example.ktpwatermark.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PreviewModel(
    val bitmap: Bitmap): Parcelable
