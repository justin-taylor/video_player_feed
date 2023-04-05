package org.tayloredapps.videoplayerfeed

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.offline.StreamKey
import com.google.android.exoplayer2.source.BaseMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache


class ExoplayerFactory(var context: Context) {

    fun build(playListener: Player.Listener): ExoPlayer {
       var exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer.addListener(playListener)
        exoPlayer.seekTo(0)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        return exoPlayer
    }

}

class VideoMediaSourceFactory(var dataSourceFactory: DataSource.Factory) {

    fun createMediaSource(video: Video): BaseMediaSource {
        var mediaItem = MediaItem.fromUri(video.url)
        var mediaSource: BaseMediaSource = when(video.videoType) {
            VideoType.HLS -> HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            VideoType.MP4 -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
        }
        return mediaSource
    }
}