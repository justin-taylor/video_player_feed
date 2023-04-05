package org.tayloredapps.videoplayerfeed

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

class ExoplayerFactory(var context: Context) {

    fun build(playListener: Player.Listener): ExoPlayer {
       var exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer.addListener(playListener)
        exoPlayer.seekTo(0)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        return exoPlayer
    }

}

class MediaSourceFactory(var cache: SimpleCache, var dataSourceFactory: DataSource.Factory) {
    fun build(video: Video): BaseMediaSource {
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(dataSourceFactory)

        val mediaSource: BaseMediaSource
        when(video.videoType) {
            VideoType.HLS -> mediaSource = HlsMediaSource.Factory(cacheDataSourceFactory).createMediaSource(
                MediaItem.fromUri(video.url))
            VideoType.MP4 -> mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(video.url))
        }
        return mediaSource
    }

}