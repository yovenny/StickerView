package com.yovenny.stickview.db.dao;


import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import com.yovenny.stickview.Constant;
import com.yovenny.stickview.util.AssetsUtil;
import com.yovenny.stickview.util.Ln;

import java.io.File;



public class WaterBaseDAO {
    protected Context mContext;

    public WaterBaseDAO(Context context) {
        this.mContext = context;
    }

    protected synchronized SQLiteDatabase getWaterMarkDb() {
        String dbPath = getDatabasePath(Constant.WATER_SAVE_DB).getAbsolutePath();
        if (!copyDBIfNecessary(dbPath)) {
            return null;
        }
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
    private File getDatabasePath(String name) {
        String dbRoot = "/data/data/" + mContext.getPackageName();
        File dbFile = makeFilename(new File(dbRoot, "databases"), name);
        if (!dbFile.getParentFile().exists() && dbFile.getParentFile().mkdir()) ;
        return dbFile;
    }

    private File makeFilename(File base, String name) {
        if (name.indexOf(File.separatorChar) < 0) {
            return new File(base, name);
        }
        throw new IllegalArgumentException("File " + name + " contains a path separator");
    }


    private boolean copyDBIfNecessary(String newPath) {
        return new File(newPath).exists()|| copyDBFromAssets(newPath);
    }

    private boolean copyDBFromAssets(String newPath) {
        AssetManager am = mContext.getAssets();
        String dbSuff = ".db";
        String dbName = null;
        try {
            String[] dbNames = am.list("");
            for (String currName : dbNames) {
                if (currName.endsWith(dbSuff)) {
                    dbName = currName;
                }
            }
//            if (dbName == null || (underIdx = dbName.lastIndexOf(95)) < 0) {  // 名称不符合gwiki_1324242424411.db类似规范
//                Ln.e("assets database(" + dbName + ") format error");
//                return false;
//            }
            AssetsUtil.copyAssetFile(am, dbName, newPath);
            Ln.d("copy database[" + dbName + "] from assets");
        } catch (Exception ie) {
            Ln.e("copy wiki database error:" + ie.getMessage());
            return false;
        }
        return true;
    }
}
