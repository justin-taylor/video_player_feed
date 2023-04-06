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

## Overview

The application uses Exoplayer version 2.18.5 (latest at time of writing) in a Viewpager to provide a user
interface similar to that of Instagram Reels or Tiktok

### Caching/Prefetch strategy


