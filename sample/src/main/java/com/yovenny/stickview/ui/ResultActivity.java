package com.yovenny.stickview.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yovenny.stickview.R;
import com.yovenny.stickview.StickApp;
import com.yovenny.stickview.base.BaseActivity;
import com.yovenny.stickview.util.DensityUtil;

import java.util.List;

import butterknife.InjectView;

/**
 * Desc：
 * User: fenzaichui (1312397605@qq.com)
 * Date:2015/10/13.
 * Copyright: yovenny.com
 */
public class ResultActivity extends BaseActivity {

    private Bitmap mUploadBmp;

    @InjectView(R.id.result_linear)
    LinearLayout mResultLinear;

    @InjectView(R.id.result_image)
    ImageView mResultImage;

    @Override
    public void initView() {
        handleIntent(getIntent());
        initResult();
    }
    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_result;
    }

    private void initResult() {
        String savePath = getIntent().getStringExtra(WaterActivity.ADD_TOPIC_PIC);
        if (savePath != null && !TextUtils.isEmpty(savePath)) {
            Bitmap tempBitmap = BitmapFactory.decodeFile(savePath);
            updateHeadBmp(tempBitmap);
        }
    }

    private void updateHeadBmp(Bitmap newBmp) {
        if (newBmp == null) {
            return;
        }
        if (mUploadBmp != null && !mUploadBmp.isRecycled()) {
            mUploadBmp.recycle();
            mUploadBmp = null;
            System.gc();
        }
        mUploadBmp = newBmp;
        //按比例展示图片
        int photoFixWidth = StickApp.sWidthPix - DensityUtil.dip2px(this, 18 * 2);
        float height = mUploadBmp.getHeight();
        float width = mUploadBmp.getWidth();
        float scale = height / width;
        int displayHeight = (int) (photoFixWidth * scale);
        ViewGroup.LayoutParams params = mResultLinear.getLayoutParams();
        params.height = displayHeight;
        mResultImage.setImageBitmap(mUploadBmp);
    }

    private void handleIntent(Intent intent) {
        //水印对象
        if (intent.hasExtra("watermarkCategoryIds") && intent.hasExtra("watermarkIds") && intent.hasExtra("contents")) {
            List categoryIds = intent.getIntegerArrayListExtra("watermarkCategoryIds");
            List ids = intent.getIntegerArrayListExtra("watermarkIds");
            List contents = intent.getStringArrayListExtra("contents");
        }
    }
}
