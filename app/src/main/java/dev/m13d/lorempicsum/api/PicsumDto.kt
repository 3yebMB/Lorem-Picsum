package dev.m13d.lorempicsum.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class PicsumDto(
    val id: String,
    val author: String,
//    val width: Int,
//    val height: Int,
    val url: String,
    val download_url: String,
)
