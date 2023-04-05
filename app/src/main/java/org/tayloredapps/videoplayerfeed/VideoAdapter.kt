package org.tayloredapps.videoplayerfeed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import org.tayloredapps.videoplayerfeed.databinding.ViewHolderVideoPlayerBinding

class VideoAdapter(
    var context: Context,
    var videos: ArrayList<Video>,
    var videoPreparedListener: OnVideoPreparedListener,
    var exoplayerFactory: ExoplayerFactory,
    var mediaSourceFactory: VideoMediaSourceFactory
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(
        binding: ViewHolderVideoPlayerBinding,
        private var videoPreparedListener: OnVideoPreparedListener,
        private var exoPlayer: ExoPlayer,
        private var mediaSourceFactory: VideoMediaSourceFactory
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setVideo(video: Video) {
            exoPlayer.setMediaSource(mediaSourceFactory.createMediaSource(video))
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

        return VideoViewHolder(view, videoPreparedListener, exoPlayer, mediaSourceFactory)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.setVideo(videos[position])
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    interface OnVideoPreparedListener {
        fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
    }
}