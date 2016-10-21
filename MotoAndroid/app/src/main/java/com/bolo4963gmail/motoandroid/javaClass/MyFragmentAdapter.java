package com.bolo4963gmail.motoandroid.javaClass;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bolo4963gmail.motoandroid.Setting1Fragment;
import com.bolo4963gmail.motoandroid.Setting2Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10733 on 2016/10/10.
 */
public class MyFragmentAdapter extends FragmentPagerAdapter {

    private static final String TAG = "MyFragmentAdapter";

    private final int PAGE_QUANTITY = 2;

    private Context mContext;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private String[] mTabTitle = {"消息设置", "项目删减"};

    public MyFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        mFragmentList.add(Setting1Fragment.newInstance(1));
        mFragmentList.add(Setting2Fragment.newInstance(2));

    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return PAGE_QUANTITY;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitle[position];
    }

}
