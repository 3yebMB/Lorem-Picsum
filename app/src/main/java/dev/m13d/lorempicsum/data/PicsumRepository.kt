package dev.m13d.lorempicsum.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import dev.m13d.lorempicsum.api.PicsumApi
import dev.m13d.lorempicsum.util.Resource
import dev.m13d.lorempicsum.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PicsumRepository @Inject constructor(
    private val picsumApi: PicsumApi,
    private val picsumDb: PicsumDatabase
) {
    private val picsumDao = picsumDb.picsumPhotoDao()

    fun getPicsumPhotos(
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<PicsumPhoto>>> =
        networkBoundResource(
            query = {
                picsumDao.getAllPicsumPhotos()
            },
            fetch = {
                picsumApi.getPicsumPhotos()
            },
            saveFetchResult = { serverPicsumPhotos ->
                val bookmarkedPhotos = picsumDao.getAllBookmarkedPhotos().first()

                val picsumPhotos =
                    serverPicsumPhotos.body()!!.map { serverPicsumPhoto ->
                        val isBookmarked = bookmarkedPhotos.any { bookmarkedPhoto ->
                            bookmarkedPhoto.url == serverPicsumPhoto.url
                        }

                        PicsumPhoto(
                            id = serverPicsumPhoto.id,
                            author = serverPicsumPhoto.author,
                            url = serverPicsumPhoto.url,
                            download_url = serverPicsumPhoto.download_url,
                            isBookmarked = isBookmarked
                        )
                    }

                val picsumPhoto = picsumPhotos.map { photo ->
                    PicsumGallery(photo.url)
                }

                picsumDb.withTransaction {
                    picsumDao.deleteAllPicsumPhotos()
                    picsumDao.insertPhotos(picsumPhotos)
                    picsumDao.insertPhotoDallery(picsumPhoto)
                }
            },
            shouldFetch = { cachedPhotos ->
                if (forceRefresh) {
                    true
                } else {
                    val sortedPhotos = cachedPhotos.sortedBy { photo ->
                        photo.updatedAt
                    }
                    val oldestTimestamp = sortedPhotos.firstOrNull()?.updatedAt
                    val needsRefresh = oldestTimestamp == null ||
                            oldestTimestamp < System.currentTimeMillis() -
                            TimeUnit.MINUTES.toMillis(60)
                    needsRefresh
                }
            },
            onFetchSuccess = onFetchSuccess,
            onFetchFailed = { t ->
                if (t !is HttpException && t !is IOException) {
                    throw t
                }
                onFetchFailed(t)
            }
        )

    fun getSearchResultsPaged(
        query: String,
        refreshOnInit: Boolean
    ): Flow<PagingData<PicsumPhoto>> =
        Pager(
            config = PagingConfig(pageSize = 20, maxSize = 100),
            remoteMediator = SearchPicsumMediator(query, picsumApi, picsumDb, refreshOnInit),
            pagingSourceFactory = { picsumDao.getSearchPicsumPhotosPaged(query) }
        ).flow

    fun getAllBookmarkedPhotos(): Flow<List<PicsumPhoto>> =
        picsumDao.getAllBookmarkedPhotos()

    suspend fun updatePhoto(picsumPhoto: PicsumPhoto) {
        picsumDao.updatePhoto(picsumPhoto)
    }

    suspend fun resetAllBookmarks() {
        picsumDao.resetAllBookmarks()
    }

    suspend fun deleteNonBookmarkedPhotosOlderThan(timestampInMillis: Long) {
        picsumDao.deleteNonBookmarkedArticlesOlderThan(timestampInMillis)
    }
}