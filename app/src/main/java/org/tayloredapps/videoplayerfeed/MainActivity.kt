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

    @SuppressLint("MissingSuperCall")
    override fun onConfigurationChanged(newConfig: Configuration) {
        // ignore config changes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var context = SSLContext.getInstance("TLSv1.2")
        context.init(null, null, null)
        context.createSSLEngine()



        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Build the Cache
        var dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        var cacheSize: Long = 500 * 1024 * 1024 // 500 mb
        var cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
        var exoplayerDatabaseProvider = StandaloneDatabaseProvider(this)

        // first ensure cache is clear on startup
        SimpleCache.delete(cacheDir, exoplayerDatabaseProvider)
        videoCache = SimpleCache(cacheDir, cacheEvictor, exoplayerDatabaseProvider)

        // Build the CacheDataSourceFactory
        var cacheWriteDataSinkFactory = CacheDataSink.Factory().setCache(videoCache)
        var cacheReadDataSourceFactory = FileDataSource.Factory()
        var cacheDataSourceFactory = CacheDataSource.Factory()
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

        // Initial Video Prefetch
        videoPrefetcher = VideoPrefetcher(videoCache, cacheDataSourceFactory)
        var videos = GetVideoList()
        videos.forEach {
            videoPrefetcher.prefetchVideo(it)
        }

        // Build VideoAdapter
        var playerFactory = ExoplayerFactory(this)
        var mediaSourceFactory = VideoMediaSourceFactory(cacheDataSourceFactory)
        adapter = VideoAdapter(this, videos, object : VideoAdapter.OnVideoPreparedListener {
            override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                var index = exoPlayerItems.indexOfFirst { it.position == exoPlayerItem.position }
                if(index != -1) {
                    exoPlayerItems.removeAt(index)
                }
                exoPlayerItems.add(exoPlayerItem)
            }
        }, playerFactory, mediaSourceFactory)
        binding.videoList.adapter = adapter
        binding.videoList.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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

                // TODO Prefetch next items
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