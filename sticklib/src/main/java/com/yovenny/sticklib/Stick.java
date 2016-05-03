package com.yovenny.sticklib;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;

//标签基类
public class Stick {
    public boolean isShowStick=true;
    public boolean isHideFrame;
    public float minSacle;

    public PointF mid = new PointF();
    public PointF oldMid = new PointF();

    //stickerBitmap 四个点的位置
    public float[] sp = new float[8];
    public float[] fp = new float[8];

    public float oldDist = 1f;
    public float oldRotation = 0;

    public Matrix matrix = new Matrix();
    public Matrix transMatrix1 = new Matrix();
    public Matrix savedMatrix = new Matrix();

    //stickbitmap Frame的矩阵
    public Matrix frameMatrix = new Matrix();
    public Matrix frameTransMatrix1 = new Matrix();
    public Matrix frameSavedMatrix = new Matrix();

    public Bitmap stickBitmap;

    public Stick(Bitmap stickBitmap){
        this.stickBitmap=stickBitmap;
    }
}