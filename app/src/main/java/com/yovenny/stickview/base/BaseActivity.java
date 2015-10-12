package com.yovenny.stickview.base;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.yovenny.stickview.interf.BaseView;

import butterknife.ButterKnife;

/**
 * Desc：
 * User: fenzaichui (1312397605@qq.com)
 * Date:2015/10/12.
 * Copyright: yovenny.com
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseView{
    protected ActionBar mActionBar;

    protected void po(int res){
        po(getString(res));
    }
    protected void po(String res){
        Toast.makeText(BaseActivity.this,res,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onBeforeSetContentLayout();
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        mActionBar = getSupportActionBar();
        if (hasActionBar()) {
            initActionBar(mActionBar);
        }
        ButterKnife.inject(this);
        init(savedInstanceState);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    protected void onBeforeSetContentLayout() {}

    protected boolean hasActionBar() {
        return true;
    }

    protected int getLayoutId() {
        return 0;
    }

    protected boolean hasBackButton() {
        return false;
    }

    protected void init(Bundle savedInstanceState) {}

    protected void initActionBar(ActionBar actionBar) {
        if (actionBar == null)
            return;
        if (hasBackButton()) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
