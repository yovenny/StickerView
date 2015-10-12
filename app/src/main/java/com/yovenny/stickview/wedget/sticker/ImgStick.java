package com.yovenny.stickview.wedget.sticker;

import android.graphics.Bitmap;

//
public class ImgStick extends Stick {
    public int categoryId=-1;
    public int position=-1;
    public int watermarkId=-1;

    public ImgStick(Bitmap stickBitmap,int categoryId,int watermarkId,int position) {
        super(stickBitmap);
        this.categoryId=categoryId;
        this.position=position;
        this.watermarkId=watermarkId;
    }
}