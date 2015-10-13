package com.yovenny.stickview.wedget.sticker;
/**
 * Summary: 水印标签自定义操作
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.yovenny.stickview.Constant;
import com.yovenny.stickview.R;
import com.yovenny.stickview.StickApp;
import com.yovenny.stickview.util.BitmapUtil;
import com.yovenny.stickview.util.Convert;
import com.yovenny.stickview.util.FileUtil;
import com.yovenny.stickview.util.Ln;
import com.yovenny.stickview.util.PhotoUtils;
import com.yovenny.stickview.util.TaskExecutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//bitmap recycle 设置新的sticker text 将bitmap cycle
public class StickerSeriesView extends ImageView {

    float x_down = 0;
    float y_down = 0;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int DELETE = 3;

    //绘制的固定参数
    public float OUTSIDE_RADIOU = Convert.dip2px(getContext(), 14);
    private float INSIDE_RADIOU = Convert.dip2px(getContext(), 9);
    private float STROKE_WIDTH = Convert.dip2px(getContext(), 2);
    private float LOCATION_PADDING = Convert.dip2px(getContext(), 15);

    private float MIN_STICK_SCALE_HEIGHT = OUTSIDE_RADIOU * 4;
    private float MIN_TEXT_SCALE_HEIGHT = OUTSIDE_RADIOU * 2;//3

    private float range = OUTSIDE_RADIOU;
    private int mode = NONE;
    private int mBgWidth;
    private int mBgHeight;
    //显示文字水印和图片水印的总开关
    private boolean isShowStick = true;
    private boolean isShowText = true;
    private boolean isShowAddress;

    public static final int STICK_TEXT_MAX_COUNT = 15;
    public static final int STICK_IMG_MAX_COUNT = 15;


    //联系添加水印的思路如下：调用方面将会有
    //1.封装每个水印的属性,要用的时候取出。List保存
    //2.对list进行排序，从上到下排列。
    //3.按照list的顺序进行排序。
    private List<Stick> mStickList = new ArrayList<>();


    //删除的listener
    private OnStickDelListener mOnStickDelListener;
    private OnStickTextDelListener mOnStickTextDelListener;

    public void delStick(int categoryId, int position) {
        for (int i = 0; i < mStickList.size(); i++) {
            Stick stick = mStickList.get(i);
            if (stick instanceof ImgStick) {
                if (((ImgStick) stick).categoryId == categoryId && ((ImgStick) stick).position == position) {
                    mStickList.remove(i);
                    i--;
                    break;
                }
            }

        }
        invalidate();
    }

    public interface OnStickDelListener {
        public void onStickDel(int categoryId, int postion);
    }

    public interface OnStickTextDelListener {
        public void onStickTextDel();
    }

    public void setOnStickDelListener(OnStickDelListener onStickDelListener) {
        mOnStickDelListener = onStickDelListener;
    }

    public void setOnStickTextDelListener(OnStickTextDelListener onStickTextDelListener) {
        mOnStickTextDelListener = onStickTextDelListener;
    }


    int widthScreen;
    int heightScreen;
    private float savex, savey, curx, cury;
    private Bitmap mLocBitmap;
    private float mLocTextSize = 12;
    private float mLocTextMargin = 8;
    private float mLocBitmapWidth = 9;

    // 距离差
    private float _scaleDelta;//Sticker 和text 的按下偏移

    //拉伸和放大的图标
//    private Bitmap mScaleBitmap;
//    private Bitmap mDelBitmap;
    private String mLocationStr = "";
    float mDensity = Convert.getScreenDensity(getContext());

    //实时的背景，用户旋转操作时需将其设进来
    private Bitmap mBgBitmap;

    // 生成图片leftTopMargin
    private float topMargin;
    private float leftMargin;
    private float scaleHeight;
    private float scaleWidth;

    private boolean isEverShowText;

    public boolean isEverShowText() {
        return isEverShowText;
    }

    public StickerSeriesView(Context context) {
        super(context);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthScreen = dm.widthPixels;
        heightScreen = dm.heightPixels;
        mLocBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_local_draw);
//        mScaleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_process_scale);
//        mDelBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_process_del);
        zoomDisplayBitmap();
        // Disable hardware acceleration to make sure bitmaps are drawn with anti-aliasing
        // See http://stackoverflow.com/questions/14378573/bitmap-not-drawn-anti-aliased/14443954
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public StickerSeriesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthScreen = dm.widthPixels;
        heightScreen = dm.heightPixels;
        mLocBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_local_draw);
//        mScaleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_process_scale);
//        mDelBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_process_del);
        zoomDisplayBitmap();
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void zoomDisplayBitmap() {
//        mDelBitmap = BitmapUtil.zoomBitmap(mDelBitmap, (int) range * 2, (int) range * 2);
//        mScaleBitmap = BitmapUtil.zoomBitmap(mScaleBitmap, (int) range * 2, (int) range * 2);
        float tempScale = (float) mLocBitmap.getWidth() / (mLocBitmapWidth * mDensity);
        float locScaleHeight = (float) mLocBitmap.getHeight() / tempScale;
        mLocBitmap = BitmapUtil.zoomBitmap(mLocBitmap, (int) (mLocBitmapWidth * mDensity), (int) locScaleHeight);
    }


    /**
     * 画边框
     */
    private void drawFrame(Canvas canvas, float[] fp, Paint paint) {

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
//      paint.setPathEffect(new DashPathEffect(new float[]{4, 2, 4, 2}, 0));
        canvas.drawLine(fp[0], fp[1], fp[2] + STROKE_WIDTH / 2, fp[3], paint);
        canvas.drawLine(fp[0], fp[1], fp[4], fp[5], paint);
        canvas.drawLine(fp[2], fp[3], fp[6], fp[7], paint);
        canvas.drawLine(fp[4] - STROKE_WIDTH / 2, fp[5], fp[6], fp[7], paint);
        paint.setStyle(Paint.Style.FILL);

        //绘制UI提供的操作图
//        canvas.drawBitmap(mDelBitmap, fp[0] - mDelBitmap.getWidth() / 2, fp[1] - mDelBitmap.getHeight() / 2, paint);
//        canvas.drawBitmap(mScaleBitmap, fp[6] - mScaleBitmap.getWidth() / 2, fp[7] - mScaleBitmap.getHeight() / 2, paint);

        // 创建绘制边框的边界值
        canvas.drawCircle(fp[2], fp[3], INSIDE_RADIOU, paint);
        canvas.drawCircle(fp[4], fp[5], INSIDE_RADIOU, paint);
        canvas.drawCircle(fp[0], fp[1], OUTSIDE_RADIOU, paint);
        canvas.drawCircle(fp[6], fp[7], INSIDE_RADIOU, paint);
        //绘制删除的黑线
        paint.setColor(Color.BLACK);
        float len = (float) (Math.sqrt(INSIDE_RADIOU * INSIDE_RADIOU / 2));
        canvas.drawLine(fp[0] - len, fp[1] - len, fp[0] + len, fp[1] + len, paint);
        canvas.drawLine(fp[0] + len, fp[1] - len, fp[0] - len, fp[1] + len, paint);
    }


    private void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        if (!TextUtils.isEmpty(mLocationStr)) {
            paint.setStrokeWidth(2 * mDensity); // 线宽
            paint.setTextSize(mLocTextSize * mDensity);
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
//            paint.setFakeBoldText(true);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.RIGHT);
            Rect rect = new Rect();
            paint.getTextBounds(mLocationStr, 0, mLocationStr.length(), rect);
            float w = rect.width();
//            int w = (int) paint.measureText(mLocationStr);
            Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            //根据实际显示的图片，绘制坐标文字
            float baseline = mBgHeight - topMargin - fontMetrics.bottom - LOCATION_PADDING;
            canvas.drawText(mLocationStr, mBgWidth - leftMargin - LOCATION_PADDING, baseline, paint);// 文字位置
            canvas.drawBitmap(mLocBitmap, mBgWidth - leftMargin - LOCATION_PADDING - w - mLocBitmap.getWidth() - mLocTextMargin * mDensity, baseline - mLocBitmap.getHeight(), paint);
        }
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        drawText(canvas);
        drawStickAndTexByHandle(canvas);
        canvas.restore();

    }

    private void drawStickAndTexByHandle(Canvas canvas) {
        Paint bitmapPaint = new Paint();//Paint.FILTER_BITMAP_FLAG//Paint.ANTI_ALIAS_FLAG
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);

        for (int i = mStickList.size() - 1; i >= 0; i--) {
            Stick stick = mStickList.get(i);
            if ((stick instanceof ImgStick && isShowStick) || (stick instanceof TextStick && isShowText)) {
                if (stick.isShowStick) {
                    canvas.drawBitmap(stick.stickBitmap, stick.matrix, bitmapPaint);
                    if (!stick.isHideFrame) {
                        drawFrame(canvas, stick.fp, paint);
                    }
                }
            }

        }
    }

    private void onStickHandleDownZoom(float x_down, float y_down) {
        if (mStickList.size() <= 0) {
            return;
        }
        Stick stick = mStickList.get(0);
        if (isDeleltePoint(x_down, y_down, stick.fp)) {
            if (!stick.isHideFrame) {
                mode = DELETE;
                stick.isHideFrame = false;
                mStickList.remove(stick);
                if (stick instanceof ImgStick) {
                    if (mOnStickDelListener != null) {
                        mOnStickDelListener.onStickDel(((ImgStick) stick).categoryId, ((ImgStick) stick).position);
                    }
                } else {
                    if (mOnStickTextDelListener != null) {
                        mOnStickTextDelListener.onStickTextDel();
                    }
                }
            }
        } else {
            midPoint(stick.mid, stick.sp[0], stick.sp[1], stick.sp[6], stick.sp[7]);
            stick.oldRotation = (float) getRorateDegrees(stick.mid.x, stick.mid.y, savex, savey, curx, cury);
            stick.oldDist = spacing(stick.mid.x, stick.mid.y, stick.sp[6], stick.sp[7]);
            float frameOldStickDist = spacing(stick.mid.x, stick.mid.y, stick.fp[6], stick.fp[7]);
            //计算偏移的stickScale
            _scaleDelta = spacing(stick.mid.x, stick.mid.y, curx, cury) - frameOldStickDist;
            //计算边框的最小缩放值
            if (stick instanceof ImgStick) {
                stick.minSacle = MIN_STICK_SCALE_HEIGHT / spacing(stick.fp[0], stick.fp[1], stick.fp[4], stick.fp[5]);
            } else {
                stick.minSacle = MIN_TEXT_SCALE_HEIGHT / spacing(stick.fp[0], stick.fp[1], stick.fp[4], stick.fp[5]);
            }
            //恢复初始
            if (spacing(stick.sp[0], stick.sp[1], stick.sp[4], stick.sp[5]) < (stick instanceof ImgStick ? MIN_STICK_SCALE_HEIGHT : MIN_TEXT_SCALE_HEIGHT)) {
                float frameDist = spacing(stick.mid.x, stick.mid.y, stick.fp[6], stick.fp[7]);
                float scale = frameDist / stick.oldDist;
                stick.matrix.postScale(scale, scale, stick.mid.x, stick.mid.y);// 縮放
                stick.oldDist = frameDist;
            }
        }

    }

    private void onStickHandleDownDrag() {
        if (mStickList.size() <= 0) {
            return;
        }
        Stick stick = mStickList.get(0);
        midPoint(stick.mid, stick.sp[0], stick.sp[1], stick.sp[6], stick.sp[7]);
    }


    //恢复外边框的状态
    private void resumeFrameStatus() {
        if (mStickList.size() <= 0) {
            return;
        }
        if (mode == DELETE) {
            invalidate();
            return;
        }
        Stick stick = mStickList.get(0);
        if (stick.isHideFrame) {
            stick.isHideFrame = false;
        }
        invalidate();
        stick.savedMatrix.set(stick.matrix);
        stick.frameSavedMatrix.set(stick.frameMatrix);
    }

    private void adjustStickList(Stick stick) {
        if (mStickList.contains(stick)) {
            mStickList.remove(stick);
            mStickList.add(0, stick);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        curx = event.getX();
        cury = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x_down = event.getX();
                y_down = event.getY();
                savex = curx;
                savey = cury;
                //根据坐标位置判断是否移动和缩放//判断是否为zoom or drag ,取zoom的优先级
                //枚举列表判断，判断列表的中哪个stick符合，当前的按下事件,优先判断zoom，再判断drag
                for (int i = 0; i < mStickList.size(); i++) {
                    Stick stick = mStickList.get(i);
                    if (stick.isShowStick) {
                        matrixCheck(stick.matrix, stick.sp, stick.stickBitmap);
                        matrixCheck(stick.frameMatrix, stick.fp, stick.stickBitmap);
                        if (isZoomPoint(x_down, y_down, stick.fp)) {
                            adjustStickList(stick);
                            mode = ZOOM;
                            onStickHandleDownZoom(x_down, y_down);
                            resumeFrameStatus();
                            break;
                        }
                    }
                }
                if (mode == NONE) {
                    for (int i = 0; i < mStickList.size(); i++) {
                        Stick stick = mStickList.get(i);
                        if (stick.isShowStick) {
//                            if (isDragPoint(x_down, y_down, stick.fp)) {
                            if (isDragPoint(x_down, y_down, stick.frameMatrix, stick.stickBitmap)) {
                                adjustStickList(stick);
                                mode = DRAG;
                                onStickHandleDownDrag();
                                resumeFrameStatus();
                                break;
                            }
                        }
                    }
                }
                //枚举列表对列表边框进行隐藏
                if (mode == NONE) {
//                    //需求1： 点击隐藏一个水印边框：
//                    for (int i = 0; i < mStickList.size(); i++) {
//                        Stick stick = mStickList.get(i);
//                        if (stick.isShowStick) {
//                            if (!stick.isHideFrame) {
//                                stick.isHideFrame = true;
////                                终止边框的隐藏事件
//                                invalidate();
//                                break;
//                            }
//                        }
//                    }

                    //需求2：点击隐藏所有水印边框?只要边框显示，state==hide，either：state==show
                    if (getShowFrameCount() > 0) {
                        setFrameStutas(true);
                    } else {
                        setFrameStutas(false);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    if (mStickList.size() <= 0) {
                        return true;
                    }
                    Stick stick = mStickList.get(0);
                    stick.transMatrix1.set(stick.savedMatrix);
                    float rotation = (float) (getRorateDegrees(stick.mid.x, stick.mid.y, savex, savey, curx, cury));
                    float displayRotation;
                    if (Float.compare(rotation, Float.NaN) == 0) {
                        displayRotation = stick.oldRotation;
                    } else {
                        if (Math.abs(rotation) < 1.5f) {//小于0.5度则不进行旋转
                            displayRotation = stick.oldRotation;
                        } else {
                            displayRotation = stick.oldRotation + rotation;
                        }
                        /**
                         * 遇到数据不准确时，记住打印数据，观察数据的正确性
                         */
                        stick.oldRotation = stick.oldRotation + rotation;
                    }
                    //避免直线滑动，造成图片翻转 ，没有生效
//                  displayRotation = displayRotation % 360;
                    float newDist = spacing(stick.mid.x, stick.mid.y, curx, cury) - _scaleDelta;
                    float scale = newDist / stick.oldDist;
                    /**
                     *  现需求不支持文字缩放，旋转,若支持，去掉此判断：代码冗余
                     */
//                    if (stick instanceof ImgStick) {
                    stick.transMatrix1.postRotate(displayRotation, stick.mid.x, stick.mid.y);
                    stick.transMatrix1.postScale(scale, scale, stick.mid.x, stick.mid.y);// 縮放
//                    }
                    matrixCheck(stick.transMatrix1, stick.sp, stick.stickBitmap);
                    stick.matrix.set(stick.transMatrix1);

                    stick.frameTransMatrix1.set(stick.frameSavedMatrix);
                    /**
                     *  现需求不支持文字缩放，旋转,若支持，去掉此判断：代码冗余
                     */
//                    if (stick instanceof ImgStick) {
                    stick.frameTransMatrix1.postRotate(displayRotation, stick.mid.x, stick.mid.y);
//                    }
                    if (spacing(stick.sp[0], stick.sp[1], stick.sp[4], stick.sp[5]) < (stick instanceof ImgStick ? MIN_STICK_SCALE_HEIGHT : MIN_TEXT_SCALE_HEIGHT)) {
                        /**
                         *  现需求不支持文字缩放，旋转,若支持，去掉此判断：代码冗余
                         */
//                        if (stick instanceof ImgStick) {
                        stick.frameTransMatrix1.postScale(stick.minSacle, stick.minSacle, stick.mid.x, stick.mid.y);// 縮放
//                        }
                        matrixCheck(stick.frameTransMatrix1, stick.fp, stick.stickBitmap);
                    } else {
                        /**
                         *  现需求不支持文字缩放，旋转,若支持，去掉此判断：代码冗余
                         */
//                        if (stick instanceof ImgStick) {
                        stick.frameTransMatrix1.postScale(scale, scale, stick.mid.x, stick.mid.y);// 縮放
//                        }
                        matrixCheck(stick.frameTransMatrix1, stick.fp, stick.stickBitmap);
                    }
                    stick.frameMatrix.set(stick.frameTransMatrix1);
                    invalidate();
                } else if (mode == DRAG) {
                    if (mStickList.size() <= 0) {
                        return true;
                    }
                    Stick stick = mStickList.get(0);
                    stick.transMatrix1.set(stick.savedMatrix);
                    stick.transMatrix1.postTranslate(event.getX() - x_down, event.getY() - y_down);// 平移
                    matrixCheck(stick.transMatrix1, stick.sp, stick.stickBitmap);
                    stick.matrix.set(stick.transMatrix1);
//边框
                    stick.frameTransMatrix1.set(stick.frameSavedMatrix);
                    stick.frameTransMatrix1.postTranslate(event.getX() - x_down, event.getY() - y_down);// 平移
                    matrixCheck(stick.frameTransMatrix1, stick.fp, stick.stickBitmap);
                    stick.frameMatrix.set(stick.frameTransMatrix1);
                    invalidate();
                }
                savex = curx;
                savey = cury;
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                _scaleDelta = 0;
                break;
        }
        return true;
    }


    private boolean isDeleltePoint(float x_down, float y_down, float[] sp) {
        if (x_down <= sp[0] + range && x_down >= sp[0] - range && y_down <= sp[1] + range && y_down >= sp[1] - range) {
            return true;
        }
        return false;
    }


    private boolean isZoomPoint(float x_down, float y_down, float[] p) {
        if ((x_down <= p[0] + range && x_down >= p[0] - range && y_down <= p[1] + range && y_down >= p[1] - range) ||
                (x_down <= p[6] + range && x_down >= p[6] - range && y_down <= p[7] + range && y_down >= p[7] - range) ||
                (x_down <= p[2] + range && x_down >= p[2] - range && y_down <= p[3] + range && y_down >= p[3] - range) ||
                (x_down <= p[4] + range && x_down >= p[4] - range && y_down <= p[5] + range && y_down >= p[5] - range)) {
            return true;
        }
        return false;
    }

    private boolean isDragPoint(float x_down, float y_down, float[] p) {
        //比较四个xy的最大值和最小值
        float[] xMM = getMaxAndMin(new float[]{p[0], p[2], p[4], p[6]});
        float[] yMM = getMaxAndMin(new float[]{p[1], p[3], p[5], p[7]});
        //不用加yMM[0] + range
        if (xMM[0] <= x_down && x_down <= xMM[1] && yMM[0] <= y_down && y_down <= yMM[1]) {
            return true;
        }
        return false;
    }

    //利用矩阵的翻转，进行对比初始值
    private boolean isDragPoint(float x_down, float y_down, Matrix matrix, Bitmap bitmap) {
        Matrix inMatrix = new Matrix();
        matrix.invert(inMatrix);
        float[] dst = new float[2];
        float[] p = new float[8];
        matrixCheck(inMatrix, p, bitmap);
        inMatrix.mapPoints(dst, new float[]{x_down, y_down});
        if (dst[0] > 0 && dst[0] < bitmap.getWidth() && dst[1] > 0 && dst[1] < bitmap.getHeight()) {
            return true;
        }
        return false;
    }


    private float[] getMaxAndMin(float[] data) {
        float min, max;
        min = max = data[0];
        for (int i = 0; i < data.length; i++) {
            if (data[i] > max) {
                max = data[i];
            }
            if (data[i] < min) {
                min = data[i];
            }
        }
        return new float[]{min, max};
    }


    private void matrixCheck(Matrix matrix, float[] ps, Bitmap bitmap) {
        float[] f = new float[9];
        matrix.getValues(f);
        ps[0] = f[Matrix.MSCALE_X] * 0 + f[Matrix.MSKEW_X] * 0
                + f[Matrix.MTRANS_X];
        ps[1] = f[Matrix.MSKEW_Y] * 0 + f[Matrix.MSCALE_Y] * 0
                + f[Matrix.MTRANS_Y];
        ps[2] = f[Matrix.MSCALE_X] * bitmap.getWidth() + f[Matrix.MSKEW_X] * 0
                + f[Matrix.MTRANS_X];
        ps[3] = f[Matrix.MSKEW_Y] * bitmap.getWidth() + f[Matrix.MSCALE_Y] * 0
                + f[Matrix.MTRANS_Y];
        ps[4] = f[Matrix.MSCALE_X] * 0 + f[Matrix.MSKEW_X]
                * bitmap.getHeight() + f[Matrix.MTRANS_X];
        ps[5] = f[Matrix.MSKEW_Y] * 0 + f[Matrix.MSCALE_Y]
                * bitmap.getHeight() + f[Matrix.MTRANS_Y];
        ps[6] = f[Matrix.MSCALE_X] * bitmap.getWidth() + f[Matrix.MSKEW_X] * bitmap.getHeight() + f[Matrix.MTRANS_X];
        ps[7] = f[Matrix.MSKEW_Y] * bitmap.getWidth() + f[Matrix.MSCALE_Y] * bitmap.getHeight() + f[Matrix.MTRANS_Y];
    }

    // 触碰两点间距离  
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    // 两点间距离
    private float spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return FloatMath.sqrt(x * x + y * y);
    }

    // 取手势中心点  
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    // 取手势中心点
    private void midPoint(PointF point, float x1, float y1, float x2, float y2) {
        float x = x1 + x2;
        float y = y1 + y2;
        point.set(x / 2, y / 2);
    }


    // 取旋转角度  
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    // 取旋转角度
    private float rotation(float x1, float y1, float x2, float y2) {
        double delta_x = x1 - x2;
        double delta_y = y1 - y2;
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 获取变换的角度
     *
     * @param centerX 中心x
     * @param centerY
     * @param saveX   上一次X
     * @param saveY
     * @param curX    现在X
     * @param curY
     * @return
     */
    private double getRorateDegrees(float centerX, float centerY, float saveX, float saveY, float curX, float curY) {
        double a = Math.sqrt((saveX - curX) * (saveX - curX) + (saveY - curY) * (saveY - curY));
        double b = Math.sqrt((centerX - curX) * (centerX - curX) + (centerY - curY) * (centerY - curY));
        double c = Math.sqrt((saveX - centerX) * (saveX - centerX) + (saveY - centerY) * (saveY - centerY));
        double cosA = (b * b + c * c - a * a) / (2 * b * c);
        double arcA = Math.acos(cosA);
        double degree = arcA * 180 / Math.PI;
        if (saveY < centerY && curY < centerY) {
            if (saveX < centerX && curX > centerX) {
                return degree;
            } else if (saveX >= centerX && curX <= centerX) {
                return -degree;
            }
        }
        if (saveY > centerY && curY > centerY) {
            if (saveX < centerX && curX > centerX) {
                return -degree;
            } else if (saveX > centerX && curX < centerX) {
                return degree;
            }
        }
        if (saveX < centerX && curX < centerX) {
            if (saveY < centerY && curY > centerY) {
                return -degree;
            } else if (saveY > centerY && curY < centerY) {
                return degree;
            }
        }
        if (saveX > centerX && curX > centerX) {
            if (saveY > centerY && curY < centerY) {
                return -degree;
            } else if (saveY < centerY && curY > centerY) {
                return degree;
            }
        }
        float tanB = (saveY - centerY) / (saveX - centerX);
        float tanC = (curY - centerY) / (curX - centerX);
        if ((saveX > centerX && saveY > centerY && curX > centerX && curY > centerY && tanB > tanC)
                || (saveX > centerX && saveY < centerY && curX > centerX && curY < centerY && tanB > tanC)
                || (saveX < centerX && saveY < centerY && curX < centerX && curY < centerY && tanB > tanC)
                || (saveX < centerX && saveY > centerY && curX < centerX && curY > centerY && tanB > tanC))
            return -degree;
        return degree;
    }


    private void getLTRB(Bitmap bitmap) {
        //算出左右上下的margin
        if (bitmap.getWidth() > bitmap.getHeight()) {
            float tempScale = (float) bitmap.getWidth() / (float) mBgWidth;
            scaleHeight = bitmap.getHeight() / tempScale;
            scaleWidth = mBgWidth;
            topMargin = (mBgHeight - scaleHeight) / 2;
            leftMargin = 0;
        } else {
            float tempScale = (float) bitmap.getHeight() / (float) mBgHeight;
            scaleWidth = bitmap.getWidth() / tempScale;
            scaleHeight = mBgHeight;
            leftMargin = (mBgWidth - scaleWidth) / 2;
            topMargin = 0;
        }
    }


    /**
     * TODO 这里不能根据屏幕的大小来生成图片，否则小屏幕生成的图片显示到大屏幕上可能会出现问题,
     * TODO WIDTH 应该为指定的一个值，而不是屏幕参数
     * TODO createBitamp 之后该bitamp不使用时，记得释放
     */
    public Bitmap creatFavouriteFixWithPhoto(Bitmap bgBitmap) {
        float fixWith = 1080L;//px
        Paint bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap bitmap;
        Bitmap saveBitmap;
        float screenScale;
        float transW;
        float transH;

        //算出左右上下的margin,按照屏幕宽度拉长
        if (bgBitmap.getWidth() > bgBitmap.getHeight()) {
            scaleWidth = mBgWidth;
            screenScale = fixWith / scaleWidth;

            float tempWidthScale = (float) bgBitmap.getWidth() / fixWith;
            scaleWidth = fixWith;
            scaleHeight = bgBitmap.getHeight() / tempWidthScale;

            transW = mBgWidth * screenScale - mBgWidth;
            transH = mBgHeight * screenScale - mBgHeight;

            mBgWidth *= screenScale;
            mBgHeight *= screenScale;

            topMargin = (mBgHeight - scaleHeight) / 2;
            leftMargin = 0;
        } else {
            float tempScale = (float) bgBitmap.getHeight() / (float) mBgHeight;
            scaleWidth = bgBitmap.getWidth() / tempScale;
            screenScale = fixWith / scaleWidth;

            float tempWidthScale = (float) bgBitmap.getWidth() / fixWith;
            scaleWidth = fixWith;
            scaleHeight = bgBitmap.getHeight() / tempWidthScale;

            transW = mBgWidth * screenScale - mBgWidth;
            transH = mBgHeight * screenScale - mBgHeight;

            mBgWidth *= screenScale;
            mBgHeight *= screenScale;

            leftMargin = (mBgWidth - scaleWidth) / 2;
            topMargin = 0;
        }

        //TODO 为什么要用初始值，来还原值，待查。
        for (Stick stick : mStickList) {
            stick.matrix.postTranslate(transW / 2, transH / 2);
            stick.matrix.postScale(screenScale, screenScale, stick.oldMid.x + transW / 2, stick.oldMid.y + transH / 2);// 縮放
        }
        mLocTextSize *= screenScale;
        LOCATION_PADDING *= screenScale;
        mLocTextMargin *= screenScale;
        mLocBitmapWidth *= screenScale;
        zoomDisplayBitmap();

        //当显示为竖图时，拉大宽度，其它参数也要相应的变化
        bitmap = Bitmap.createBitmap(mBgWidth, mBgHeight, Config.ARGB_8888); // 背景图片
        Canvas canvas = new Canvas(bitmap); //新建画布
        bgBitmap = BitmapUtil.zoomBitmap(bgBitmap, (int) scaleWidth, (int) scaleHeight, false);
        canvas.drawBitmap(bgBitmap, leftMargin, topMargin, bitmapPaint);
        try {
            //绘制的先后顺序
            for (int i = mStickList.size() - 1; i >= 0; i--) {
                Stick stick = mStickList.get(i);
                if ((stick instanceof ImgStick && isShowStick) || (stick instanceof TextStick && isShowText)) {
                    if (stick.isShowStick) {
                        canvas.drawBitmap(stick.stickBitmap, stick.matrix, bitmapPaint);
                    }
                }

            }
            drawText(canvas);
            //对图片进行裁剪：
            //topmargin+1px 是因为改变actionbar高度后，裁剪时出现黑线，如果要去掉加1px ,则回复actionbar的高度48dp
            saveBitmap = Bitmap.createBitmap(bitmap, (int) leftMargin, (int) topMargin + 1, (int) scaleWidth, (int) scaleHeight - 1);
            canvas.save(Canvas.ALL_SAVE_FLAG); // 保存画布
            canvas.restore();
        } finally {
            if (bgBitmap != null && !bgBitmap.isRecycled()) {
                bgBitmap.recycle();
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            for (Stick stick : mStickList) {
                if (stick.stickBitmap != null && !stick.stickBitmap.isRecycled()) {
                    stick.stickBitmap.recycle();
                    stick.stickBitmap = null;
                }
            }
            System.gc();
        }
        return saveBitmap;
    }

    public void saveBitmapTofile(Bitmap bg) {
        try {
            String savePath = Environment.getExternalStorageDirectory() + "/yovenny/";
            File saveFile = new File(savePath);
            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }
            Bitmap bitmap = creatFavouriteFixWithPhoto(bg);
            FileOutputStream fos = new FileOutputStream(new File(saveFile, "tem.png"));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBgWidthHeight(int bgWidth, int bgHeight) {
        mBgWidth = bgWidth;
        mBgHeight = bgHeight;
    }

    public void setStick(Stick stick) {
        mStickList.add(0, stick);
        //边框
        int originWidth = stick.stickBitmap.getWidth();
        int originHeight = stick.stickBitmap.getHeight();

        //算出初始位置
        stick.matrix.setTranslate((mBgWidth / 2 - originWidth / 2), (mBgHeight / 2 - originHeight / 2));
        if (stick instanceof TextStick) {
            isEverShowText = true;
//            stick.matrix.postScale(0.5f, 0.5f, (mBgWidth / 2), mBgHeight / 2);
        } else {
            //计算出要显示的scale；
            int widthPx = Convert.dip2px(getContext(), Constant.STICK_WIDTH);
            float scale = (float) widthPx / (float) originWidth;
            stick.matrix.postScale(scale, scale, (mBgWidth / 2), mBgHeight / 2);
        }
        matrixCheck(stick.matrix, stick.sp, stick.stickBitmap);
        //边框
        stick.frameMatrix.set(stick.matrix);
        matrixCheck(stick.frameMatrix, stick.fp, stick.stickBitmap);
        midPoint(stick.mid, stick.sp[0], stick.sp[1], stick.sp[6], stick.sp[7]);
        midPoint(stick.oldMid, stick.sp[0], stick.sp[1], stick.sp[6], stick.sp[7]);
        invalidate();
    }

    public void setmLocationStr(String locationStr) {
        mLocationStr = locationStr;
        if (TextUtils.isEmpty(mLocationStr)) {
            isShowAddress = false;
        } else {
            isShowAddress = true;
        }
        invalidate();
    }

    public boolean isShowAddress() {
        return isShowAddress;
    }

    public boolean isShowStick() {
        return isShowStick;
    }

    public boolean isShowText() {
        return isShowText;
    }

    public void setShowText(boolean isShowText) {
        this.isShowText = isShowText;
        invalidate();
    }

    public void setShowStick(boolean isShowStick) {
        this.isShowStick = isShowStick;
        invalidate();
    }

    public void setmBgBitmap(Bitmap bgBitmap) {
        this.mBgBitmap = bgBitmap;
        getLTRB(mBgBitmap);
        invalidate();
    }

    //获取文字水印的个数
    public int getTextCount() {
        int count = 0;
        for (Stick stick : mStickList) {
            if (stick instanceof TextStick) {
                count++;
            }
        }
        return count;
    }

    //获取图片水印的个数
    public int getStickCount() {
        int count = 0;
        for (Stick stick : mStickList) {
            if (stick instanceof ImgStick) {
                count++;
            }
        }
        return count;
    }

    //获取图片水印分类ids
    public ArrayList getStickCategoryIds() {
        ArrayList<Integer> categoryIds = new ArrayList<>();
        for (Stick stick : mStickList) {
            if (stick instanceof ImgStick) {
                categoryIds.add(((ImgStick) stick).categoryId);
            }
        }
        return categoryIds;
    }

    //获取图片水印的ids
    public ArrayList getStickIds() {
        ArrayList<Integer> watermarkIds = new ArrayList<>();
        for (Stick stick : mStickList) {
            if (stick instanceof ImgStick) {
                watermarkIds.add(((ImgStick) stick).watermarkId);
            }
        }
        return watermarkIds;
    }

    //获取文字水印的ids
    public ArrayList getTextContents() {
        ArrayList<String> contents = new ArrayList<>();
        for (Stick stick : mStickList) {
            if (stick instanceof TextStick) {
                contents.add(((TextStick) stick).content);
            }
        }
        return contents;
    }

    public void destory() {
        for (Stick stick : mStickList) {
            if (stick.stickBitmap != null && !stick.stickBitmap.isRecycled()) {
                stick.stickBitmap.recycle();
            }
        }
        mStickList = null;
        System.gc();
    }


    public int getShowFrameCount() {
        int showFrameCount = 0;
        for (Stick stick : mStickList) {
            if (stick.isShowStick) {
                if (!stick.isHideFrame) {
                    showFrameCount++;
                }
            }
        }
        return showFrameCount;
    }

    public void setFrameStutas(boolean isHideFrame) {
        for (Stick stick : mStickList) {
            stick.isHideFrame = isHideFrame;
        }
    }



    public void createFinalBitmap(final String originPath, final OnSaveResultListener listener) {
        TaskExecutor.executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    String savePath;
                    //2、压缩后的bitmap
                    //Bitmap   bitmap = BitmapUtil.getBitmap(mOriginalPhotoPath);
                    Bitmap bitmap = BitmapUtil.getThumbBitmap(new File(originPath));
                    //根据sticker可见判断状态,FIT_CENTER/fitCenter这里要对bitmap获取width and height
                    Bitmap favouriteBitmap = creatFavouriteFixWithPhoto(bitmap);
                    savePath = savePhoto(favouriteBitmap, originPath);
                    PhotoUtils.fileScan(StickApp.ins(), savePath);
                    if (favouriteBitmap != null && !favouriteBitmap.isRecycled()) {
                        favouriteBitmap.recycle();
                    }
                    System.gc();
                    listener.onSaveResult(savePath);
                } catch (Exception e) {
                    Log.w(Ln.LOG_TAG, e);
                    listener.onSaveResult("");
                }
            }
        });

    }

    private String savePhoto(Bitmap bitmap, String originPath) {
        File file = new File(originPath);
        File saveDir = new File(FileUtil.STORE_PATH + "merge");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        } else {
            if (!saveDir.isDirectory()) {
                FileUtil.deleteDirectory(saveDir.getAbsolutePath());
                saveDir.mkdirs();
            }
        }
        String name = file.getName().substring(0, file.getName().lastIndexOf('.')) + "_";
        int count = 0;
        String format = String.format("%%0%dd", 3);
        File saveFile;
        do {
            count++;
            String filename = name + String.format(format, count) + ".jpg";
            saveFile = new File(saveDir, filename);
        } while (saveFile.exists());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return saveFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.w(Ln.LOG_TAG, e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
        return "";
    }

    public interface OnSaveResultListener {
         void onSaveResult(String saveFile);
    }


}
