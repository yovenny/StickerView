/**
 * Summary:水印分类
 */
package com.yovenny.stickview.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class WaterMarkCategory  implements Parcelable {
    private  int  id;//该字段为数据库映射的关键字，故赋值给cid
    private int cid;
    private int status;//1 新增，2删除
    private String name;
    public List<WaterMarkItem> waterMarkItems = new ArrayList<>(); //  频道标签列表

    public WaterMarkCategory() {

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cid);
        dest.writeInt(status);
        dest.writeString(name);
        dest.writeList(waterMarkItems);
    }

    //反序列化对象
    public static final Creator<WaterMarkCategory> CREATOR = new Creator() {

        @Override
        public WaterMarkCategory createFromParcel(Parcel source) {
            WaterMarkCategory waterMarkCategory = new WaterMarkCategory();
            waterMarkCategory.cid = source.readInt();
            waterMarkCategory.status = source.readInt();
            waterMarkCategory.name = source.readString();
            source.readList(waterMarkCategory.waterMarkItems, waterMarkCategory.getClass().getClassLoader());
            return waterMarkCategory;
        }

        @Override
        public WaterMarkCategory[] newArray(int size) {
            return new WaterMarkCategory[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        cid=this.id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public void setWaterMarkItems(List<WaterMarkItem> waterMarkItems) {
        this.waterMarkItems = waterMarkItems;
    }

}
