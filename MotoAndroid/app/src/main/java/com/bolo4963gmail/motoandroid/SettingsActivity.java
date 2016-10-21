package com.bolo4963gmail.motoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bolo4963gmail.motoandroid.javaClass.MyFragmentAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import luckyandyzhang.github.io.statusbarcompat.StatusBarCompat;

/**
 * Created by 10733 on 2016/8/4.
 */
public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    public static final String REFRESH = "refresh";
    public static final String RESTART = "restart";

    private boolean ifRefresh = false;
    private boolean ifRestart = false;

    private MyFragmentAdapter fragmentAdapter;

    @BindView(R.id.setting_toolbar) Toolbar toolbar;
    @BindView(R.id.setting_appBarLayout) AppBarLayout appBarLayout;
    @BindView(R.id.setting_tab_layout) TabLayout tabLayout;
    @BindView(R.id.setting_view_pager) ViewPager viewPager;

    /**
     * 设置逻辑
     * 点击MainActivity中menu的setting按钮启动此活动
     * 用tabLayout显示 一个为设置接收的条目 一个为删除项目
     * 从数据库中提取所有的服务器和项目（使用自定义的Adapter显示在listView上）
     * 点击每个的删除按钮的时候弹出警告框 点击确定则清空数据库中关于这个项目的所有数据
     * 点多选时由按钮变成复选框多选变成取消 当有复选框被选中的时候编程删除
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // TODO: 2016/10/10 更改每个活动的statusBar
        StatusBarCompat.init(this, R.color.status_bar_color);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("设置");

        fragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(REFRESH, ifRefresh);
        intent.putExtra(RESTART, ifRestart);
        setResult(RESULT_OK, intent);

        super.onBackPressed();

        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra(REFRESH, ifRefresh);
                intent.putExtra(RESTART, ifRestart);
                setResult(RESULT_OK, intent);
                finish();
                return true;
                default:
                    break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setIfRefresh(boolean ifRefresh) {
        this.ifRefresh = ifRefresh;
    }

    public void setIfRestart(boolean ifRestart) {
        this.ifRestart = ifRestart;
    }
}
