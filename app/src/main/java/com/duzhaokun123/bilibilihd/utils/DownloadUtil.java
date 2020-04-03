package com.duzhaokun123.bilibilihd.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.duzhaokun123.bilibilihd.R;
import com.duzhaokun123.bilibilihd.services.VideoDownloadService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class DownloadUtil {
    public static void downloadPicture(Activity activity, String url) {
        switch (Settings.download.getDownloader()) {
            case Settings.Download.DOWNLOAD_MANAGER:
                DownloadManager.Request dmRequest = new DownloadManager.Request(Uri.parse(url))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setTitle(activity.getString(R.string.download))
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "bilibili HD" + File.separator + System.currentTimeMillis());
                try {
                    ((DownloadManager) Objects.requireNonNull(activity.getSystemService(Context.DOWNLOAD_SERVICE))).enqueue(dmRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                    activity.runOnUiThread(() -> ToastUtil.sendMsg(activity, R.string.saved));
                }
                break;
            case Settings.Download.GLIDE_CACHE_FIRST:
                new Thread() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            activity.runOnUiThread(() -> ToastUtil.sendMsg(activity, "Please use " + activity.getString(R.string.download_manager)));
                        } else {
                            FileInputStream fileInputStream = null;
                            FileOutputStream fileOutputStream = null;
                            try {
                                File srcFile = Glide.with(activity).asFile().load(url).submit().get();
                                fileInputStream = new FileInputStream(Objects.requireNonNull(srcFile));
                                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "bilibili HD");
                                if (!dir.exists() && !dir.mkdirs()) {
                                    return;
                                }
                                File file = new File(dir, String.valueOf(System.currentTimeMillis()));
                                fileOutputStream = new FileOutputStream(file);
                                FileUtil.copy(fileInputStream, fileOutputStream);
                                activity.runOnUiThread(() -> ToastUtil.sendMsg(activity, R.string.saved));
                            } catch (Exception e) {
                                e.printStackTrace();
                                activity.runOnUiThread(() -> ToastUtil.sendMsg(activity, e.getMessage()));
                            } finally {
                                try {
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (fileOutputStream != null) {
                                        fileOutputStream.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }.start();
                break;
        }

    }

    public static void downloadVideo(Context context, String video, String audio, String danmaku, String title, String bvid, boolean videoOnly) {
        VideoDownloadService.downloadVideo(context, video, audio, danmaku, context.getCacheDir().getPath() + File.separator + bvid + "_" + title, title, bvid, videoOnly);
    }
}
