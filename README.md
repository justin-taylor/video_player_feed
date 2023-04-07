# Video Player Feed

Sample application to mimic Tiktok/Instagram Reel style viewing of videos

## Running the Application

1. Clone the repository
  ```
  git clone https://github.com/justin-taylor/video_player_feed.git
  ```
2. Import the Project in Android Studio via `File > New > Import Project` and selecting the root folder `video_player_feed`
1. Allow gradle sync to complete
1. Select a device to run the application on. This can be a physical device or an emulator created in the Device Manager.
1. Ensure the `app` run configuration is selected
1. Run the application from Android Studio

The application has been configured to allow the use of user install SSL certificates for the use of proxying requests
through charles or any other network capture tools

## Overview

The application uses Exoplayer version 2.18.5 (latest at time of writing) in a Viewpager to provide a user
interface similar to that of Instagram Reels or Tiktok.

### Video Stream URLs

The application uses a list of video stream urls to act as a "feed" of video the user can scroll through. Currently
only `mp4` and `m3u8` stream formats are supported, but can be easily extended to other adaptive and progressive stream types

The video stream urls are hardcoded into the `videoStrings` constant in the `models.kt` file. The variable
is formatted such that each stream url is on it's own line and is later parsed into `Video` objects for
use throughout the application.

### Prefetching and Caching

The video cache is stored to disk and uses Exoplayers built in `Cache` with their `CacheDataSource` to fetch and 
store video segments and partial videos in the case of progressive media files.

**NOTE:** For this demo, and for ease of development, the cache is cleared everytime the application is started.

The prefetching logic can be found in the `VideoPrefetcher`. It uses coroutines to fetch and cache a video up to 
a specified amount of memory. This allows the beginning of videos to be prebuffered so when prepared by the exoplayer
the video is able to start automatically. The total cache size for all data is limited by the `Cache` object, this includes
all video fetched by the exoplayer instances, not just the `VideoPrefetcher`.

### Future Improvements
* Update the `VideoPrefetcher` to act as a thread pool for the coroutines to better manage the ongoing jobs.
* Add cache fill awareness to the `VideoPrefetcher` to allow for more intelligent prefetching on prefill and on scroll
* Setup a way for the `VideoPrefetcher` and `VideoAdapter` to evict videos that are invalid
* Use a class of the LifecycleObserver to handle pausing/playing videos when the application is placed into the background
* Generate a preview thumbnail using cached video data
* Adjust the `LoadControl` settings for faster playback startup
* Use a custom http client to manage the connect for downloads
* Migrate from multiple exoplayer instances in the viewpager to single instance
    attached to the view when scrolled. This would be reliant on generating a
    preview thumbnail to give the appearance to users of a ready video

