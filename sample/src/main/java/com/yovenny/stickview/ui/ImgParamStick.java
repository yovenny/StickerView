package com.yovenny.stickview.ui;

import android.graphics.Bitmap;

import com.yovenny.sticklib.ImgStick;
import com.yovenny.sticklib.Stick;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skyArraon on 16/5/3.
 * TODO 考虑到控件自身不与业务逻辑相关联,so 如需添加额外参数请继承.回调强转获取该参数
 */
public class ImgParamStick extends ImgStick {
    public int categoryId = -1;
    public int position = -1;
    public int watermarkId = -1;


    public ImgParamStick(Bitmap stickBitmap, int categoryId, int watermarkId, int position) {
        super(stickBitmap);
        this.categoryId = categoryId;
        this.position = position;
        this.watermarkId = watermarkId;
    }

    public static ArrayList<Integer> getStickCategoryIds(List<Stick> stickList){
        ArrayList<Integer> categoryIds=new ArrayList<>();
        for (Stick stick :stickList){
            if(stick instanceof  ImgParamStick){
                categoryIds.add(((ImgParamStick) stick).categoryId);
            }
        }
        return  categoryIds;
    }

    public static ArrayList<Integer> getStickIds(List<Stick> stickList){
        ArrayList<Integer> ids=new ArrayList<>();
        for (Stick stick :stickList){
            if(stick instanceof  ImgParamStick){
                ids.add(((ImgParamStick) stick).watermarkId);
            }
        }
       return ids;
    }
}
