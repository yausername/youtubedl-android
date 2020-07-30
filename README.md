# youtubedl-android
Android library wrapper for [youtube-dl](https://github.com/rg3/youtube-dl) executable

[![](https://jitpack.io/v/yausername/youtubedl-android.svg)](https://jitpack.io/#yausername/youtubedl-android)


## Credits
*  [youtubedl-java](https://github.com/sapher/youtubedl-java) by [sapher](https://github.com/sapher), youtubedl-android adds android compatibility to youtubedl-java.

<br/>

## Sample app
Debug apk for testing can be downloaded from the [releases page](https://github.com/yausername/youtubedl-android/releases)
<br/>
<br/>
![Download Example](https://media.giphy.com/media/fvI9yytF4rxmH7pGHu/giphy.gif)
![Streaming Example](https://media.giphy.com/media/UoqecxgY9IWbUs5tSR/giphy.gif)



Checkout [dvd](https://github.com/yausername/dvd), a video downloader app based on this library.

![dvd](https://imgur.com/download/DdhdBuc)

## Installation

### Gradle
Step 1 : Add jitpack repository to your project build file
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2: Add the dependency
```gradle
dependencies {
    implementation 'com.github.yausername.youtubedl-android:library:0.12.+'
}
```
Optional FFmpeg dependency can also be added
```gradle
dependencies {
    implementation 'com.github.yausername.youtubedl-android:library:0.12.+'
    implementation 'com.github.yausername.youtubedl-android:ffmpeg:0.12.+'
}
```
<br/>

* Set `android:extractNativeLibs="true"` in your app's manifest.
* Use `abiFilters 'x86', 'x86_64', 'armeabi-v7a', 'arm64-v8a'` in app/build.gradle, see [sample app](https://github.com/yausername/youtubedl-android/blob/master/app/build.gradle).
* Use abi splits to reduce apk size, see [sample app](https://github.com/yausername/youtubedl-android/blob/master/app/build.gradle).
* On android 10 set `android:requestLegacyExternalStorage="true"`. I haven't tested with scoped storage, feel free to do so.

<br/>

## Usage

* youtube-dl executable and python 3.8 are bundled in the library.
* Initialize library, preferably in `onCreate`.

```java
try {
    YoutubeDL.getInstance().init(getApplication());
} catch (YoutubeDLException e) {
    Log.e(TAG, "failed to initialize youtubedl-android", e);
}
```


* Downloading / custom command (A detailed example can be found in the [sample app](app/src/main/java/com/yausername/youtubedl_android_example/DownloadingExampleActivity.java))
```java
File youtubeDLDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "youtubedl-android");
YoutubeDLRequest request = new YoutubeDLRequest("https://vimeo.com/22439234");
request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
YoutubeDL.getInstance().execute(request, (progress, etaInSeconds) -> {
    System.out.println(String.valueOf(progress) + "% (ETA " + String.valueOf(etaInSeconds) + " seconds)");
});
```


* Get stream info (equivalent to `--dump-json` of youtube-dl)
```java
VideoInfo streamInfo = YoutubeDL.getInstance().getInfo("https://vimeo.com/22439234");
System.out.println(streamInfo.getTitle());
```


* Get a single playable link containing video+audio
```java
YoutubeDLRequest request = new YoutubeDLRequest("https://youtu.be/Pv61yEcOqpw");
request.addOption("-f", "best");
VideoInfo streamInfo = YoutubeDL.getInstance().getInfo(request);
System.out.println(streamInfo.getUrl());
```

* youtube-dl supports myriad different options which be seen [here](https://github.com/rg3/youtube-dl)

* youtube-dl binary can be updated from within the library
```java
YoutubeDL.getInstance().updateYoutubeDL(getApplication());
```

## FFmpeg
If you wish to use ffmpeg features of youtube-dl (e.g. --extract-audio), include and initialize the ffmpeg library.
```java
try {
    YoutubeDL.getInstance().init(getApplication());
    FFmpeg.getInstance().init(getApplication());
} catch (YoutubeDLException e) {
    Log.e(TAG, "failed to initialize youtubedl-android", e);
}
```

## Docs
 *  Though not required for just using this library, documentation on building python for android can be seen [here](BUILD_PYTHON.md). Same for ffmpeg [here](BUILD_FFMPEG.md). Alternatively, you can use pre-built packages from [here (android5+)](http://termux.net/dists/stable/) or [here (android7+)](https://bintray.com/termux/termux-packages-24).
 * youtubedl-android uses lazy extractors based build of youtube-dl ([youtubedl-lazy](https://github.com/yausername/youtubedl-lazy/))
