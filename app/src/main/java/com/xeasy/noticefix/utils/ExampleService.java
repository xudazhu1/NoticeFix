package com.xeasy.noticefix.utils;

import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ipc.RootService;

// Create the file system service in the root process
// For example, create and send the service back to the client in a RootService
public class ExampleService extends RootService {

    private Shell shell;

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建一个Shell对象
        shell = Shell.getShell();
    }
}
