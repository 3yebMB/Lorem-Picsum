package dev.m13d.lorempicsum.data

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import dev.m13d.lorempicsum.api.PicsumApi
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_PAGE_INDEX = 1

class SearchPicsumMediator(
    private val searchQuery: String,
    private val picsumApi: PicsumApi,
    private val picsumDb: PicsumDatabase,
    private val refreshOnInit: Boolean
) : RemoteMediator<Int, PicsumPhoto>() {

    private val picsumPhotoDao = picsumDb.picsumPhotoDao()
    private val searchQueryRemoteKeyDao = picsumDb.searchQueryRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PicsumPhoto>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> STARTING_PAGE_INDEX
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> searchQueryRemoteKeyDao.getRemoteKey(searchQuery).nextPageKey
        }

        try {
            val response = picsumApi.searchNewPhotos(searchQuery, page, state.config.pageSize)
//            val serverSearchResults = response.picsumPhotos

            val bookmarkedPhotos = picsumPhotoDao.getAllBookmarkedPhotos().first()

            val searchResultPhotos = response.body()!!.map { serverSearchResultPhoto ->
                val isBookmarked = bookmarkedPhotos.any { bookmarkedPhoto ->
                    bookmarkedPhoto.url == serverSearchResultPhoto.url
                }

                PicsumPhoto(
                    id = serverSearchResultPhoto.id,
                    author = serverSearchResultPhoto.author,
                    url = serverSearchResultPhoto.url,
                    download_url = serverSearchResultPhoto.download_url,
                    isBookmarked = isBookmarked
                )
            }

            picsumDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    picsumPhotoDao.deleteSearchResultsForQuery(searchQuery)
                }

                val lastQueryPosition = picsumPhotoDao.getLastQueryPosition(searchQuery) ?: 0
                var queryPosition = lastQueryPosition + 1

                val searchResults = searchResultPhotos.map { photo ->
                    PicsumSearchResult(searchQuery, photo.url, queryPosition++)
                }

                val nextPageKey = page + 1

                picsumPhotoDao.insertPhotos(searchResultPhotos)
                picsumPhotoDao.insertSearchResults(searchResults)
                searchQueryRemoteKeyDao.insertRemoteKey(
                    SearchQueryRemoteKey(searchQuery, nextPageKey)
                )
            }
            return MediatorResult.Success(endOfPaginationReached = response.isSuccessful)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return if (refreshOnInit) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }
}