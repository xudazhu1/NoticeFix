package com.xeasy.noticefix.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Callable;

public class PermissionsUtil {



    public static void reqPermissions(Activity activity, String ... permissions) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
        }
    }

    public static <T> void reqPermission(Activity activity, String permission, Callable<T> callable) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            int requestCodeOut = (int) (Math.random() * 100);
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCodeOut);
            new ActivityCompat.OnRequestPermissionsResultCallback() {
                @Override
                public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                    if ( requestCode == requestCodeOut && grantResults[0]  == PackageManager.PERMISSION_GRANTED ) {
                        try {
                            callable.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            };


        } else {
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
