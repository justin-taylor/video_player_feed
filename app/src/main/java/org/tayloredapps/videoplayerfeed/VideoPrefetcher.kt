package org.tayloredapps.videoplayerfeed

import android.util.Log
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.StreamKey
import com.google.android.exoplayer2.source.hls.offline.HlsDownloader
import com.google.android.exoplayer2.source.hls.playlist.HlsMultivariantPlaylist
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import kotlinx.coroutines.*
import java.util.*

class VideoPrefetcher(
    var cache: Cache,
    var cacheDataSourceFactory: CacheDataSource.Factory
) {
    companion object {
        private const val TAG = "PREFETCHER"
        private const val PRE_CACHE_SIZE = 5 * 1024 * 1024L // only cache 5 mb
    }

    fun prefetchVideo(video: Video) {
        when(video.videoType) {
            VideoType.HLS -> prefetchHlsVideo(video)
            VideoType.MP4 -> prefetchMP4Video(video)
        }
    }

    private fun prefetchHlsVideo(video: Video) {
        var prefetcher = HlsPrefetcher(cacheDataSourceFactory, cache, video)
        GlobalScope.launch(Dispatchers.IO){ prefetcher.prefetchVideo() }
    }

    private fun prefetchMP4Video(video: Video) {
        // TODO
    }

    class HlsPrefetcher(
        cacheDataSourceFactory: CacheDataSource.Factory,
        var cache: Cache,
        var video: Video
    ) {

       var downloader: HlsDownloader
       init {
           var mediaItem = MediaItem.Builder()
               .setUri(video.url).
               setStreamKeys(
                   Collections.singletonList(
                       StreamKey(HlsMultivariantPlaylist.GROUP_INDEX_VARIANT, 0)
                   )
               ).build()
           downloader = HlsDownloader(mediaItem, cacheDataSourceFactory)
       }

        private fun cancelPreFetch() {
            downloader.cancel()
        }

        suspend fun prefetchVideo() = withContext(Dispatchers.IO) {
            runCatching {
                if(cache.isCached(video.url.toString(), 0, PRE_CACHE_SIZE)) {
                    return@runCatching
                }
                downloader.download { contentLength, bytesDownloaded, percentDownloaded ->
                    if (bytesDownloaded >= PRE_CACHE_SIZE) cancelPreFetch()
                    Log.d(TAG, "bytesDownloaded: $bytesDownloaded, percentDownloaded: $percentDownloaded")
                }
                Log.d(TAG,"Dispatching")
            }.onFailure {
                if(it is CancellationException || it is InterruptedException) {
                    return@onFailure
                }
                Log.e(TAG,"Error: $it", it)
            }.onSuccess {
                Log.d(TAG,"Sucess")
            }
        }
    }

}