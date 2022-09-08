package com.xeasy.noticefix.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xeasy.noticefix.bean.AppIconConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xudaz
 */
@Deprecated
public class DBUtils extends SQLiteOpenHelper {
    private static final String DB_NAME = "NoticeFix";
    private static final int DB_VERSION = 1;

    private static DBUtils dbUtils;
    private SQLiteDatabase mReadDb;
    private SQLiteDatabase mWriteDb;
//    private SQLiteDatabase db;
    private final List<Class<?>> beanClassList;

    /**
     * @param context c
     * @param beanClassList 要初始化的bean的集合 在OnCreate自动创建
     */
    private DBUtils(Context context, List<Class<?>> beanClassList) {
        super(context, DB_NAME, null, DB_VERSION);
        this.beanClassList = beanClassList;
    }

    /**
     * 单例模式
     * @return this
     */
    public static DBUtils getInstance(Context context, List<Class<?>> beanClassList){
        if(dbUtils == null){
            dbUtils = new DBUtils(context, beanClassList);
            return dbUtils;
        }
        return dbUtils;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //    todo 反射创建表    beanClassList
    }

    /**
     * 关闭连接
     */
    private void closeLink() {
        if ( mReadDb != null && mReadDb.isOpen() ) {
            mReadDb.close();
        }
        if ( mWriteDb != null && mWriteDb.isOpen() ) {
            mWriteDb.close();
        }
    }
    /**
     * 获取读链接
     * @return db
     */
    private SQLiteDatabase getReadLink() {
        if ( mReadDb == null || ! mReadDb.isOpen() ) {
            mReadDb = dbUtils.getReadableDatabase();
        }
        return mReadDb;
    }
    /**
     * 获取写链接
     * @return db
     */
    private SQLiteDatabase getWriteLink() {
        if ( mWriteDb == null || ! mWriteDb.isOpen() ) {
            mWriteDb = dbUtils.getWritableDatabase();
        }
        return mWriteDb;
    }

    /**
     * 查询数据
     * 返回List
     */
    public ArrayList<AppIconConfig> selectAll() {
        ArrayList<AppIconConfig> list = new ArrayList<>();
        /*Cursor cursor = db.query(TABLE_APP_ICON_CONFIG,null,null,null,null,null,null);
        while (cursor.moveToNext()){
            AppIconConfig iconConfig = new AppIconConfig();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            int bsid = cursor.getInt(cursor.getColumnIndex("bsid"));
            iconConfig.setName(name);
            iconConfig.setBsid(bsid);
            iconConfig.setId(id);
            list.add(iconConfig);
            Log.e("--Main--", "==============selectis======"+id+"================"+name+"================"+bsid);
        }
        if(cursor != null){
            cursor.close();
        }*/

        return list;
    }
//    /**
//     * 根据ID删除数据
//     * id 删除id
//     */
//    public int delData(int id){
//        int inde = db.delete("t_person","id = ?",new String[]{String.valueOf(id)});
//        Log.e("--Main--", "==============删除了======================"+inde );
//        return inde;
//    }
//    /**
//     * 根据ID修改数据
//     * id 修改条码的id
//     * bsid 修改的ID
//     * name 修改的数据库
//     */
//    public int modifyData(int id,int bsid, String name){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("name",name);
//        contentValues.put("bsid",id);
//        int index = db.update("t_person",contentValues,"id = ?",new String[]{String.valueOf(id)});
//        Log.e("--Main--", "==============修改了======================"+index );
//        return index;
//    }
//    /**
//     * 添加数据
//     * bsid 添加的数据ID
//     * name 添加数据名称
//     */
//    public long insertData(int bsid, String name){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("name",name);
//        contentValues.put("bsid",bsid);
//        long dataSize = db.insert("t_person",null,contentValues);
//        Log.e("--Main--", "==============insertData======================"+name+"================"+bsid);
//        return dataSize;
//    }
//    /**
//     * 查询名字单个数据
//     * @param names
//     * @return
//     */
//    public boolean selectData(String names){
//        //查询数据库
//        Cursor cursor = db.query("t_person",null,"name = ?",new String[]{names},null,null,null);
//        while (cursor.moveToNext()){
//            return true;
//        }
//        return false;
//    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}