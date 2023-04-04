package org.tayloredapps.videoplayerfeed

import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer

data class Video(var url: Uri)

class ExoPlayerItem(
    var exoPlayer: ExoPlayer,
    var position: Int
)

fun GetVideoList(): ArrayList<Video> {
    var videoStrings = """
http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4
http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4
""".trimIndent().split("\n")

    var videoList = ArrayList<Video>()
    for( i in videoStrings ) {
        videoList.add(Video(Uri.parse(i)))
    }
    return  videoList
}