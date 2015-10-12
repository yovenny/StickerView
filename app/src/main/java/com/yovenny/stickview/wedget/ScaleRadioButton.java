package com.yovenny.stickview.wedget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class ScaleRadioButton extends RadioButton {
    public static final int NONE_DOT_STYLE = 0;
    public static final int CORNER_DOT_STYLE = 1;
    public static final int YELLOW_DOT_STYLE = 2;

    private int mCurrStyle = 0;

    public ScaleRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleRadioButton(Context context) {
        super(context);
    }

    public void setRedDotStyle(int style) {
        if (style != mCurrStyle) {
            mCurrStyle = style;
            invalidate();
        }
    }

    public int getCurrDotStyle () {
        return mCurrStyle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        String text = getText().toString();
        int totalHeight = getHeight();
        int totalWidth = getWidth();
        float textHeight = 0;
        float textWidth = 0;
        int drawablePadding = getCompoundDrawablePadding();
        int bottomPadding = getPaddingBottom();
        int drawStartX;
        int drawStartY = getPaddingTop();

        int textStartX = 0;
        int textStartY = getPaddingTop();

        Paint paint = getPaint();
        if (!TextUtils.isEmpty(text)) {
            textHeight = paint.descent() - paint.ascent();
            textWidth = paint.measureText(text);
        }

        Drawable[] drs = getCompoundDrawables();
        Drawable drawableTop;
        Drawable drawableLeft;

        if (drs != null && drs.length > 0 && drs[0] != null) {
            drawableLeft = drs[0];
            drawableLeft.setState(getDrawableState());
            int remainH = totalHeight - drawStartY - bottomPadding;
            BoundScale topBound = computeDrawableScale(drawableLeft, remainH, 0);
            drawStartX = (int)((totalWidth - topBound.scaleW - drawablePadding - textWidth) / 2);
            drawableLeft.setBounds(drawStartX, drawStartY, drawStartX + topBound.scaleW, drawStartY + topBound.scaleH);
            drawableLeft.draw(canvas);
            textStartX += drawStartX + topBound.scaleW + drawablePadding;
        }

        if (drs != null && drs.length > 1 && drs[1] != null) {
            drawableTop = drs[1];
            drawableTop.setState(getDrawableState());
            int remainH = (int)(totalHeight - drawStartY - drawablePadding - textHeight - bottomPadding);
            BoundScale topBound = computeDrawableScale(drawableTop, remainH, 0);
            drawStartX = (totalWidth - topBound.scaleW) / 2;
            drawableTop.setBounds(drawStartX, drawStartY, drawStartX + topBound.scaleW, drawStartY + topBound.scaleH);
            drawableTop.draw(canvas);
            textStartY += drawStartY + topBound.scaleH + drawablePadding;
            textStartX += (int)((totalWidth - textWidth) /2);
        }

        if (!TextUtils.isEmpty(text)) {
            paint.setColor(getCurrentTextColor());
            textStartY += (int)((totalHeight - textStartY - textHeight - bottomPadding) / 2) - paint.ascent();
            if(drs[0] == null && drs[1] == null && drs[2] == null && drs[3] == null) {
                textStartX += (int)((totalWidth - textWidth) / 2);
            }
            canvas.drawText(text, textStartX, textStartY, paint);
        }

        if (mCurrStyle == CORNER_DOT_STYLE) {
            float radius;
            paint.setColor(Color.parseColor("#fe0000"));
            radius = textHeight / 5f;
            int circleStartY = (int)(((totalHeight - textStartY - textHeight - bottomPadding) / 2) - paint.ascent());
            canvas.drawCircle(textStartX + textWidth + radius + 10, circleStartY, radius, paint);
        } else if(mCurrStyle == YELLOW_DOT_STYLE) {
            float radius;
            paint.setColor(Color.parseColor("#ffec91"));
            radius = textHeight / 5f;
            canvas.drawCircle(textStartX + textWidth + radius + 10, (totalHeight - radius) / 2, radius, paint);
        }

        canvas.restore();
    }

    private BoundScale computeDrawableScale (Drawable srcDrawable, int maxHeight, int hBalance) {
        BoundScale bp =  new BoundScale();
        bp.orgH = srcDrawable.getIntrinsicHeight();
        bp.orgW = srcDrawable.getIntrinsicWidth();
        bp.scaleH = Math.min(bp.orgH, maxHeight + hBalance);
        bp.scaleW = (int)(((float)bp.scaleH / bp.orgH) * bp.orgW);
        return bp;
    }


    public class BoundScale {
        int scaleH;
        int scaleW;
        int orgH;
        int orgW;
    }
}
