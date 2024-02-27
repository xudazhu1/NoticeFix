package com.xeasy.noticefix.utils;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class RootConnection implements ServiceConnection {
    public IBinder myBinder;
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        myBinder = service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
