package org.tayloredapps.videoplayerfeed

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import org.tayloredapps.videoplayerfeed.databinding.ActivityMainBinding
import java.io.File
import javax.net.ssl.SSLContext


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: VideoAdapter
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    private lateinit var videoCache: SimpleCache
    private lateinit var videoPrefetcher: VideoPrefetcher
    private lateinit var videos: ArrayList<Video>

    @SuppressLint("MissingSuperCall")
    override fun onConfigurationChanged(newConfig: Configuration) {
        // ignore config changes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Build the Cache
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val cacheSize: Long = 500 * 1024 * 1024 // 500 mb
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
        val exoplayerDatabaseProvider = StandaloneDatabaseProvider(this)

        // first ensure cache is clear on startup
        SimpleCache.delete(cacheDir, exoplayerDatabaseProvider)
        videoCache = SimpleCache(cacheDir, cacheEvictor, exoplayerDatabaseProvider)

        // Build the CacheDataSourceFactory
        val cacheWriteDataSinkFactory = CacheDataSink.Factory().setCache(videoCache)
        val cacheReadDataSourceFactory = FileDataSource.Factory()
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(videoCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setCacheReadDataSourceFactory(cacheReadDataSourceFactory)
            .setCacheWriteDataSinkFactory(cacheWriteDataSinkFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setEventListener(object : CacheDataSource.EventListener {
                override fun onCachedBytesRead(cacheSizeBytes: Long, cachedBytesRead: Long) {
                    Log.d("VideoCache", "onCachedBytesRead. cacheSizeBytes:$cacheSizeBytes, cachedBytesRead: $cachedBytesRead")
                }
                override fun onCacheIgnored(reason: Int) {
                    Log.d("VideoCache", "onCacheIgnored. reason:$reason")
                }
            })

        // Initial Video Prefetch of first 10 items
        videoPrefetcher = VideoPrefetcher(videoCache, cacheDataSourceFactory)
        videos = GetVideoList()
        IntRange(0, 10).forEach {
            if(it >= videos.size) {
                return@forEach
            }
            videoPrefetcher.prefetchVideo(videos[it])
        }

        // Build VideoAdapter
        val playerFactory = ExoplayerFactory(this)
        val mediaSourceFactory = VideoMediaSourceFactory(cacheDataSourceFactory)
        adapter = VideoAdapter(this, videos, object : VideoAdapter.OnVideoPreparedListener {
            override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                val index = exoPlayerItems.indexOfFirst { it.position == exoPlayerItem.position }
                if(index != -1) {
                    exoPlayerItems.removeAt(index)
                }
                exoPlayerItems.add(exoPlayerItem)
            }
        }, playerFactory, mediaSourceFactory)
        binding.videoList.adapter = adapter
        binding.videoList.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            var previousPage: Int = 0

            override fun onPageSelected(position: Int) {
                exoPlayerItems.forEach {
                    val player = it.exoPlayer
                    player.pause()
                    player.playWhenReady = false
                }

                val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                if (newIndex != -1) {
                    val player = exoPlayerItems[newIndex].exoPlayer
                    player.playWhenReady = true
                    player.play()
                }

                val videoRange: IntRange = if(previousPage < position) { // Scrolling forward
                    IntRange(position+1, position+10)
                } else {
                    IntRange(position-1, position-10)
                }
                videoRange.forEach{
                    if(it >= videos.size) {
                        return@forEach
                    }
                }
                previousPage = position
            }
        })
    }

    override fun onPause() {
        super.onPause()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.videoList.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.videoList.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = true
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
                player.release()
            }
        }
        videoCache.release()
    }
}