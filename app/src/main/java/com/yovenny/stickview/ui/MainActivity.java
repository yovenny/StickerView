package com.yovenny.stickview.ui;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

import com.yovenny.stickview.R;
import com.yovenny.stickview.base.BaseActivity;
import com.yovenny.stickview.util.FileUtil;
import com.yovenny.stickview.util.MediaUtil;
import com.yovenny.stickview.util.UIHelper;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if(id==R.id.action_clear){
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri mFullCameraUri;
    public static final int ALBUM_REQUEST_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 1337;

    @OnClick(R.id.camera)
    public void camera() {
        onPhotoChoose();
    }

    @OnClick(R.id.album)
    public void album() {
        onAlblumChoose();
    }

    private void onAlblumChoose() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ALBUM_REQUEST_CODE);
    }

    private void onPhotoChoose() {
        Intent intent;
        rebuildCameraUri();
        if (mFullCameraUri == null) {
            return;
        }
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mFullCameraUri);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }


    private void rebuildCameraUri() {
        try {
            mFullCameraUri = Uri.fromFile(FileUtil.createTempFile("camtmp", ".jpg"));
        } catch (Exception e) {
            po("Please check SD card! Image shot is impossible!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri processImgUri;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    UIHelper.showWaterActivity(this, mFullCameraUri.getPath());
                    break;
                case ALBUM_REQUEST_CODE:
                    processImgUri = data.getData();
                    final String photoPath = MediaUtil.getKitkatPath(this, processImgUri);
                    if (processImgUri != null) {
                        UIHelper.showWaterActivity(this, photoPath);
                    } else {
                        po("获取图片失败");
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
