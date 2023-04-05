package org.tayloredapps.videoplayerfeed

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.viewpager2.widget.ViewPager2
import org.tayloredapps.videoplayerfeed.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var videos: ArrayList<Video>

    private lateinit var adapter: VideoAdapter
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()



    @SuppressLint("MissingSuperCall")
    override fun onConfigurationChanged(newConfig: Configuration) {
        // ignore config changes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        videos = GetVideoList()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val cacheSize: Long = 90 * 1024 * 1024
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
        val exoplayerDatabaseProvider = StandaloneDatabaseProvider(this)
        val cache = SimpleCache(cacheDir, cacheEvictor, exoplayerDatabaseProvider)

        adapter = VideoAdapter(this, videos, object : VideoAdapter.OnVideoPreparedListener {
            override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                exoPlayerItems.add(exoPlayerItem)
            }
        }, ExoplayerFactory(this), MediaSourceFactory(cache, DefaultHttpDataSource.Factory()))
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
            }
        }
    }
}