package com.duzhaokun123.bilibilihd.ui.article;

import android.annotation.SuppressLint;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;

import com.duzhaokun123.bilibilihd.Params;
import com.duzhaokun123.bilibilihd.R;
import com.duzhaokun123.bilibilihd.databinding.ActivityArticleBinding;
import com.duzhaokun123.bilibilihd.bases.BaseActivity;
import com.duzhaokun123.bilibilihd.utils.BrowserUtil;
import com.duzhaokun123.bilibilihd.utils.MyBilibiliClientUtil;
import com.duzhaokun123.bilibilihd.utils.ShareUtil;

public class ArticleActivity extends BaseActivity<ActivityArticleBinding> {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_in_browser:
                BrowserUtil.openCustomTab(this, MyBilibiliClientUtil.getCvUrl(getStartIntent().getLongExtra("id", 0)));
                return true;
            case R.id.share:
                ShareUtil.INSTANCE.shareText(this, MyBilibiliClientUtil.getCvUrl(getStartIntent().getLongExtra("id", 0)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected int initConfig() {
        return 0;
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_article;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initView() {
        baseBind.wv.getSettings().setJavaScriptEnabled(true);
        baseBind.wv.getSettings().setBlockNetworkImage(false);
        baseBind.wv.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        baseBind.wv.getSettings().setUserAgentString(Params.DESKTOP_USER_AGENT);
        baseBind.wv.getSettings().setDomStorageEnabled(true);
    }

    @Override
    protected void initData() {
        baseBind.wv.loadUrl("https://www.bilibili.com/read/cv" + getStartIntent().getLongExtra("id", 0));
    }
}
