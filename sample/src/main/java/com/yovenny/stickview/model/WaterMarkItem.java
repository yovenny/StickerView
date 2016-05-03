/**
 * Summary:水印单项
 */
package com.yovenny.stickview.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.yovenny.stickview.util.FileUtil;

import java.io.File;


public class WaterMarkItem  implements Parcelable {
    private int id;//该字段为数据库映射的关键字，故赋值给wid
    private int wid;
    private int status;//1 新增，2删除
    private int categoryId;
    private String url;
    private String name;
    private int imageId;


    public WaterMarkItem() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(wid);
        dest.writeInt(status);
        dest.writeInt(categoryId);
        dest.writeString(url);
        dest.writeString(name);
        dest.writeInt(imageId);

    }

    //反序列化对象
    public static final Creator<WaterMarkItem> CREATOR = new Creator() {

        @Override
        public WaterMarkItem createFromParcel(Parcel source) {
            WaterMarkItem waterMarkItem = new WaterMarkItem();
            waterMarkItem.wid = source.readInt();
            waterMarkItem.status = source.readInt();
            waterMarkItem.categoryId = source.readInt();
            waterMarkItem.url = source.readString();
            waterMarkItem.name = source.readString();
            waterMarkItem.imageId = source.readInt();
            return waterMarkItem;
        }

        @Override
        public WaterMarkItem[] newArray(int size) {
            return new WaterMarkItem[size];
        }
    };


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWid() {
        return wid;
    }

    public void setWid(int wid) {
        this.wid = wid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        wid = this.id;
    }

    public String getSavePath() {
        return FileUtil.STORE_PATH + "water/"+ File.separator + categoryId +  File.separator+ imageId;
    }

    public String getCategoryPath() {
        return FileUtil.STORE_PATH +"water/"  + categoryId + File.separator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
