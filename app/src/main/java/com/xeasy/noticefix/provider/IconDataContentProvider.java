package com.xeasy.noticefix.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.xeasy.noticefix.bean.CustomIconBean;
import com.xeasy.noticefix.bean.IconLibBean;
import com.xeasy.noticefix.dao.CustomIconDao;
import com.xeasy.noticefix.dao.GlobalConfigDao;
import com.xeasy.noticefix.dao.IconFuncDao;
import com.xeasy.noticefix.dao.IconLibDao;

import java.util.List;
import java.util.Map;

public class IconDataContentProvider extends ContentProvider {
    public IconDataContentProvider() {
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        // 实现它以在启动时初始化您的内容提供程序
        return false;
    }

    static Gson gson = new Gson();
    //  content://com.xeasy.noticefix.provider.IconDataContentProvider
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"globalConfig", "iconFunc", "libIconList", "customIconList"});
        GlobalConfigDao globalConfigDao = GlobalConfigDao.globalConfigDao;
        globalConfigDao.read = true;
        List<IconFuncDao.IconFuncStatus> iconFunc = IconFuncDao.getIconFunc(getContext());
        Map<String, IconLibBean> iconLib = IconLibDao.getIconLib(getContext(), false);
        Map<String, CustomIconBean> allCustomIcons = CustomIconDao.getAllCustomIcons(getContext());
        matrixCursor.addRow(new Object[]{
                gson.toJson(globalConfigDao),
                gson.toJson(iconFunc),
                gson.toJson(iconLib),
                gson.toJson(allCustomIcons)
        });
        return matrixCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}