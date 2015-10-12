package com.yovenny.stickview.wedget.sticker;
/**
 * Summary: 水印标签自定义操作
 * Version 0.2.0
 * Author: chenbc@jugame.com.cn
 * Company: www.mjgame.cn
 * Date: 15-5-11
 * Time: 上午11:14
 * Copyright: Copyright (c) 2014
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.yovenny.stickview.Constant;
import com.yovenny.stickview.R;
import com.yovenny.stickview.util.BitmapUtil;
import com.yovenny.stickview.util.Convert;
import com.yovenny.stickview.util.Ln;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



//bitmap recycle 设置新的sticker text 将bitmap cycle
@Deprecated
public class StickerView extends ImageView {
    float x_down = 0;
    float y_down = 0;

    //stickbitmap 的rotate scale参数
    PointF mStickMid = new PointF();
    PointF mOldStickMid=new PointF();
    float mOldStickDist = 1f;
    float mOldStickRotation = 0;

    //textbitmap 的rotate scale参数
    PointF mTextMid = new PointF();
    PointF mOldTextMid=new PointF();
    float mOldTextDist = 1f;
    float mOldTextRotation = 0;

    //stickbitmap 的矩阵
    Matrix mStickMatrix = new Matrix();
    Matrix mStickTransMatrix1 = new Matrix();
    Matrix mStickSavedMatrix = new Matrix();

    //stickbitmap Frame的矩阵
    Matrix mStickFrameMatrix = new Matrix();
    Matrix mStickFrameTransMatrix1 = new Matrix();
    Matrix mStickFrameSavedMatrix = new Matrix();

    //textbitmap 的矩阵
    Matrix mTextMatrix = new Matrix();
    Matrix mTextTransMatrix1 = new Matrix();
    Matrix mTextSavedMatrix = new Matrix();

    //textbitmap 的矩阵
    Matrix mTextFrameMatrix = new Matrix();
    Matrix mTextFrameTransMatrix1 = new Matrix();
    Matrix mTextFrameSavedMatrix = new Matrix();


    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    //绘制的固定参数
    public float OUTSIDE_RADIOU = Convert.dip2px(getContext(), 14);
    private float INSIDE_RADIOU = Convert.dip2px(getContext(), 9);
    private float STROKE_WIDTH = Convert.dip2px(getContext(), 2);
    private float LOCATION_PADDING = Convert.dip2px(getContext(), 15);
    private float MIN_STICK_SCALE_HEIGHT = OUTSIDE_RADIOU * 4;
    private float MIN_TEXT_SCALE_HEIGHT = OUTSIDE_RADIOU * 3;
    private float mStickMinSacle;
    private float mTextMinSacle;


    private float range = OUTSIDE_RADIOU;
    private int mode = NONE;
    private int mBgWidth;
    private int mBgHeight;
    private boolean isShowStick;
    private boolean isShowText;
    private boolean isHideStickFrame;
    private boolean isHidTextFrame;
    private boolean isShowAddress;


    //stickerBitmap 四个点的位置
    private float[] sp = new float[8];
    private float[] tp = new float[8];


    //
    private float[] sfp = new float[8];
    private float[] tfp = new float[8];


    boolean mMatrixCheck = false;
    int widthScreen;
    int heightScreen;
    private float savex, savey, curx, cury;
    private Bitmap mStickBitmap;
    private Bitmap mTextBitmap;
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

    //实时的背景，用户旋转操作时需将其射进来
    private Bitmap mBgBitmap;
    private static final int STICK_HANDLE = 0;
    private static final int TEXT_HANDLE = 1;
    private static final int NONE_HANDLE = 2;
    private int mLastHandle = -1;
    private int mCurHandle = -1;

    // 生成图片leftTopMargin
    private float topMargin;
    private float leftMargin;
    private float scaleHeight;
    private float scaleWidth;
    private boolean isEverShowText;

    public boolean isEverShowText() {
        return isEverShowText;
    }

    //删除的listener
    private OnStickDelListener mOnStickDelListener;
    private OnStickTextDelListener mOnStickTextDelListener;

    public interface OnStickDelListener {
        public void onStickDel();
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


    public StickerView(Context context) {
        super(context);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthScreen = dm.widthPixels;
        heightScreen = dm.heightPixels;
        mStickMatrix = new Matrix();
        mStickBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mTextBitmap = BitmapUtil.createBitmap(context, context.getString(R.string.app_name));
        mLocBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_local_draw);
//        mScaleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_process_scale);
//        mDelBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_process_del);
        zoomDisplayBitmap();
        matrixCheck(mStickTransMatrix1, sp, mStickBitmap);
        matrixCheck(mTextTransMatrix1, tp, mTextBitmap);
        // Disable hardware acceleration to make sure bitmaps are drawn with anti-aliasing
        // See http://stackoverflow.com/questions/14378573/bitmap-not-drawn-anti-aliased/14443954
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthScreen = dm.widthPixels;
        heightScreen = dm.heightPixels;
        mStickMatrix = new Matrix();
        mStickBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mTextBitmap = BitmapUtil.createBitmap(context, context.getString(R.string.app_name));
        mLocBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_local_draw);
//        mScaleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_process_scale);
//        mDelBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_process_del);
        zoomDisplayBitmap();
        matrixCheck(mStickTransMatrix1, sp, mStickBitmap);
        matrixCheck(mTextTransMatrix1, tp, mTextBitmap);
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
//        Path path = new Path();
//        path.moveTo(x3, y3);
//        path.lineTo(x4, y4);
//        path.lineTo(x2, y2);
//        path.lineTo(x1, y1);
//        path.close();
//        canvas.drawPath(path, paint);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
//      paint.setPathEffect(new DashPathEffect(new float[]{4, 2, 4, 2}, 0));
        canvas.drawLine(fp[0], fp[1], fp[2] + STROKE_WIDTH / 2, fp[3], paint);
        canvas.drawLine(fp[0], fp[1], fp[4], fp[5], paint);
        canvas.drawLine(fp[2], fp[3], fp[6], fp[7], paint);
        canvas.drawLine(fp[4] - STROKE_WIDTH / 2, fp[5], fp[6], fp[7], paint);

//        设置填充
//        paint.setColor(Color.RED);
//        canvas.drawCircle(x1, y1, OUTSIDE_RADIOU, paint);
//        canvas.drawCircle(x2, y2, OUTSIDE_RADIOU, paint);
//        canvas.drawCircle(x3, y3, OUTSIDE_RADIOU, paint);
//        canvas.drawCircle(x4, y4, OUTSIDE_RADIOU, paint);
        paint.setStyle(Paint.Style.FILL);


        /**
         * 绘制UI提供的操作图
         */
//        canvas.drawBitmap(mDelBitmap, fp[0] - mDelBitmap.getWidth() / 2, fp[1] - mDelBitmap.getHeight() / 2, paint);
//        canvas.drawBitmap(mScaleBitmap, fp[6] - mScaleBitmap.getWidth() / 2, fp[7] - mScaleBitmap.getHeight() / 2, paint);

        /**
         * 创建绘制边框的边界值
         * 暂时不绘制2,3点
         */
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
//      canvas.drawBitmap(bg, 0, 0, null);
        canvas.save();
        drawText(canvas);
        //根据curHandle来,判断绘制的先后顺序
        drawStickAndTexByHandle(canvas);
        canvas.restore();

    }

    private void drawStickAndTexByHandle(Canvas canvas) {
//        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
//        canvas.setDrawFilter(new PaintFlagsDrawFilter(1, Paint.ANTI_ALIAS_FLAG));
        Paint bitmapPaint = new Paint();//Paint.FILTER_BITMAP_FLAG//Paint.ANTI_ALIAS_FLAG
//      bitmapPaint.setAntiAlias(true);
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
//      bitmapPaint.setDither(true);


        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);

        if (mCurHandle == STICK_HANDLE) {
            if (isShowText) {
                canvas.drawBitmap(mTextBitmap, mTextMatrix, bitmapPaint);
                if (!isHidTextFrame) {
                    drawFrame(canvas, tfp, paint);
                }
            }
            if (isShowStick) {
                canvas.drawBitmap(mStickBitmap, mStickMatrix, bitmapPaint);
                if (!isHideStickFrame) {
                    drawFrame(canvas, sfp, paint);
                }
            }
        } else if (mCurHandle == TEXT_HANDLE) {
            if (isShowStick) {
                canvas.drawBitmap(mStickBitmap, mStickMatrix, bitmapPaint);
                if (!isHideStickFrame) {
                    drawFrame(canvas, sfp, paint);
                }
            }
            if (isShowText) {
                if (!isHidTextFrame) {
                    drawFrame(canvas, tfp, paint);
                }
                canvas.drawBitmap(mTextBitmap, mTextMatrix, bitmapPaint);
            }
        } else {
            if (isShowStick) {
                canvas.drawBitmap(mStickBitmap, mStickMatrix, bitmapPaint);
                if (!isHideStickFrame) {
                    drawFrame(canvas, sfp, paint);
                }
            }
            if (isShowText) {
                canvas.drawBitmap(mTextBitmap, mTextMatrix, bitmapPaint);
                if (!isHidTextFrame) {
                    drawFrame(canvas, tfp, paint);
                }
            }
        }

    }

    private void onStickHandleDown(float x_down, float y_down) {
        if (isDeleltePoint(x_down, y_down, sfp)) {
            mode = ZOOM;
            if (!isHideStickFrame) {
                isShowStick = false;
//                invalidate();
                if (mOnStickDelListener != null) {
                    mOnStickDelListener.onStickDel();
                }
            }
        } else {
            mode = ZOOM;
            midPoint(mStickMid, sp[0], sp[1], sp[6], sp[7]);
            mOldStickRotation = (float) getRorateDegrees(mStickMid.x, mStickMid.y, savex, savey, curx, cury);
            mOldStickDist = spacing(mStickMid.x, mStickMid.y, sp[6], sp[7]);
            float mFrameOldStickDist = spacing(mStickMid.x, mStickMid.y, sfp[6], sfp[7]);
            //计算偏移的stickScale
            _scaleDelta = spacing(mStickMid.x, mStickMid.y, curx, cury) - mFrameOldStickDist;
            //计算边框的最小缩放值
            mStickMinSacle = MIN_STICK_SCALE_HEIGHT / spacing(sfp[0], sfp[1], sfp[4], sfp[5]);
            //恢复初始
            if (spacing(sp[0], sp[1], sp[4], sp[5]) < MIN_STICK_SCALE_HEIGHT) {
                float frameDist = spacing(mStickMid.x, mStickMid.y, sfp[6], sfp[7]);
                float scale = frameDist / mOldStickDist;
                mStickMatrix.postScale(scale, scale, mStickMid.x, mStickMid.y);// 縮放
                mOldStickDist = frameDist;
//                invalidate();
            }
        }
    }

    private void onTextHandleDown(float x_down, float y_down) {
        if (isDeleltePoint(x_down, y_down, tfp)) {
            if (!isHidTextFrame) {
                mode = ZOOM;
                isShowText = false;
//                invalidate();
                if (mOnStickTextDelListener != null) {
                    mOnStickTextDelListener.onStickTextDel();
                }
            }
        } else {
            mode = ZOOM;
            midPoint(mTextMid, tp[0], tp[1], tp[6], tp[7]);
            mOldTextRotation = (float) getRorateDegrees(mTextMid.x, mTextMid.y, savex, savey, curx, cury);
            mOldTextDist = spacing(mTextMid.x, mTextMid.y, tp[6], tp[7]);
            float mFrameOldTextDist = spacing(mTextMid.x, mTextMid.y, tfp[6], tfp[7]);
            //TODO(一个touch事件，只会产生一个_scaleDelta) 故不进行区分是sticker，Text
            _scaleDelta = spacing(mTextMid.x, mTextMid.y, curx, cury) - mFrameOldTextDist;
            //计算边框的最小缩放值
            mTextMinSacle = MIN_TEXT_SCALE_HEIGHT / spacing(tfp[0], tfp[1], tfp[4], tfp[5]);
            //恢复初始
            if (spacing(tp[0], tp[1], tp[4], tp[5]) < MIN_TEXT_SCALE_HEIGHT) {
                float frameDist = spacing(mTextMid.x, mTextMid.y, tfp[6], tfp[7]);
                float scale = frameDist / mOldTextDist;
                mTextMatrix.postScale(scale, scale, mTextMid.x, mTextMid.y);// 縮放
                mOldTextDist = frameDist;
//                invalidate();
            }
        }
    }

    //恢复外边框的状态
    private void resumeFrameStatus() {
        if (isHidTextFrame && mCurHandle == TEXT_HANDLE) {
            isHidTextFrame = false;
        }
        if (isHideStickFrame && mCurHandle == STICK_HANDLE) {
            isHideStickFrame = false;
        }
        invalidate();
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
                matrixCheck(mTextMatrix, tp, mTextBitmap);
                matrixCheck(mStickMatrix, sp, mStickBitmap);
                matrixCheck(mStickFrameMatrix, sfp, mStickBitmap);
                matrixCheck(mTextFrameMatrix, tfp, mTextBitmap);
                //根据坐标位置判断是否移动和缩放//判断是否为zoom or drag ,取zoom的优先级
                if (isShowStick && isShowText && isZoomPoint(x_down, y_down, sfp) && isZoomPoint(x_down, y_down, tfp)) {
                    if (mLastHandle == STICK_HANDLE) {
                        mCurHandle = STICK_HANDLE;
                        onStickHandleDown(x_down, y_down);
                        resumeFrameStatus();
                    } else if (mLastHandle == TEXT_HANDLE) {
                        mCurHandle = TEXT_HANDLE;
                        onTextHandleDown(x_down, y_down);
                        resumeFrameStatus();
                    }
                } else if (isShowStick && isZoomPoint(x_down, y_down, sfp)) {
                    mCurHandle = STICK_HANDLE;
                    onStickHandleDown(x_down, y_down);
                    resumeFrameStatus();

                } else if (isShowText && isZoomPoint(x_down, y_down, tfp)) {
                    mCurHandle = TEXT_HANDLE;
                    onTextHandleDown(x_down, y_down);
                    resumeFrameStatus();
                } else if (isShowStick && isShowText && isDragPoint(x_down, y_down, sfp) && isDragPoint(x_down, y_down, tfp)) {
                    if (mLastHandle == STICK_HANDLE) {
                        mCurHandle = STICK_HANDLE;
                        midPoint(mStickMid, sp[0], sp[1], sp[6], sp[7]);
                    } else if (mLastHandle == TEXT_HANDLE) {
                        mCurHandle = TEXT_HANDLE;
                        midPoint(mTextMid, tp[0], tp[1], tp[6], tp[7]);
                    }
                    mode = DRAG;
                    resumeFrameStatus();
                } else if (isShowStick && isDragPoint(x_down, y_down, sfp)) {// isDragPoint(x_down, y_down,mStickFrameMatrix, mStickBitmap)) {
                    mCurHandle = STICK_HANDLE;
                    mode = DRAG;
                    resumeFrameStatus();
                    midPoint(mStickMid, sp[0], sp[1], sp[6], sp[7]);

                } else if (isShowText && isDragPoint(x_down, y_down, tfp)) {
                    ;// isDragPoint(x_down, y_down,mTextFrameMatrix,mTextBitmap)) {
                    mCurHandle = TEXT_HANDLE;
                    mode = DRAG;
                    midPoint(mTextMid, tp[0], tp[1], tp[6], tp[7]);
                    resumeFrameStatus();
                } else {
                    //if (mode == NONE) {
                    if (isShowText && isShowStick) {
                        if (mCurHandle == STICK_HANDLE) {
                            if (isHideStickFrame && !isHidTextFrame) {
                                isHidTextFrame = true;
                            } else {
                                isHideStickFrame = true;
                            }
                            invalidate();
                        } else {
                            if (isHidTextFrame && !isHideStickFrame) {
                                isHideStickFrame = true;
                            } else {
                                isHidTextFrame = true;
                            }
                            invalidate();
                        }
                    } else if (isShowText) {
                        isHidTextFrame = true;
                        invalidate();
                    } else if (isShowStick) {
                        isHideStickFrame = true;
                        invalidate();
                    }
                }
                mLastHandle = mCurHandle;
                mStickSavedMatrix.set(mStickMatrix);
                mTextSavedMatrix.set(mTextMatrix);
                mStickFrameSavedMatrix.set(mStickFrameMatrix);
                mTextFrameSavedMatrix.set(mTextFrameMatrix);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    if (mCurHandle == STICK_HANDLE) {
                        mStickTransMatrix1.set(mStickSavedMatrix);
                        float rotation = (float) (getRorateDegrees(mStickMid.x, mStickMid.y, savex, savey, curx, cury));
                        float displayRotation;
                        if (Float.compare(rotation, Float.NaN) == 0) {
                            mOldStickRotation += 0.0;
                            displayRotation = mOldStickRotation;
                        } else {
                            if (Math.abs(rotation) < 1.5f) {//小于0.5度则不进行旋转
                                displayRotation = mOldStickRotation;
                            } else {
                                displayRotation = mOldStickRotation + rotation;
                            }
                            mOldStickRotation += rotation;
                        }
                        //避免直线滑动，造成图片翻转 ，没有生效
//                        displayRotation = displayRotation % 360;
                        float newDist = spacing(mStickMid.x, mStickMid.y, curx, cury) - _scaleDelta;
                        float scale = newDist / mOldStickDist;
                        mStickTransMatrix1.postRotate(displayRotation, mStickMid.x, mStickMid.y);
                        mStickTransMatrix1.postScale(scale, scale, mStickMid.x, mStickMid.y);// 縮放
                        mMatrixCheck = matrixCheck(mStickTransMatrix1, sp, mStickBitmap);
                        mStickMatrix.set(mStickTransMatrix1);

                        mStickFrameTransMatrix1.set(mStickFrameSavedMatrix);
                        mStickFrameTransMatrix1.postRotate(displayRotation, mStickMid.x, mStickMid.y);
                        if (spacing(sp[0], sp[1], sp[4], sp[5]) < MIN_STICK_SCALE_HEIGHT) {
                            mStickFrameTransMatrix1.postScale(mStickMinSacle, mStickMinSacle, mStickMid.x, mStickMid.y);// 縮放
                            matrixCheck(mStickFrameTransMatrix1, sfp, mStickBitmap);
                        } else {
                            mStickFrameTransMatrix1.postScale(scale, scale, mStickMid.x, mStickMid.y);// 縮放
                            matrixCheck(mStickFrameTransMatrix1, sfp, mStickBitmap);
                        }
                        mStickFrameMatrix.set(mStickFrameTransMatrix1);
                        invalidate();
                    } else if (mCurHandle == TEXT_HANDLE) {
                        mTextTransMatrix1.set(mTextSavedMatrix);
                        float rotation = (float) (getRorateDegrees(mTextMid.x, mTextMid.y, savex, savey, curx, cury));
                        float displayRotation;
                        if (Float.compare(rotation, Float.NaN) == 0) {
                            mOldTextRotation += 0.0;
                            displayRotation = mOldTextRotation;
                        } else {
                            if (Math.abs(rotation) < 1.5f) {//小于0.5度则不进行旋转
                                displayRotation = mOldTextRotation;
                            } else {

                                displayRotation = mOldTextRotation + rotation;
                            }
                            mOldTextRotation += rotation;
                        }
                        //避免直线滑动，造成图片翻转,没有生效
//                        displayRotation = displayRotation % 360;
                        float newDist = spacing(mTextMid.x, mTextMid.y, curx, cury) - _scaleDelta;
                        float scale = newDist / mOldTextDist;
                        mTextTransMatrix1.postScale(scale, scale, mTextMid.x, mTextMid.y);// 縮放
                        mTextTransMatrix1.postRotate(displayRotation, mTextMid.x, mTextMid.y);
                        mMatrixCheck = matrixCheck(mTextTransMatrix1, tp, mTextBitmap);
                        mTextMatrix.set(mTextTransMatrix1);

                        mTextFrameTransMatrix1.set(mTextFrameSavedMatrix);
                        mTextFrameTransMatrix1.postRotate(displayRotation, mTextMid.x, mTextMid.y);
                        if (spacing(tp[0], tp[1], tp[4], tp[5]) < MIN_TEXT_SCALE_HEIGHT) {
                            mTextFrameTransMatrix1.postScale(mTextMinSacle, mTextMinSacle, mTextMid.x, mTextMid.y);// 縮放
                            matrixCheck(mTextFrameTransMatrix1, tfp, mTextBitmap);
                        } else {
                            mTextFrameTransMatrix1.postScale(scale, scale, mTextMid.x, mTextMid.y);// 縮放
                            matrixCheck(mTextFrameTransMatrix1, tfp, mTextBitmap);
                        }
                        mTextFrameMatrix.set(mTextFrameTransMatrix1);
                        invalidate();
                    }

                } else if (mode == DRAG) {
                    if (mCurHandle == STICK_HANDLE) {
                        mStickTransMatrix1.set(mStickSavedMatrix);
                        mStickTransMatrix1.postTranslate(event.getX() - x_down, event.getY() - y_down);// 平移
                        mMatrixCheck = matrixCheck(mStickTransMatrix1, sp, mStickBitmap);
                        mStickMatrix.set(mStickTransMatrix1);
//边框
                        mStickFrameTransMatrix1.set(mStickFrameSavedMatrix);
                        mStickFrameTransMatrix1.postTranslate(event.getX() - x_down, event.getY() - y_down);// 平移
                        matrixCheck(mStickFrameTransMatrix1, sfp, mStickBitmap);
                        mStickFrameMatrix.set(mStickFrameTransMatrix1);
                        invalidate();
                    } else if (mCurHandle == TEXT_HANDLE) {
                        mTextTransMatrix1.set(mTextSavedMatrix);
                        mTextTransMatrix1.postTranslate(event.getX() - x_down, event.getY() - y_down);// 平移
                        mMatrixCheck = matrixCheck(mTextTransMatrix1, tp, mTextBitmap);
                        mTextMatrix.set(mTextTransMatrix1);
//边框
                        mTextFrameTransMatrix1.set(mTextFrameSavedMatrix);
                        mTextFrameTransMatrix1.postTranslate(event.getX() - x_down, event.getY() - y_down);// 平移
                        matrixCheck(mTextFrameTransMatrix1, tfp, mTextBitmap);
                        mTextFrameMatrix.set(mTextFrameTransMatrix1);
                        invalidate();
                    }
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
                (x_down <= p[4] + range && x_down >= p[4] - range && y_down <= p[5] + range && y_down >= p[5] - range)
                ) {
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
//        Matrix的invert(Matrix)方法用来反操作一个Matrix，比如matrix用来把一个图片旋转30度，那么matrix.invert(inMatrix)，inMatrix会反向旋转30度。
        matrix.invert(inMatrix);
        float[] dst = new float[2];
        //inMatrix.mapPoints(float[] dst, float[] src); 是把src对应的坐标点还原到没有旋转30度之前的坐标，保存到dst中。我们就可以直接用原来的判断方式了，比如onTouchEvent中
        float[] p = new float[8];
        matrixCheck(inMatrix, p, bitmap);
//      if(dst[0] > 0 && dst[0] < bitmap.getWidth() && dst[1] > 0 && dst[1] < bitmap.getHeight())
        inMatrix.mapPoints(dst, new float[]{x_down, y_down});
        Ln.i("dst[0]" + dst[0] + ":----dst[1]" + dst[1]);
        Ln.i("x_down" + x_down + ":----y_down" + y_down);
        if (p[0] <= x_down && x_down <= p[2] && p[1] <= y_down && y_down <= p[5]) {
            return true;
        }
        return false;
    }


    private float[] getMaxAndMin(float[] data) {
        float min = 0, max = 0;
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

    private boolean matrixCheck(Matrix matrix, float[] ps, Bitmap bitmap) {
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
/**
 * 强哥整理
 */
//        float cos = f[0];
//        float sin = f[3];
//        float sinf = f[1];
//        float translateX = f[2];
//        float translateY = f[5];
//        x1 = translateX;
//        y1 = translateY;
//        x2 = x1 + (gintama.getWidth() * cos);
//        y2 = y1 + (gintama.getWidth() * sin);
//        x3 = x1 - (gintama.getHeight() * sin);
//        y3 = y1 + (gintama.getHeight() * cos);
//        x4 = x1 + gintama.getWidth() * cos + sinf * gintama.getHeight();
//        y4 = y1 + sin * gintama.getWidth() + cos * gintama.getHeight();

        // 图片现宽度
        double width = Math.sqrt((ps[0] - ps[2]) * (ps[0] - ps[2]) + (ps[1] - ps[3]) * (ps[1] - ps[3]));
        // 缩放比率判断  
        if (width < widthScreen / 3 || width > widthScreen * 3) {
            return true;
        }
        // 出界判断  
        if ((ps[0] < widthScreen / 3 && ps[2] < widthScreen / 3
                && ps[4] < widthScreen / 3 && ps[6] < widthScreen / 3)
                || (ps[0] > widthScreen * 2 / 3 && ps[2] > widthScreen * 2 / 3
                && ps[4] > widthScreen * 2 / 3 && ps[6] > widthScreen * 2 / 3)
                || (ps[1] < heightScreen / 3 && ps[3] < heightScreen / 3
                && ps[5] < heightScreen / 3 && ps[7] < heightScreen / 3)
                || (ps[1] > heightScreen * 2 / 3 && ps[3] > heightScreen * 2 / 3
                && ps[5] > heightScreen * 2 / 3 && ps[7] > heightScreen * 2 / 3)) {
            return true;
        }
        return false;
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

    @Deprecated
    public Bitmap creatFavouritePhoto(Bitmap bgBitmap) {
        Paint bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
//      bitmapPaint.setFilterBitmap(true);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap bitmap;
        bitmap = Bitmap.createBitmap(mBgWidth, mBgHeight, Config.ARGB_8888); // 背景图片
        getLTRB(bgBitmap);
        Canvas canvas = new Canvas(bitmap); //新建画布
        bgBitmap = BitmapUtil.zoomBitmap(bgBitmap, (int) scaleWidth, (int) scaleHeight, false);
        canvas.drawBitmap(bgBitmap, leftMargin, topMargin, bitmapPaint);
        //绘制的先后顺序
        if (mCurHandle == STICK_HANDLE) {
            if (isShowText) {
                canvas.drawBitmap(mTextBitmap, mTextMatrix, bitmapPaint);
            }
            if (isShowStick) {
                canvas.drawBitmap(mStickBitmap, mStickMatrix, bitmapPaint);
            }
        } else if (mCurHandle == TEXT_HANDLE) {
            if (isShowStick) {
                canvas.drawBitmap(mStickBitmap, mStickMatrix, bitmapPaint);
            }
            if (isShowText) {
                canvas.drawBitmap(mTextBitmap, mTextMatrix, bitmapPaint);
            }
        } else {
            if (isShowStick) {
                canvas.drawBitmap(mStickBitmap, mStickMatrix, bitmapPaint);
            }
            if (isShowText) {
                canvas.drawBitmap(mTextBitmap, mTextMatrix, bitmapPaint);
            }
        }
        drawText(canvas);
        //对图片进行裁剪：
        //topmargin+1px 是因为改变actionbar高度后，裁剪时出现黑线，如果要去掉加1px ,则回复actionbar的高度48dp
        bitmap = Bitmap.createBitmap(bitmap, (int) leftMargin, (int) topMargin + 1, (int) scaleWidth, (int) scaleHeight - 1);
        canvas.save(Canvas.ALL_SAVE_FLAG); // 保存画布
        canvas.restore();
        return bitmap;
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
//      bitmapPaint.setFilterBitmap(true);

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
        mTextMatrix.postTranslate(transW / 2, transH / 2);
        mTextMatrix.postScale(screenScale, screenScale, mOldTextMid.x + transW / 2, mOldTextMid.y + transH / 2);// 縮放

        mStickMatrix.postTranslate(transW / 2, transH / 2);
        mStickMatrix.postScale(screenScale, screenScale, mOldStickMid.x + transW / 2, mOldStickMid.y + transH / 2);// 縮放

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
            if (mCurHandle == STICK_HANDLE) {
                if (isShowText) {
                    canvas.drawBitmap(mTextBitmap, mTextMatrix, bitmapPaint);
                }
                if (isShowStick) {
                    canvas.drawBitmap(mStickBitmap, mStickMatrix, bitmapPaint);
                }
            } else if (mCurHandle == TEXT_HANDLE) {
                if (isShowStick) {
                    canvas.drawBitmap(mStickBitmap, mStickMatrix, bitmapPaint);
                }
                if (isShowText) {
                    canvas.drawBitmap(mTextBitmap, mTextMatrix, bitmapPaint);
                }
            } else {
                if (isShowStick) {
                    canvas.drawBitmap(mStickBitmap, mStickMatrix, bitmapPaint);
                }
                if (isShowText) {
                    canvas.drawBitmap(mTextBitmap, mTextMatrix, bitmapPaint);
                }
            }
            drawText(canvas);
            //对图片进行裁剪：
            //topmargin+1px 是因为改变actionbar高度后，裁剪时出现黑线，如果要去掉加1px ,则回复actionbar的高度48dp
             saveBitmap = Bitmap.createBitmap(bitmap, (int) leftMargin, (int) topMargin + 1, (int) scaleWidth, (int) scaleHeight - 1);
             canvas.save(Canvas.ALL_SAVE_FLAG); // 保存画布
             canvas.restore();
        }finally {
            if(bgBitmap != null && !bgBitmap.isRecycled()){
                bgBitmap.recycle();
            }
            if(bitmap != null && !bitmap.isRecycled()){
                bitmap.recycle();
            }
            if(mStickBitmap != null && !mStickBitmap.isRecycled()){
                mStickBitmap.recycle();
                mStickBitmap=null;
            }
            if(mTextBitmap != null && !mTextBitmap.isRecycled()){
                mTextBitmap.recycle();
                mTextBitmap=null;
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
            Bitmap bitmap = creatFavouritePhoto(bg);
            FileOutputStream fos = new FileOutputStream(new File(saveFile, "tem.png"));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStickBitmap(Bitmap stickBitmap) {
        if (isHideStickFrame) {
            isHideStickFrame = false;
        }
        if(mStickBitmap !=null){
            mStickBitmap.recycle();
            mStickBitmap=null;
        }
        //计算出要显示的scale；
        int widthPx = Convert.dip2px(getContext(), Constant.STICK_WIDTH);
        float scale = (float) widthPx / (float) stickBitmap.getWidth();

        //拉伸图片到相同比例
        mStickBitmap = stickBitmap;
        int  mOriginStickWidth = stickBitmap.getWidth();
        int  mOriginStickHeight = stickBitmap.getHeight();
        //算出初始位置
        mStickMatrix.setTranslate((mBgWidth / 2 - mOriginStickWidth / 2), (mBgHeight / 2 - mOriginStickHeight / 2));
        mStickMatrix.postScale(scale, scale, (mBgWidth / 2), mBgHeight / 2);
        matrixCheck(mStickMatrix, sp, mStickBitmap);

        //边框
        mStickFrameMatrix.set(mStickMatrix);
        matrixCheck(mStickFrameMatrix, sfp, mStickBitmap);

        midPoint(mStickMid, sp[0], sp[1], sp[6], sp[7]);
        midPoint(mOldStickMid, sp[0], sp[1], sp[6], sp[7]);
//        Ln.i("mStickMid.x"+mStickMid+"----");
        isShowStick = true;
        mCurHandle = mLastHandle = STICK_HANDLE;
        invalidate();
    }

    public void setBgWidthHeight(int bgWidth, int bgHeight) {
        mBgWidth = bgWidth;
        mBgHeight = bgHeight;
    }



    public void setTextBitmap(Bitmap textBitmap) {
        if (isHidTextFrame) {
            isHidTextFrame = false;
        }

        isEverShowText = true;
        if(mTextBitmap !=null){
            mTextBitmap.recycle();
            mTextBitmap=null;
        }
        mTextBitmap = textBitmap;
        int mOriginTextWidth = textBitmap.getWidth();
        int mOriginTextHeight = textBitmap.getHeight();

        //算出初始位置
        mTextMatrix.setTranslate((mBgWidth / 2 - mOriginTextWidth / 2), (mBgHeight / 2 - mOriginTextHeight / 2));
        mTextMatrix.postScale(0.5f, 0.5f, (mBgWidth / 2), mBgHeight / 2);
        matrixCheck(mTextMatrix, tp, mTextBitmap);

        //边框
        mTextFrameMatrix.set(mTextMatrix);
        matrixCheck(mTextFrameMatrix, tfp, mTextBitmap);

        midPoint(mTextMid, tp[0], tp[1], tp[6], tp[7]);
        midPoint(mOldTextMid, tp[0], tp[1], tp[6], tp[7]);

        isShowText = true;
        mCurHandle = mLastHandle = TEXT_HANDLE;
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

}
