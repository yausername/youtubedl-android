package com.yausername.youtubedl_android.utils;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
            Reader in = new InputStreamReader(stream, "UTF-8");
            int nextChar;
            while ((nextChar = in.read()) != -1) {
                this.buffer.append((char) nextChar);
            }
        } catch (IOException e) {
            Logger.e(e, "failed to read stream");
        }
    }
}