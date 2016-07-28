package com.yovenny.stickview.db.dao;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yovenny.stickview.model.WaterMarkItem;

import java.util.ArrayList;
import java.util.List;


public class WaterItemDAO extends WaterBaseDAO {
    public static final String TABLE_NAME = "WaterMarkItem";
    public static final String KEY_ID = "id";
    public static final String KEY_IMAGEID = "imageId";
    public static final String KEY_WID = "wid";
    public static final String KEY_NAME = "name";
    public static final String KEY_STATUS = "status";
    public static final String KEY_URL = "url";
    public static final String KEY_CATEGORYID = "categoryId";

    public WaterItemDAO(Context context) {
        super(context);
    }

    public boolean find(int wid) {
        boolean result = false;
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + KEY_WID + " = ? ", new String[]{String.valueOf(wid)});
            if (cursor.moveToNext()) {
                result = true;
            }
            cursor.close();
            db.close();
        }
        return result;
    }

    public void save(WaterMarkItem item) {
        if (find(item.getWid())) {
            update(item);
            return;
        }
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            db.execSQL("insert into WaterMarkItem(wid,status,name,categoryId,url,imageId) values (?,?,?,?,?,?)",
                    new Object[]{String.valueOf(item.getWid()), String.valueOf(item.getStatus())
                            , item.getName(), String.valueOf(item.getCategoryId()), item.getUrl(), String.valueOf(item.getImageId())});
            db.close();
        }
    }

    public void delete(int wid) {
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            db.execSQL("delete from WaterMarkItem where wid =?", new Object[]{wid});
            db.close();
        }
    }

    public void deleteByCid(int categoryId) {
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
//            if (findByCid(categoryId).size() > 0) {
                db.execSQL("delete from WaterMarkItem where categoryId =?", new Object[]{categoryId});
//            }
            db.close();
        }
    }

    public void update(WaterMarkItem item) {
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            db.execSQL("update WaterMarkItem set imageId=?,name=?,status=?,url=?,categoryId=? where wid = ?",
                    new Object[]{item.getImageId(), item.getName(), item.getStatus(), item.getUrl(), item.getCategoryId(), item.getWid()});
            db.close();
        }
    }

    public List<WaterMarkItem> findValid(int cid) {
        List<WaterMarkItem> waterMarkItems = new ArrayList<>();
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from WaterMarkItem where status =? and categoryId = ? ORDER BY wid DESC", new String[]{String.valueOf(1), String.valueOf(cid)});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    WaterMarkItem item = new WaterMarkItem();
                    item.setImageId(cursor.getInt(cursor.getColumnIndex(KEY_IMAGEID)));
                    item.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    item.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_STATUS)));
                    item.setUrl(cursor.getString(cursor.getColumnIndex(KEY_URL)));
                    item.setCategoryId(cursor.getInt(cursor.getColumnIndex(KEY_CATEGORYID)));
                    item.setWid(cursor.getInt(cursor.getColumnIndex(KEY_WID)));
                    waterMarkItems.add(item);
                }
                cursor.close();
                db.close();
            }
        }
        return waterMarkItems;
    }


    public List<WaterMarkItem> findByCid(int cid) {
        List<WaterMarkItem> waterMarkItems = new ArrayList<>();
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from WaterMarkItem where categoryId = ?", new String[]{String.valueOf(cid)});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    WaterMarkItem item = new WaterMarkItem();
                    item.setImageId(cursor.getInt(cursor.getColumnIndex(KEY_IMAGEID)));
                    item.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    item.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_STATUS)));
                    item.setUrl(cursor.getString(cursor.getColumnIndex(KEY_URL)));
                    item.setCategoryId(cursor.getInt(cursor.getColumnIndex(KEY_CATEGORYID)));
                    item.setWid(cursor.getInt(cursor.getColumnIndex(KEY_WID)));
                    waterMarkItems.add(item);
                }
                cursor.close();
                db.close();
            }
        }
        return waterMarkItems;
    }

    public List<WaterMarkItem> findAll() {
        List<WaterMarkItem> waterMarkItems = new ArrayList<>();
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from WaterMarkItem ", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    WaterMarkItem item = new WaterMarkItem();
                    item.setImageId(cursor.getInt(cursor.getColumnIndex(KEY_IMAGEID)));
                    item.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    item.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_STATUS)));
                    item.setUrl(cursor.getString(cursor.getColumnIndex(KEY_URL)));
                    item.setCategoryId(cursor.getInt(cursor.getColumnIndex(KEY_CATEGORYID)));
                    item.setWid(cursor.getInt(cursor.getColumnIndex(KEY_WID)));
                    waterMarkItems.add(item);
                }
                cursor.close();
                db.close();
            }
        }
        return waterMarkItems;
    }

}
