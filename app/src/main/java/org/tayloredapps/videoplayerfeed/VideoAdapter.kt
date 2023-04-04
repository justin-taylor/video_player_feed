package org.tayloredapps.videoplayerfeed

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import org.tayloredapps.videoplayerfeed.databinding.ViewHolderVideoPlayerBinding

class VideoAdapter(
    var context: Context,
    var videos: ArrayList<Video>,
    var videoPreparedListener: OnVideoPreparedListener,
    var exoplayerFactory: ExoplayerFactory
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(
        private val binding: ViewHolderVideoPlayerBinding,
        private var context: Context,
        private var videoPreparedListener: OnVideoPreparedListener,
        private var exoPlayer: ExoPlayer
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setVideoPath(url: Uri) {
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(url))
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()

            if (absoluteAdapterPosition == 0) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }

            videoPreparedListener.onVideoPrepared(ExoPlayerItem(exoPlayer, absoluteAdapterPosition))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = ViewHolderVideoPlayerBinding.inflate(LayoutInflater.from(context), parent, false)
        val exoPlayer = exoplayerFactory.build(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Toast.makeText(context, "Can't play this video", Toast.LENGTH_SHORT).show()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    view.loadIndicator.visibility = View.VISIBLE
                } else if (playbackState == Player.STATE_READY) {
                    view.loadIndicator.visibility = View.GONE
                }
            }
        })
        view.playerView.player = exoPlayer

        return VideoViewHolder(view, context, videoPreparedListener, exoPlayer)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val model = videos[position]
        holder.setVideoPath(model.url)
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    interface OnVideoPreparedListener {
        fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
    }
}