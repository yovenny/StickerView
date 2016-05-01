package com.yovenny.stickview.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.FloatMath;

/**
 * Created by skyArraon on 16/4/29.
 */
public class BitmapUtil {

    public static Bitmap getSampledBitmap(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                //注意floor的算法
                inSampleSize = (int) FloatMath.floor(((float) height / reqHeight) + 0.5f); //Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = (int) FloatMath.floor(((float) width / reqWidth) + 0.5f); //Math.round((float)width / (float)reqWidth);
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 字符串转换成图片
     */
    public static Bitmap createWarpBitmap(Context context, String str, int color) {
        float desity = DensityUtil.getScreenDensity(context);
        Paint paint = new Paint();
        paint.measureText(str);
        paint.setTextSize(14*desity);
        paint.setFakeBoldText(true);
        paint.setColor(color);
//        paint.setTypeface(Typeface.BOLD);
        paint.setAntiAlias(true);
        paint.setTextSkewX(0);
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        String[] lines = str.split("\n");
        float txtSize = rect.height();//-paint.ascent() + paint.descent();
        float lineSpace = txtSize * 0.2f;
        float y = desity * 10;
        int h = (int) (y * 2 + (txtSize * lines.length) + lineSpace * (lines.length - 1));
        int index = 0;
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i + 1].length() > lines[index].length()) {
                index = i + 1;
            }
        }
        int padding=50;
        int w = (int) paint.measureText(lines[index]) + (int) desity * padding;//100
        Bitmap bp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bp);
        for (int i = 0; i < lines.length; ++i) {
            paint.setTextAlign(Paint.Align.LEFT);
            Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            int top = (int) (y + txtSize * i + lineSpace * i);
            int bottom = (int) (y + txtSize * (i + 1) + lineSpace * i);
            int baseline = top + (bottom - top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
            c.drawText(lines[i], desity * padding/2, baseline, paint);
        }
        c.save(Canvas.ALL_SAVE_FLAG);
        c.restore();
        return bp;
    }
}
