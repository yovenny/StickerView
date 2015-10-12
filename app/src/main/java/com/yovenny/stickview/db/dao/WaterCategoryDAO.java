package com.yovenny.stickview.db.dao;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yovenny.stickview.model.WaterMarkCategory;

import java.util.ArrayList;
import java.util.List;


public class WaterCategoryDAO extends WaterBaseDAO {

    public static final String TABLE_NAME = "WaterMarkCategory";
    public static final String KEY_ID = "id";
    public static final String KEY_CID = "cid";
    public static final String KEY_STATUS = "status";
    public static final String KEY_NAME = "name";

    public WaterCategoryDAO(Context context) {
        super(context);
    }

    public boolean find(int cid) {
        boolean result = false;
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + KEY_CID + " = ? ", new String[]{String.valueOf(cid)});
            if (cursor.moveToNext()) {
                result = true;
            }
            cursor.close();
            db.close();
        }
        return result;
    }

    public void save(WaterMarkCategory category) {
        if (find(category.getCid())) {
            update(category);
           return;
        }
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            db.execSQL("insert into WaterMarkCategory(cid,status,name) values (?,?,?)",
                    new Object[]{String.valueOf(category.getCid()),String.valueOf(category.getStatus()),category.getName()});
            db.close();
        }
    }

    public void delete(int cid) {
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
//            if(find(cid)){
                db.execSQL("delete from WaterMarkCategory where cid =?",new Object[]{cid});
//            }
            db.close();
        }

    }

    public void update(WaterMarkCategory category) {
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            db.execSQL("update WaterMarkCategory set status=?,name=? where cid = ?", new Object[]{category.getStatus(), category.getName(),category.getCid()});
            db.close();
        }
    }

    public List<WaterMarkCategory> findValid() {
        List<WaterMarkCategory> waterCategories = new ArrayList<>();
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from WaterMarkCategory where status =?", new String[]{String.valueOf(1)});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    WaterMarkCategory category = new WaterMarkCategory();
                    category.setCid(cursor.getInt(cursor.getColumnIndex(KEY_CID)));
                    category.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    category.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_STATUS)));
                    waterCategories.add(category);
                }
                cursor.close();
                db.close();
            }
        }
        return waterCategories;
    }

    public List<WaterMarkCategory> findAll() {
        List<WaterMarkCategory> waterCategories = new ArrayList<>();
        SQLiteDatabase db = getWaterMarkDb();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from WaterMarkCategory", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    WaterMarkCategory category = new WaterMarkCategory();
                    category.setCid(cursor.getInt(cursor.getColumnIndex(KEY_CID)));
                    category.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    category.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_STATUS)));
                    waterCategories.add(category);
                }
                cursor.close();
                db.close();
            }
        }
        return waterCategories;
    }

}
