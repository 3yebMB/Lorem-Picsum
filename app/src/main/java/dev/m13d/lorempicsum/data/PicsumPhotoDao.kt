package dev.m13d.lorempicsum.data

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PicsumPhotoDao {

    @Query("SELECT * FROM picsum_gallery INNER JOIN picsum_photo ON picsumUrl = url")
    fun getAllPicsumPhotos(): Flow<List<PicsumPhoto>>

    @Query("SELECT * FROM search_results INNER JOIN picsum_photo ON picsumUrl = url WHERE searchQuery = :query ORDER BY queryPosition")
    fun getSearchPicsumPhotosPaged(query: String): PagingSource<Int, PicsumPhoto>

    @Query("SELECT * FROM picsum_photo WHERE isBookmarked = 1")
    fun getAllBookmarkedPhotos(): Flow<List<PicsumPhoto>>

    @Query("SELECT MAX(queryPosition) FROM search_results WHERE searchQuery = :searchQuery")
    suspend fun getLastQueryPosition(searchQuery: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PicsumPhoto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoDallery(picsumGallery: List<PicsumGallery>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchResults: List<PicsumSearchResult>)

    @Update
    suspend fun updatePhoto(picsumPhoto: PicsumPhoto)

    @Query("UPDATE picsum_photo SET isBookmarked = 0")
    suspend fun resetAllBookmarks()

    @Query("DELETE FROM search_results WHERE searchQuery = :query")
    suspend fun deleteSearchResultsForQuery(query: String)

    @Query("DELETE FROM picsum_gallery")
    suspend fun deleteAllPicsumPhotos()

    @Query("DELETE FROM picsum_photo WHERE updatedAt < :timestampInMillis AND isBookmarked = 0")
    suspend fun deleteNonBookmarkedArticlesOlderThan(timestampInMillis: Long)
}