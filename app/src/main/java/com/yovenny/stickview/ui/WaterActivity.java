/**
 * Summary: 水印处理的页面
 */

package com.yovenny.stickview.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yovenny.stickview.Constant;
import com.yovenny.stickview.R;
import com.yovenny.stickview.StickApp;
import com.yovenny.stickview.adapter.WaterAdapter;
import com.yovenny.stickview.base.BaseActivity;
import com.yovenny.stickview.db.DBManager;
import com.yovenny.stickview.model.WaterMarkCategory;
import com.yovenny.stickview.model.WaterMarkItem;
import com.yovenny.stickview.util.BitmapUtil;
import com.yovenny.stickview.util.Convert;
import com.yovenny.stickview.util.Global;
import com.yovenny.stickview.util.MediaUtils;
import com.yovenny.stickview.util.UIHelper;
import com.yovenny.stickview.wedget.ScaleRadioButton;
import com.yovenny.stickview.wedget.TabBarView;
import com.yovenny.stickview.wedget.sticker.ImgStick;
import com.yovenny.stickview.wedget.sticker.Stick;
import com.yovenny.stickview.wedget.sticker.StickerSeriesView;
import com.yovenny.stickview.wedget.sticker.TextStick;

import java.io.File;
import java.util.List;

public class WaterActivity extends BaseActivity implements View.OnClickListener {
    public static final int PHOTO_RESULT = 1111;
    public static final String ADD_TOPIC_PIC = "ADD_TOPIC_PIC";
    private static final String SAVE_STATE_PATH = "com.yovenny.stickview.ui.WaterActivity.mOriginalPhotoPath";
    private static final String SAVE_CAMERA_FILE_PATH = "com.yovenny.stickview.ui.WaterActivity.mCurrentCameraFilePath";
    private static final String TAG = "Knife";

    private String mOriginalPhotoPath = null;
    private Bitmap mBitmap = null;
    private ImageView mImageView = null;
    private static Handler mHandle = new Handler();

    private TabBarView mTab;
    private WaterAdapter mWaterAdapter;
    private StickerSeriesView mSticker;
    RecyclerView mRecyclerView;

    private ImageView mTextImage;
    private ImageView mWaterTipImage;
    private TextView mWaterMarkRadio;
    private RelativeLayout mTextRelative;

    private boolean isNeedResult;
    private int mLastCheck;

    //文字输入相关
    private ImageView mInputCancelImage;
    private ImageView mInputConfirmImage;
    private EditText mInputEdit;

    @Override
    public void initView() {
        initActionBar();
        initUI();
        initData();
        initWaterMarkUI();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_water;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#1c211c"));
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setDisplayShowTitleEnabled(false);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_process_top_go_back);
        return super.onPrepareOptionsMenu(menu);
    }



    public void initUI() {
        mImageView = (ImageView) findViewById(R.id.imageViewPhoto);
        mSticker = (StickerSeriesView) findViewById(R.id.process_sticker);
        mWaterTipImage = (ImageView) findViewById(R.id.process_water_tip_up);
        mWaterMarkRadio = (TextView) findViewById(R.id.watermark_radio);
        mTextImage = (ImageView) findViewById(R.id.text_image);
        mTab = (TabBarView) findViewById(R.id.tabs);
        mWaterMarkRadio.setOnClickListener(this);
        mTextRelative = (RelativeLayout) findViewById(R.id.text_relative);

        mInputCancelImage= (ImageView) findViewById(R.id.cancel_image);
        mInputConfirmImage= (ImageView) findViewById(R.id.confirm_image);
        mInputEdit= (EditText) findViewById(R.id.gather_edit);

        mRecyclerView = (RecyclerView) findViewById(R.id.watermark_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mTextImage.setOnClickListener(this);
        mInputCancelImage.setOnClickListener(this);
        mInputConfirmImage.setOnClickListener(this);
        mSticker.setOnStickDelListener(new StickerSeriesView.OnStickDelListener() {
            @Override
            public void onStickDel(int categoryId, int postion) {
                mWaterAdapter.removeItemCheck(categoryId, postion);
            }
        });
        mSticker.setOnStickTextDelListener(new StickerSeriesView.OnStickTextDelListener() {
            @Override
            public void onStickTextDel() {
//                mGatherTextCheck.setChecked(false, false);
            }
        });

        //获取长宽
        final ViewTreeObserver observer = mSticker.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSticker.setBgWidthHeight(mSticker.getMeasuredWidth(), mSticker.getMeasuredHeight());
                mSticker.setmBgBitmap(mBitmap);
            }
        });
    }

    private void initWaterMarkUI() {
        //获取水印的分类
        mTab.setRadioGroupPadding(0, 0, 0, 0);
        mTab.setScreenWidth(StickApp.sWidthPix);
        mTab.setTabHeight(R.dimen.process_bottom_height);
        mTab.hideTabIndicator();
        DBManager dbManager = new DBManager(this);
        final List<WaterMarkCategory> categoryList = dbManager.getWaterCategoryDAO().findValid();

        if (categoryList.size() > 0) {
//            final List nextXs = new ArrayList();
            for (WaterMarkCategory waterMarkCategory : categoryList) {
                waterMarkCategory.setWaterMarkItems(dbManager.getWaterItemDAO().findValid(waterMarkCategory.getCid()));
                mTab.addOrigin(createRadioButton(waterMarkCategory.getName()));
//                nextXs.add(0);
            }
            if (categoryList.size() <= 6) {
                mTab.limitInScreen(true);
            }
            mWaterAdapter = new WaterAdapter(this, null);
            mRecyclerView.setAdapter(mWaterAdapter);
            mTab.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    //更改水印的适配器
//                    nextXs.set(mLastCheck, mRecyclerView.getpo());
                    final int index = group.indexOfChild(group.findViewById(checkedId));
                    mLastCheck = index;
                    final WaterMarkCategory tempCategory = categoryList.get(index);
                    mWaterAdapter.notifyDataSetChange(tempCategory.waterMarkItems);
                }
            });
            mTab.setCheckAt(0);
            mWaterAdapter.setOnItemClickListener(new WaterAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    WaterMarkItem item = (WaterMarkItem) mWaterAdapter.getItem(position);
                    int check = mWaterAdapter.checkContain(item.getCategoryId(), position);
                    if (check < 0) {
                        onStickSelect(position, item);
                    } else {
                        mWaterAdapter.removeItemCheck(item.getCategoryId(), position);
                        mSticker.delStick(item.getCategoryId(), position);
                    }
                }
            });
        } else {
            mTab.addOrigin(createRadioButton(getString(R.string.app_name)));
        }
    }

    private RadioButton createRadioButton(String text) {
        ScaleRadioButton newRadioBtn = new ScaleRadioButton(this);
        newRadioBtn.setTextColor(getResources().getColorStateList(R.color.process_bottom_tab_text_colors));
        newRadioBtn.setBackgroundResource(R.drawable.process_bottom_selector);
        newRadioBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.process_title_text_size));
        newRadioBtn.setSingleLine();
        newRadioBtn.setMaxEms(8);
        newRadioBtn.setText(text);
        return newRadioBtn;
    }

    private void onStickSelect(int position, WaterMarkItem item) {
        if (mSticker.getStickCount() >= StickerSeriesView.STICK_IMG_MAX_COUNT) {
            po(getString(R.string.process_stick_max));
            return;
        }
        mWaterAdapter.addItemCheck(item.getCategoryId(), position);
        int widthPx = Convert.dip2px(this, Constant.UPLOAD_IMAGE_MAX_WIDTH);
        Bitmap sampleBitamp = BitmapUtil.decodeBmpFromFile(new File(item.getSavePath()), widthPx, widthPx, false);
        if (sampleBitamp == null) {
            po("加载水印失败");
        } else {
            Stick stick = new ImgStick(sampleBitamp, item.getCategoryId(), item.getWid(), position);
            mSticker.setStick(stick);
        }
    }

    private void initData() {
        isNeedResult = getIntent().getBooleanExtra("need_result", false);
        String processPath = getIntent().getStringExtra("process_path");
        if (processPath != null) {
            onImgUri(processPath);
        }
    }


    private void onImgUri(String processPath) {
        mImageView.setImageBitmap(null);
        mOriginalPhotoPath = processPath;
        loadPhoto(mOriginalPhotoPath);
        mImageView.setImageBitmap(mBitmap);
    }


    @Override
    protected void onPause() {
        hideWaitDialog();
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
        setIntent(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_confirm) {
            //保存图片，跳到发表图片界面
            onStickSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPhoto(String path) {
        if (mBitmap != null) {
            mBitmap.recycle();
        }

//TODO 图片压缩方式一
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        mBitmap = BitmapUtil.getSampledBitmap(path, displayMetrics.widthPixels, displayMetrics.heightPixels);

//TODO 图片压缩方式二
        mBitmap = BitmapUtil.getThumbBitmap(new File(path));

//TODO 图片压缩方式三
//        mBitmap = BitmapUtil.getBitmap(path);
        int angle = MediaUtils.getExifOrientation(path);
        if (mBitmap == null) {
            mHandle.post(new Runnable() {
                @Override
                public void run() {
                    po("加载图片失败");
                }
            });
            return;
        }

    }

    private void showTempPhotoInImageView() {
        if (mBitmap != null) {
            Bitmap bitmap = Bitmap.createScaledBitmap(mBitmap, mBitmap.getWidth() / 4, mBitmap.getHeight() / 4, true);
            mImageView.setImageBitmap(bitmap);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_image:
                toogleModifyText();
                break;
            case R.id.confirm_image:
                String gatherText = mInputEdit.getText().toString();
                if (TextUtils.isEmpty(gatherText)) {
                    po("文字不能为空");
                    return;
                } else {
                    Global.popSoftkeyboard(WaterActivity.this, mInputEdit, false);
                    toogleModifyText();
                    Bitmap tempTextBitmap = BitmapUtil.createWarpBitmap(WaterActivity.this, gatherText,Color.BLACK);
                    Stick stick=new TextStick(tempTextBitmap,gatherText);
                    mSticker.setStick(stick);
                }
                break;
            case R.id.watermark_radio:
                break;
            case R.id.text_image:
                if (mSticker.getTextCount() >= StickerSeriesView.STICK_TEXT_MAX_COUNT) {
                    po(getString(R.string.process_text_max));
                    return;
                }
                toogleModifyText();
                break;
        }
    }

    private void toogleModifyText() {
        if (mTextRelative.getVisibility() == View.VISIBLE) {
            mTextRelative.setVisibility(View.GONE);
            mInputEdit.clearFocus();
            mInputEdit.setText("");
        } else {
            mTextRelative.setVisibility(View.VISIBLE);
        }
    }


    private void onStickSave() {
        showWaitDialog();
        mSticker.createFinalBitmap(mOriginalPhotoPath, new StickerSeriesView.OnSaveResultListener() {
            @Override
            public void onSaveResult(String saveFile) {
               hideWaitDialog();
                if (isNeedResult) {
                    Intent intent = new Intent();
                    intent.putExtra(ADD_TOPIC_PIC, saveFile);
                    intent.putExtra("watermarkCategoryIds", mSticker.getStickCategoryIds());
                    intent.putExtra("watermarkIds", mSticker.getStickIds());
                    intent.putExtra("contents", mSticker.getTextContents());
                    setResult(PHOTO_RESULT, intent);
                } else {
                    UIHelper.showResultActivity(WaterActivity.this, saveFile, mSticker.getStickCategoryIds(), mSticker.getStickIds(), mSticker.getTextContents());
                }
                mSticker.destory();
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_STATE_PATH, mOriginalPhotoPath);
        outState.putString(SAVE_CAMERA_FILE_PATH, mOriginalPhotoPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mOriginalPhotoPath = savedInstanceState.getString(SAVE_STATE_PATH);
        String currentCameraFilePath = savedInstanceState.getString(SAVE_CAMERA_FILE_PATH);
        if (currentCameraFilePath != null) {
            mOriginalPhotoPath = currentCameraFilePath;
        }
        if (mOriginalPhotoPath != null) {
            loadFromCache();
            mImageView.setImageBitmap(mBitmap);
        }
    }

    private void loadFromCache() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        File cacheFile = new File(getCacheDir(), "cached.jpg");
        mBitmap = BitmapUtil.getSampledBitmap(cacheFile.getAbsolutePath(), displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

}