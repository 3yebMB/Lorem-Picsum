package dev.m13d.lorempicsum.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PicsumPhoto::class, PicsumGallery::class, PicsumSearchResult::class, SearchQueryRemoteKey::class],
    version = 1
)
abstract class PicsumDatabase : RoomDatabase() {

    abstract fun picsumPhotoDao(): PicsumPhotoDao

    abstract fun searchQueryRemoteKeyDao(): SearchQueryRemoteKeyDao
}