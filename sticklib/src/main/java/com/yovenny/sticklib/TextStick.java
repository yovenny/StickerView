package com.yovenny.sticklib;

import android.graphics.Bitmap;

//文字标签
public class TextStick extends Stick {
    public String content;

    public TextStick(Bitmap stickBitmap, String content) {
        super(stickBitmap);
        this.content = content;
    }
}