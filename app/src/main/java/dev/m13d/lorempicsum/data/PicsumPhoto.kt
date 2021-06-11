package dev.m13d.lorempicsum.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picsum_photo")
data class PicsumPhoto (
    val id: String?,
    val author: String,
    @PrimaryKey val url: String,
    val download_url: String?,
    val isBookmarked: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "picsum_gallery")
data class PicsumGallery(
    val picsumUrl: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(tableName = "search_results", primaryKeys = ["searchQuery", "picsumUrl"])
data class PicsumSearchResult(
    val searchQuery: String,
    val picsumUrl: String,
    val queryPosition: Int
)