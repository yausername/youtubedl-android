package com.yausername.youtubedl_android_example;

import android.app.Activity;
import android.app.AlertDialog;

import com.yausername.youtubedl_android.YoutubeDL;

/** Keeps the sample's update-channel dialog out of MainActivity's click handler. */
final class AlertDialogHelper {
    interface UpdateListener {
        void onSelected(YoutubeDL.UpdateChannel channel);
    }

    private AlertDialogHelper() { }

    static void showUpdateDialog(Activity activity, UpdateListener listener) {
        new AlertDialog.Builder(activity)
                .setTitle("Update Channel")
                .setItems(new String[]{"Stable Releases", "Nightly Releases", "Master Releases"},
                        (dialog, which) -> {
                            if (which == 0) listener.onSelected(YoutubeDL.UpdateChannel._STABLE);
                            else if (which == 1) listener.onSelected(YoutubeDL.UpdateChannel._NIGHTLY);
                            else listener.onSelected(YoutubeDL.UpdateChannel._MASTER);
                        })
                .show();
    }
}
