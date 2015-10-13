package com.yovenny.stickview.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.view.View;
import android.view.Window;

import com.yovenny.stickview.Constant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;




public class BitmapUtil {
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Bitmap 放大
     *
     * @param rect          Display对象
     * @param bitmap        图片
     * @param isResizeWidth true按宽度拉，false按高度拉
     * @return Bitmap
     */
    public static Bitmap resizeBitmap(Rect rect, Bitmap bitmap, boolean isResizeWidth, boolean recycleOrig) {
        int screenWidth = rect.width();
        int screenHeight = rect.height();

        if (isResizeWidth) {
            if (screenWidth != bitmap.getWidth()) {
                float scale = (float) screenWidth / bitmap.getWidth();
                int height = (int) (bitmap.getHeight() * scale);

                Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, height, true);
                if (recycleOrig && !bitmap.isRecycled()) bitmap.recycle();
                bitmap = newBitmap;
            }

            // 按宽度拉伸后，高度超出screenHeight，所以要把超出的部分截取掉
            if (bitmap.getHeight() > screenHeight) {
                int startY = (bitmap.getHeight() - screenHeight) / 2;
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, startY, screenWidth, screenHeight);
                if (recycleOrig && !bitmap.isRecycled()) bitmap.recycle();
                bitmap = newBitmap;
            }
        } else {
            if (screenHeight != bitmap.getHeight()) {  //
                float scale = (float) screenHeight / bitmap.getHeight();
                int width = (int) (bitmap.getWidth() * scale);

                if (width > screenWidth) {  //如果拉伸完宽度大于屏幕宽度，则按宽度拉伸
                    float widthScale = (float) screenWidth / bitmap.getWidth();
                    int height = (int) (bitmap.getHeight() * widthScale);
                    Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, height, true);
                    if (recycleOrig && widthScale != 1 && !bitmap.isRecycled()) bitmap.recycle();
                    bitmap = newBitmap;
                } else {
                    Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, screenHeight, true);
                    if (recycleOrig && !bitmap.isRecycled()) bitmap.recycle();
                    bitmap = newBitmap;
                }


            }
        }


        return bitmap;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // 获取指定Activity的截屏
    public static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        Bitmap bmap = view.getDrawingCache();
        int contentViewTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop(); /* skip status bar in screenshot */
        Bitmap smap = Bitmap.createBitmap(bmap, 0, contentViewTop, bmap.getWidth(), bmap.getHeight() - contentViewTop, null, true);
        view.setDrawingCacheEnabled(false);
        return smap;
    }

    /**
     * 保存截图到临时文件夹
     *
     * @param activity
     * @return 保存的文件实例
     */
    public static File saveScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        Bitmap bmap = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        Bitmap smap = Bitmap.createBitmap(bmap, 0, statusBarHeight, bmap.getWidth(), bmap.getHeight() - statusBarHeight, null, true);
        view.setDrawingCacheEnabled(false);
        try {
            File file = FileUtil.createTempFile("screenShot", ".png");
            FileOutputStream fop = new FileOutputStream(file);
            smap.compress(Bitmap.CompressFormat.PNG, 100, fop);
            smap.recycle();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            Ln.e(e.getMessage());
        }
        return null;
    }

    public static Bitmap createScaledWidthBmp(Bitmap src, int newWidth, boolean needRecycle) {
        int outWidth = src.getWidth();
        int outHeight = src.getHeight();

        if (newWidth >= outWidth) {
            return src;
        }
        float scale = (float) newWidth / (float) outWidth;
        int newHeight = (int) ((float) outHeight * scale);
        Bitmap dest = Bitmap.createScaledBitmap(src, newWidth, newHeight, true);

        //判断是否需要回收
        if (needRecycle && !src.isRecycled()) {
            src.recycle();
        }

        return dest;
    }

    public static Bitmap createFitWidthBmp(Bitmap src, int newWidth, boolean needRecycle) throws OutOfMemoryError {
        int outWidth = src.getWidth();
        int outHeight = src.getHeight();

        float scale = (float) newWidth / (float) outWidth;
        int newHeight = (int) ((float) outHeight * scale);
        Bitmap dest = Bitmap.createScaledBitmap(src, newWidth, newHeight, true);

        //判断是否需要回收
        if (needRecycle && !src.isRecycled()) {
            src.recycle();
        }

        return dest;
    }

    public static Bitmap createRotateBmp(Bitmap src, int degree, boolean needRecycle) throws OutOfMemoryError {
        Matrix matrix = new Matrix();
        //设置图像的旋转角度
        matrix.setRotate(degree);
        //旋转图像，并生成新的Bitmap对像
        Bitmap dest = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (needRecycle && !src.isRecycled()) {
            src.recycle();
        }
        return dest;
    }

    //decodes image and scales it to reduce memory consumption
    public static Bitmap decodeBmpFromFile(File f, int reqWidth, int reqHeight, boolean isFitStyle) {
        ByteArrayOutputStream baos = StreamUtil.getByteArrayOSFromFile(f);
        BitmapFactory.Options orgOpts = BitmapUtil.getOrgImageOpts(baos);
        //有可能f是一个不完整的文件，需要判断一下orgOpts是否为null
        if (orgOpts == null) {
            return null;
        }
        orgOpts.inSampleSize = BitmapUtil.calculateInSampleSize(orgOpts, reqWidth, reqHeight);
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inSampleSize = orgOpts.inSampleSize;
        if (isFitStyle) {
            return BitmapUtil.decodeFitBmpFromOS(baos, newOpts, 0, reqWidth);
        }
        return BitmapUtil.decodeBmpFromOS(baos, newOpts, 0, reqWidth);
    }

    public static Bitmap decodeBmpFromOS(ByteArrayOutputStream baos, BitmapFactory.Options opts, int degree, int scaledWidth) {
        InputStream is = null;
        try {
            //TODO 图片过大可能导致 toByteArray() OOM
            is = new ByteArrayInputStream(baos.toByteArray());
            opts.inJustDecodeBounds = false;
            Bitmap srcBmp = BitmapFactory.decodeStream(is, null, opts);
            StreamUtil.closeCloseable(baos);
            StreamUtil.closeCloseable(is);
            Bitmap retBmp = null;
            if (srcBmp != null) {
                if (degree != 0) {   //旋转判断
                    retBmp = BitmapUtil.createRotateBmp(srcBmp, degree, true);
                } else {
                    retBmp = srcBmp;
                }
                retBmp = BitmapUtil.createScaledWidthBmp(retBmp, scaledWidth, true);
            }
            return retBmp;
        } catch (Exception e) {
            if (Ln.DEBUG) {
                e.printStackTrace();
            }
        } catch (OutOfMemoryError ex) {
            Ln.e("out of memory while image rendering");
            if (Ln.DEBUG) {
                ex.printStackTrace();
            }
        } finally {
            StreamUtil.closeCloseable(baos);
            StreamUtil.closeCloseable(is);
        }
        return null;
    }

    public static Bitmap decodeFitBmpFromOS(ByteArrayOutputStream baos, BitmapFactory.Options opts, int degree, int fitWidth) {
        InputStream is = null;
        try {
            //TODO 图片过大可能导致 toByteArray() OOM
            is = new ByteArrayInputStream(baos.toByteArray());
            opts.inJustDecodeBounds = false;
            Bitmap srcBmp = BitmapFactory.decodeStream(is, null, opts);
            Bitmap retBmp = null;
            if (srcBmp != null) {
                retBmp = BitmapUtil.createRotateBmp(srcBmp, degree, false);
                retBmp = BitmapUtil.createFitWidthBmp(retBmp, fitWidth, false);
            }
            //如果处理后的图片非原地址引用则回收原图
            if (retBmp != srcBmp && !srcBmp.isRecycled()) {
                srcBmp.recycle();
            }
            return retBmp;
        } catch (Exception e) {
            if (Ln.DEBUG) {
                e.printStackTrace();
            }
        } catch (OutOfMemoryError ex) {
            Ln.e("out of memory while image rendering");
            if (Ln.DEBUG) {
                ex.printStackTrace();
            }
        } finally {
            StreamUtil.closeCloseable(baos);
            StreamUtil.closeCloseable(is);
        }
        return null;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels, boolean needRecycle) {
        if (bitmap == null) {
            return null;
        }
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        if (needRecycle) {
            bitmap.recycle();
        }
        return output;
    }


    public static BitmapFactory.Options getOrgImageOpts(ByteArrayOutputStream baos) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        //生成复用输入流
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.decodeStream(is, null, opts);
        StreamUtil.closeCloseable(is);
        return opts;
    }

    /**
     * Drawable 转 bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        // drawable转换成bitmap
        Bitmap oldbmp = drawable2Bitmap(drawable);
        // 创建操作图片用的Matrix对象
        Matrix matrix = new Matrix();
        // 计算缩放比例
        float sx = ((float) w / width);
        float sy = ((float) h / height);
        // 设置缩放比例
        matrix.postScale(sx, sy);
        // 建立新的bitmap，其内容是对原bitmap的缩放后的图
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
                matrix, true);
        return new BitmapDrawable(newbmp);
    }

    /**
     * Mutates and applies a filter that converts the given drawable to a Gray
     * image. This method may be used to simulate the color of disable icons in
     * Honeycomb's ActionBar.
     *
     * @return a mutated version of the given drawable with a color filter
     * applied.
     */
    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

    // 将Bitmap转换成InputStream
    public static InputStream Bitmap2InputStream(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    // 将Bitmap转换成InputStream
    public static InputStream Bitmap2InputStream(Bitmap bm, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static Bitmap getCroppedBitmap(Context context, Bitmap origin) {
        return getCroppedBitmap(context, origin, "#FFFFFF");
    }

    public static Bitmap getCroppedBitmap(Context context, Bitmap origin, String colorCode) {
        Bitmap bmp = origin.copy(Bitmap.Config.ARGB_8888, true);
        int dim = bmp.getWidth();
        Bitmap sbmp = bmp;
        float dstW = bmp.getWidth();
        float dstH = bmp.getHeight();

        float scaleRate = dim / dstW;
        if (dim / dstH > scaleRate) {
            scaleRate = dim / dstH;
        }

        sbmp = Bitmap.createScaledBitmap(bmp, (int) (dstW * scaleRate), (int) (dstH * scaleRate), false);

        int radius = dim / 2;
        Bitmap circleBitmap = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(circleBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor(colorCode));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);

        //This draw a circle of Whitecolor which will be the border of image.
        canvas.drawCircle(radius, radius, radius, paint);
        BitmapShader shader = new BitmapShader(sbmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        paint.setShader(shader);
        canvas.drawCircle(radius, radius, radius - Convert.dip2px(context, 2), paint);

        return circleBitmap;
    }



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

    public static BitmapSize getBitmapSize(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        return new BitmapSize(options.outWidth, options.outHeight);
    }

    public static BitmapSize getScaledSize(int originalWidth, int originalHeight, int numPixels) {
        float ratio = (float) originalWidth / originalHeight;

        int scaledHeight = (int) FloatMath.sqrt((float) numPixels / ratio);
        int scaledWidth = (int) (ratio * FloatMath.sqrt((float) numPixels / ratio));

        return new BitmapSize(scaledWidth, scaledHeight);
    }

    public static class BitmapSize {
        public int width;
        public int height;

        public BitmapSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    /**
     * 字符串转换成图片
     */
    public static Bitmap createBitmap(Context context, String str) {
        float desity = Convert.getScreenDensity(context);
        Paint paint = new Paint();
        paint.measureText(str);
        paint.setTextSize(desity * Constant.STICK_INIT_TEXT_SIZE);// 字体大小
        paint.setFakeBoldText(true);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSkewX(0);// 斜度
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        int h = Convert.dip2px(context, rect.height()) - (int) desity * 10;
        int w = (int) paint.measureText(str) + (int) desity * 5 + h;
        Bitmap bp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); // 画布大小
//       可能要对图片进行拉伸处理
//       bp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), bgRes), w, h,false); // 画布大小
        Canvas c = new Canvas(bp);
        //--------------绘制蒙层的背景start
//        Paint bgPaint = new Paint();
//        bgPaint.setColor(Color.parseColor(com.yovenny.stickview.Constant.STICK_TEXT_BG));
//        RectF oval = new RectF(0, 0, w, h);
//        c.drawRoundRect(oval, h, h, bgPaint);
//        c.drawRect(w-h, h/2, w, h, bgPaint);
        //-------------->绘制蒙层的背景end
        Rect targetRect = new Rect(0, 0, w, h);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = targetRect.top + (targetRect.bottom - targetRect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(str, targetRect.centerX(), baseline, paint);// 文字位置
        c.save(Canvas.ALL_SAVE_FLAG);// 保存
        c.restore();
        return bp;
    }


    /**
     * 字符串转换成图片
     */
    public static Bitmap createWarpRectBitmap(Context context, String str) {
        float desity = Convert.getScreenDensity(context);
        Paint paint = new Paint();
        paint.measureText(str);
        paint.setTextSize(desity * Constant.STICK_INIT_TEXT_SIZE);
        paint.setFakeBoldText(true);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSkewX(0);
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        String[] lines = str.split("\n");
        int itemH = Convert.dip2px(context, rect.height()) - (int) desity * 10;
        int h = (Convert.dip2px(context, rect.height()) - (int) desity * 10) * lines.length;
//        int itemH =rect.height();
//        int h = rect.height()* lines.length;
        int index = 0;
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i + 1].length() > lines[index].length()) {
                index = i + 1;
            }
        }

        int w = (int) paint.measureText(lines[index]) + (int) desity * 100;
        Bitmap bp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bp);
        int rectStartH = 0;
        for (int i = 0; i < lines.length; ++i) {
            Rect targetRect = new Rect((int) desity * 50, rectStartH, w, rectStartH + itemH);
            Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            int baseline = targetRect.top + (targetRect.bottom - targetRect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
            paint.setTextAlign(Paint.Align.LEFT);
            c.drawText(lines[i], targetRect.left, baseline, paint);
            rectStartH += itemH;
        }
        c.save(Canvas.ALL_SAVE_FLAG);
        c.restore();
        return bp;
    }

    /**
     * 字符串转换成图片
     */
    public static Bitmap createWarpBitmap(Context context, String str,int color) {
        float desity = Convert.getScreenDensity(context);
        Paint paint = new Paint();
        paint.measureText(str);
        paint.setTextSize(Constant.STICK_INIT_TEXT_SIZE*desity);
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
//            c.drawText(lines[i], desity*50, y + txtSize * (i+1)+lineSpace*i, paint);
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

    public static Bitmap zoomBitmap(Bitmap bgimage, int newWidth, int newHeight, boolean isFilter) {
        // 获取这个图片的宽和高
        int width = bgimage.getWidth();
        int height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算缩放率，新尺寸除原始尺寸
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, width, height, matrix, isFilter);
        bgimage.recycle();
        return bitmap;
//        Bitmap bm=((BitmapDrawable)this.getDrawable()).getBitmap();
    }

    public static Bitmap zoomBitmap(Bitmap bgimage, int newWidth, int newHeight) {
        return zoomBitmap(bgimage, newWidth, newHeight, true);
    }

    /**
     * 质量压缩方式
     */

    private static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 10) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 2;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public static Bitmap getBitmap(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    //以下图片压缩放
    public static Bitmap getThumbBitmap(File file) {
        ByteArrayOutputStream baos = StreamUtil.getByteArrayOSFromFile(file);
        // 保存原始参数
        BitmapFactory.Options orgOpts = BitmapUtil.getOrgImageOpts(baos);

        orgOpts.inSampleSize = BitmapUtil.calculateInSampleSize(orgOpts, 1080, 1);
        // Log.d("outWidth:" + opts.outWidth + " outHeight:" + opts.outHeight + " sampleSize:" + opts.inSampleSize);
        Bitmap scaledBmp = BitmapUtil.decodeBmpFromOS(baos, orgOpts, 0, 1080);
        return scaledBmp;
    }


}

