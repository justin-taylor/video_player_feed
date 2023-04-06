package org.tayloredapps.videoplayerfeed

import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer

enum class VideoType {
    HLS, MP4
}
data class Video(var url: Uri, var videoType: VideoType)

class ExoPlayerItem(
    var exoPlayer: ExoPlayer,
    var position: Int
)

const val videoStrings = """
    https://d2ufudlfb4rsg4.cloudfront.net/newsnation/mkaNi6xbb/adaptive/mkaNi6xbb_master.m3u8
    https://d2ufudlfb4rsg4.cloudfront.net/news12/tWOR0szTg/adaptive/tWOR0szTg_master.m3u8
    https://d2ufudlfb4rsg4.cloudfront.net/yahoofinance/VUmwcy2Sg/adaptive/VUmwcy2Sg_master.m3u8
    https://d2ufudlfb4rsg4.cloudfront.net/abcnews/AlcpSgRDg/adaptive/AlcpSgRDg_master.m3u8
    https://d2ufudlfb4rsg4.cloudfront.net/abcnews/Ph81WEwTg/adaptive/Ph81WEwTg_master.m3u8
    https://d2ufudlfb4rsg4.cloudfront.net/cnn/WqRq0tkSg/adaptive/WqRq0tkSg_master.m3u8
https://d2ufudlfb4rsg4.cloudfront.net/yahoofinance/VUmwcy2Sg/adaptive/VUmwcy2Sg_master.m3u8
http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
https://d2ufudlfb4rsg4.cloudfront.net/news12/tWOR0szTg/adaptive/tWOR0szTg_master.m3u8
http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4
https://d2ufudlfb4rsg4.cloudfront.net/news12/t6k1e6XRg/adaptive/t6k1e6XRg_master.m3u8
http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4
http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4
https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4
https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4
https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4
https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4
https://storage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4
https://storage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4
https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4
"""

fun GetVideoList(): ArrayList<Video> {
    var urlList = videoStrings.trimIndent().split("\n")
    var videoList = ArrayList<Video>()
    for( i in urlList ) {
        try {
            var url =  Uri.parse(i)
            var name = url.lastPathSegment
            var extension = name?.substring(name.lastIndexOf(".") + 1)
            val videoType: VideoType
            when (extension?.lowercase()) {
                "m3u8" -> videoType = VideoType.HLS
                //"mp4" -> videoType = VideoType.MP4
                else -> continue
            }
            videoList.add(Video(url, videoType))
        } catch (e: Exception) {
            Log.e("VIDEOLIST", "Error parsing URL", e)
        }
    }
    return  videoList
}