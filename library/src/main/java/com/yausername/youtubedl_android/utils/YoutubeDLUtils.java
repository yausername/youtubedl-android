package com.yausername.youtubedl_android.utils;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class YoutubeDLUtils {

    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        unzip(new FileInputStream(zipFile), targetDirectory);
    }

    public static void unzip(InputStream inputStream, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(inputStream));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            }
        } finally {
            zis.close();
        }
    }

    public static void deleteIfExists(File file) throws FileNotFoundException {
        if (file.isDirectory()) {
            for (File c : file.listFiles())
                deleteIfExists(c);
        }
        if (!file.delete())
            throw new FileNotFoundException("Failed to delete file: " + file);
    }

    public static boolean delete(File file){
        try {
            deleteIfExists(file);
        } catch (FileNotFoundException e) {
            Logger.e(e, "unable to delete file");
            return false;
        }
        return true;
    }
}
