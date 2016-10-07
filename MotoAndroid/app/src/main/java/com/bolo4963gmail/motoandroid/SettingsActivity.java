package com.bolo4963gmail.motoandroid;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by 10733 on 2016/8/4.
 */
public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    /**
     * 设置逻辑
     * 点击MainActivity中menu的setting按钮启动此活动
     * 用tabLayout显示 一个为设置接收的条目 一个为删除项目
     * 从数据库中提取所有的服务器和项目（使用自定义的Adapter显示在listView上）
     * 点击每个的删除按钮的时候弹出警告框 点击确定则清空数据库中关于这个项目的所有数据
     * 点多选时由按钮变成复选框多选变成取消 当有复选框被选中的时候编程删除
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



    }



}
