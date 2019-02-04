package com.yausername.youtubedl_android.utils;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;

public class StreamGobbler extends Thread {

    private InputStream stream;
    private StringBuffer buffer;

    public StreamGobbler(StringBuffer buffer, InputStream stream) {
        this.stream = stream;
        this.buffer = buffer;
        start();
    }

    public void run() {
        try {
            int nextChar;
            while ((nextChar = this.stream.read()) != -1) {
                this.buffer.append((char) nextChar);
            }
        } catch (IOException e) {
            Logger.e(e, "failed to read stream");
        }
    }
}