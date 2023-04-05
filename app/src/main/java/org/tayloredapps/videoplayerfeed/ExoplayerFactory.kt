package org.tayloredapps.videoplayerfeed

import android.content.Context
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class ExoplayerFactory(var context: Context) {

    fun build(playListener: Player.Listener): ExoPlayer {
       var exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer.addListener(playListener)
        exoPlayer.seekTo(0)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        return exoPlayer
    }

}