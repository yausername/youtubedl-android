# youtubedl-android
Android library wrapper for [yt-dlp](https://github.com/yt-dlp/yt-dlp) (formerly [youtube-dl](https://github.com/rg3/youtube-dl)) executable

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.junkfood02.youtubedl-android/library)


## Credits
*  [youtubedl-java](https://github.com/sapher/youtubedl-java) by [sapher](https://github.com/sapher), youtubedl-android adds android compatibility to youtubedl-java.

<br/>

## Sample app
Debug apk for testing can be downloaded from the [releases page](https://github.com/yausername/youtubedl-android/releases)
<br/>
<br/>
![Download Example](https://media.giphy.com/media/fvI9yytF4rxmH7pGHu/giphy.gif)
![Streaming Example](https://media.giphy.com/media/UoqecxgY9IWbUs5tSR/giphy.gif)



If you wish to use config file in the download option by using this command `--config-location` you must create a file named `config.txt` inside `youtubedl-android` directory and add the commands for example.

```
--no-mtime

-o /sdcard/Download/youtubedl-android/%(title)s.%(ext)s

-f "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"
```


<br/>

Checkout [dvd](https://github.com/yausername/dvd), a video downloader app based on this library.

Also take a look at [Seal](https://github.com/JunkFood02/Seal), another video/audio downloader app which demonstrates a more advanced and customized use of this library.

## Installation

### Gradle

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.junkfood02.youtubedl-android:library:0.16.0")
    implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.16.0")
    implementation("io.github.junkfood02.youtubedl-android:aria2c:0.16.0") // optional
}
```

* Set `android:extractNativeLibs="true"` in your app's manifest.
* Use `abiFilters 'x86', 'x86_64', 'armeabi-v7a', 'arm64-v8a'` in app/build.gradle, see [sample app](https://github.com/yausername/youtubedl-android/blob/master/app/build.gradle).
* Use abi splits to reduce apk size, see [sample app](https://github.com/yausername/youtubedl-android/blob/master/app/build.gradle).
* On Android 10 (API 29), set `android:requestLegacyExternalStorage="true"`.
* On Android 10+ (API 30 or higher), due to Android's Scoped Storage changes, apps only have the direct access to  `Download/` and `Documents/` . And you can only download the videos into these two directories, see [related issue](https://github.com/yausername/youtubedl-android/issues/174).

## Usage

* yt-dlp executable and python 3.8 are bundled in the library.
* Initialize library, preferably in `onCreate`.

```java
try {
    YoutubeDL.getInstance().init(this);
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

* Stopping a previously started download process
```java
    YoutubeDLRequest request = new YoutubeDLRequest("https://vimeo.com/22439234");
    final String processId = "MyProcessDownloadId";
    YoutubeDL.getInstance().execute(request, (progress, etaInSeconds) -> {
    System.out.println(String.valueOf(progress) + "% (ETA " + String.valueOf(etaInSeconds) + " seconds)");
    }, processId);
    ...
    YoutubeDL.getInstance().destroyProcessById(processId);
```


* Get stream info (equivalent to `--dump-json` of yt-dlp)
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

* yt-dlp supports myriad different options which be seen [here](https://github.com/yt-dlp/yt-dlp)

* yt-dlp binary can be updated from within the library (A example can be found in the [sample app](app/src/main/java/com/yausername/youtubedl_android_example/MainActivity.java))
```java
    YoutubeDL.getInstance().updateYoutubeDL(this, updateChannel); // UpdateChannel.NIGHTLY or UpdateChannel.STABLE
```

## FFmpeg
If you wish to use ffmpeg features of yt-dlp (e.g. --extract-audio), include and initialize the ffmpeg library.
```java
try {
    YoutubeDL.getInstance().init(this);
    FFmpeg.getInstance().init(this);
} catch (YoutubeDLException e) {
    Log.e(TAG, "failed to initialize youtubedl-android", e);
}
```

## Aria2c

This library can make use of aria2c as the external downloader. include and initialize the `aria2c` library.
```java
try {
    YoutubeDL.getInstance().init(this);
    FFmpeg.getInstance().init(this);
    Aria2c.getInstance().init(this);
} catch (YoutubeDLException e) {
    Log.e(TAG, "failed to initialize youtubedl-android", e);
}
```
and options for the request as below:
```kotlin
request.addOption("--downloader", "libaria2c.so");
```

## Docs

*  Though not required for just using this library, documentation on building python for android can be seen [here](BUILD_PYTHON.md). Same for ffmpeg [here](BUILD_FFMPEG.md). Alternatively, you can use pre-built packages from [here (android5+)](https://packages.termux.dev/apt/termux-main-21/pool/main/) or [here (android7+)](https://packages.termux.dev/apt/termux-main/pool/main/).
* youtubedl-android uses lazy extractors based build of yt-dlp - [ytdlp-lazy](https://github.com/xibr/ytdlp-lazy) (formerly [youtubedl-lazy](https://github.com/yausername/youtubedl-lazy/))
* To build `aria2` you need `libc++, c-ares, openssl, libxml2, zlib, ibiconv` it can be found in [here (android5+)](https://packages.termux.dev/apt/termux-main-21/pool/main/) or [here (android7+)](https://packages.termux.dev/apt/termux-main/pool/main/). then follow the method used to build [python](BUILD_PYTHON.md) or [ffmpeg](BUILD_FFMPEG.md).

## Donate
You can support the project by donating to below addresses.
| Type  | Address |
| ------------- | ------------- |
| <img src="https://en.bitcoin.it/w/images/en/2/29/BC_Logo_.png" alt="Bitcoin" width="50"/>  | bc1qw3g7grh6dxk69mzwjmewanj9gj2ycc5mju5dc4  |
| <img src="https://www.getmonero.org/press-kit/symbols/monero-symbol-480.png" alt="Monero" width="50"/>  | 49SQgJTxoifhRB1vZGzKwUXUUNPMsrsxEacZ8bRs5tqeFgxFUHyDFBiUYh3UBRLAq355tc2694gbX9LNT7Ho7Vch2XEP4n4  |
