## Video Engine Proof of Concept

As a initial approach, we are loading the whole file into a byte array.

The App Loads MP4 video and audio files. Use `ByteArrayDataSource` to create media source from `ByteArray`, and merge both video and audio media sources with `MergingMediaSource`.

## Concerns

- It crashes the App with `OutOfMemoryError` when the file is too large.
- In our sample, `ByteStreamDataSource` loads the media source on IO Thread and ExoPlayer reads the data from the video player thread. It requires data synchronization between different threads.
- Working with large ByteArrays may cause JVM to run the garbage collector many times during data synchronization. When it happens, the video playback performance drops a lot. 

## Media Sources

Unzip the source files from `https://drive.google.com/file/d/1-nPaY5iksWiM1wTy4uWva0TFd8ymeTbe/view?usp=sharing` and drop then on `assets` folder
˚
## Sample Code

```kotlin
val player = SimpleExoPlayer.Builder(context).build()
playerView.player = player

val videoByteArray = assets.open("video-2019-12-20T15_44_00.668Z.mp4").toByteArray()
val videoDataSourceFactory = ByteArrayDataSourceFactory(videoByteArray)
val videoSource = ProgressiveMediaSource.Factory(videoDataSourceFactory).createMediaSource()

val audioByteArray = assets.open("audio-2019-12-20T15_44_01.860Z.mp4").toByteArray()
val audioDataSourceFactory = ByteArrayDataSourceFactory(audioByteArray)
val audioSource = ProgressiveMediaSource.Factory(audioDataSourceFactory).createMediaSource()

val mediaSource = MergingMediaSource(videoSource, audioSource)
player.prepare(mediaSource)
```