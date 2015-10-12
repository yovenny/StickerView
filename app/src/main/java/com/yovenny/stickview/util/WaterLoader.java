package com.yovenny.stickview.util;

import com.yovenny.stickview.StickApp;
import com.yovenny.stickview.db.DBManager;
import com.yovenny.stickview.interf.OnSimpleProgressUpdateListener;
import com.yovenny.stickview.model.WaterMarkCategory;
import com.yovenny.stickview.model.WaterMarkItem;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Summary: 水印数据操作类
 **/
public class WaterLoader {
    static WaterLoader gInstance;
    private final Object gLock = new Object();
    private int hasDownloadCount;
    public static boolean isLoadingWater = false;
    private DBManager mDbManager;


    public static WaterLoader ins() {
        if (gInstance == null) {
            gInstance = new WaterLoader();
        }
        return gInstance;
    }

    private WaterLoader() {
        mDbManager = new DBManager(StickApp.ins());
    }

    /**
     * 后台获取最新的水印数据，与本地数据库同步
     * @param waterMarkCategoryList 后台最新的category
     * @param watermarkList 后台最新的watermark
     * @param modifyTime 本次更新时间，获取时提交
     */
    public synchronized void syncWaterMark(final List<WaterMarkCategory> waterMarkCategoryList, final List<WaterMarkItem> watermarkList, final long modifyTime) {
        synchronized (gLock) {
            final Map<File, String> downImgs = new HashMap<>();
            final File watermarkFile = new File(FileUtil.STORE_PATH, "watermark");
            if (!watermarkFile.exists()) {
                watermarkFile.mkdirs();
            }

            if (waterMarkCategoryList != null) {
                //TODO 保持数据库一致  新增category
                for (WaterMarkCategory waterMarkCategory : waterMarkCategoryList) {
                    File watermarkCategoryFile = new File(watermarkFile, waterMarkCategory.getCid() + "");
                    if (waterMarkCategory.getStatus() == 1) {
                        if (!watermarkCategoryFile.exists()) {
                            watermarkCategoryFile.mkdirs();
                        }
                    }
                }
            }

            if (watermarkList != null) {
                //TODO 保持数据库一致 新增waterMarkItem
                for (final WaterMarkItem waterMarkItem : watermarkList) {
                    File watermarkCategoryFile = new File(watermarkFile, waterMarkItem.getCategoryId() + "");
                    String fileName = waterMarkItem.getImageId() + "";
                    final File watermarkItemFile = new File(watermarkCategoryFile, fileName);
                    if (waterMarkItem.getStatus() == 1) {
                        if (!watermarkItemFile.exists()) {
                            downImgs.put(watermarkItemFile, waterMarkItem.getUrl());
                        }
                    }
                }
            }

            if (downImgs.size() > 0) {
                hasDownloadCount = 0;
                for (Map.Entry<File, String> entry : downImgs.entrySet()) {
                    final String url = entry.getValue();
                    final File downFile = entry.getKey();
                    TaskExecutor.executeTask(new Runnable() {
                        @Override
                        public void run() {
                            NetService.downloadFile(url, downFile, 3, new OnSimpleProgressUpdateListener(){
                                @Override
                                public void onError(Exception e) {
                                    //删除未下载完全的文件
                                    if (downFile.exists()) {
                                        FileUtil.deleteFile(downFile.getAbsolutePath());
                                    }
                                    isLoadingWater = false;
                                    super.onError(e);
                                }
                            });
                            if (hasDownloadCount == downImgs.size()) {
                                for (WaterMarkCategory category : waterMarkCategoryList) {
                                    if (category.getStatus() == 1) {
                                        mDbManager.getWaterCategoryDAO().save(category);
                                    }
                                }
                                for (WaterMarkItem item : watermarkList) {
                                    if (item.getStatus() == 1) {
                                        mDbManager.getWaterItemDAO().save(item);
                                    }
                                }
                                onStatusCategoryDel(waterMarkCategoryList, watermarkFile);
                                onStatusWaterItemDel(watermarkList, watermarkFile);
                                PreferenceHelper.writeLong(StickApp.ins(),StickApp.STICK_PREF,"lastUpdateWaterTime",modifyTime);
                                isLoadingWater = false;

                            }
                        }
                    });
                }
            } else {
                for (WaterMarkCategory category : waterMarkCategoryList) {
                    if (category.getStatus() == 1) {
                        mDbManager.getWaterCategoryDAO().save(category);
                    }
                }
                for (WaterMarkItem item : watermarkList) {
                    if (item.getStatus() == 1) {
                        mDbManager.getWaterItemDAO().save(item);
                    }
                }
                onStatusCategoryDel(waterMarkCategoryList, watermarkFile);
                onStatusWaterItemDel(watermarkList, watermarkFile);
                PreferenceHelper.writeLong(StickApp.ins(), StickApp.STICK_PREF, "lastUpdateWaterTime", modifyTime);
                isLoadingWater = false;
            }
        }
    }

    //删除waterMarkItem
    private void onStatusWaterItemDel(List<WaterMarkItem> watermarkList, File watermarkFile) {
        for (final WaterMarkItem waterMarkItem : watermarkList) {
            File watermarkCategoryFile = new File(watermarkFile, waterMarkItem.getCategoryId() + "");
            String fileName = waterMarkItem.getImageId() + "";
            final File watermarkItemFile = new File(watermarkCategoryFile, fileName);
            if (waterMarkItem.getStatus() == 2) {
                if (watermarkItemFile.exists()) {
                    FileUtil.deleteFile(watermarkItemFile.getAbsolutePath());
                }
                mDbManager.getWaterItemDAO().delete(waterMarkItem.getWid());
            }
        }
    }

    //删除category
    private void onStatusCategoryDel(List<WaterMarkCategory> waterMarkCategoryList, File watermarkFile) {
        for (WaterMarkCategory waterMarkCategory : waterMarkCategoryList) {
            File watermarkCategoryFile = new File(watermarkFile, waterMarkCategory.getCid() + "");
            if (waterMarkCategory.getStatus() == 2) {
                if (watermarkCategoryFile.exists()) {
                    FileUtil.deleteDirectory(watermarkCategoryFile.getAbsolutePath());
                }
                mDbManager.getWaterCategoryDAO().delete(waterMarkCategory.getCid());
                mDbManager.getWaterItemDAO().deleteByCid(waterMarkCategory.getCid());
            }
        }
    }
}
