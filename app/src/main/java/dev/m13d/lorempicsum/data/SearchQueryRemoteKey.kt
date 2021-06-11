package dev.m13d.lorempicsum.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_query_remote_keys")
data class SearchQueryRemoteKey(
    @PrimaryKey val searchQuery: String,
    val nextPageKey: Int
)