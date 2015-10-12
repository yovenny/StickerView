/**
 * Summary: 数据库对象操作入口
 * Version 1.0
 * Author: zhaomi@jugame.com.cn
 * Company: www.mjgame.cn
 * Date: 13-11-14
 * Time: 下午3:33
 * Copyright: Copyright (c) 2013
 */

package com.yovenny.stickview.db;

import android.content.Context;

import com.yovenny.stickview.db.dao.WaterCategoryDAO;
import com.yovenny.stickview.db.dao.WaterItemDAO;

public class DBManager {
    private WaterCategoryDAO mWaterCategoryDAO;
    private WaterItemDAO mWaterItemDAO;

    public DBManager(Context context) {
        mWaterCategoryDAO = new WaterCategoryDAO(context);
        mWaterItemDAO = new WaterItemDAO(context);
    }

    public synchronized WaterCategoryDAO getWaterCategoryDAO() {
        synchronized (this) {
            return mWaterCategoryDAO;
        }
    }

    public synchronized WaterItemDAO getWaterItemDAO() {
        synchronized (this) {
            return mWaterItemDAO;
        }
    }

}