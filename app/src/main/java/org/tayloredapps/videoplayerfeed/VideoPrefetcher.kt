package org.tayloredapps.videoplayerfeed

import android.util.Log
import com.google.android.exoplayer2.source.hls.offline.HlsDownloader
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import kotlinx.coroutines.*
import java.io.InterruptedIOException

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
        var prefetcher = Mp4Prefetcher(cacheDataSourceFactory, cache, video)
        GlobalScope.launch(Dispatchers.IO){ prefetcher.prefetchVideo() }
    }

    class Mp4Prefetcher(
        cacheDataSourceFactory: CacheDataSource.Factory,
        var cache: Cache,
        var video: Video
    ) {
        var cacheWriter: CacheWriter
        init {
            var dataSpec = DataSpec(video.url)
            var progressListener = CacheWriter.ProgressListener { requestLength, bytesCached, _ ->
                if (bytesCached >= PRE_CACHE_SIZE) {
                    cancelPreFetch()
                }
                var percentCached = bytesCached * 100.0 / requestLength
                Log.d(TAG, "bytesCached: $bytesCached, percentCached: $percentCached")
            }
            cacheWriter = CacheWriter(
                cacheDataSourceFactory.createDataSource(),
                dataSpec,
                null,
                progressListener)
        }

        private fun cancelPreFetch() {
            cacheWriter.cancel()
        }

        fun prefetchVideo() {
            runCatching {
                if(cache.isCached(video.url.toString(), 0, PRE_CACHE_SIZE)) {
                    return@runCatching
                }
                cacheWriter.cache()
            }
            .onFailure {
                if(it is CancellationException || it is InterruptedException || it is InterruptedIOException) {
                    return@onFailure
                }
                Log.e(TAG,"Error: $it", it)
            }.onSuccess {
                Log.d(TAG,"Sucess")
            }
        }
    }

    class HlsPrefetcher(
        cacheDataSourceFactory: CacheDataSource.Factory,
        var cache: Cache,
        var video: Video
    ) {

       var downloader: HlsDownloader
       init {
           downloader = HlsDownloader(video.mediaItem, cacheDataSourceFactory)
       }

        private fun cancelPreFetch() {
            downloader.cancel()
        }

        suspend fun prefetchVideo() = withContext(Dispatchers.IO) {
            runCatching {
                if(cache.isCached(video.url.toString(), 0, PRE_CACHE_SIZE)) {
                    return@runCatching
                }
                downloader.download { _, bytesDownloaded, percentDownloaded ->
                    if (bytesDownloaded >= PRE_CACHE_SIZE) {
                        cancelPreFetch()
                    }
                    Log.d(TAG, "bytesDownloaded: $bytesDownloaded, percentDownloaded: $percentDownloaded")
                }
                Log.d(TAG,"Dispatching")
            }.onFailure {
                if(it is CancellationException || it is InterruptedException) {
                    return@onFailure
                }
                Log.e(TAG,"Error on ${video.url.toString()}: $it", it)
            }.onSuccess {
                Log.d(TAG,"Sucess")
            }
        }
    }

}