package com.bolo4963gmail.motoandroid;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bolo4963gmail.motoandroid.javaClass.MyRecyclerViewAdapter;
import com.bolo4963gmail.motoandroid.javaClass.ThisDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link Setting2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Setting2Fragment extends Fragment {

    private static final String TAG = "Setting2Fragment";

    private static final String ARG_PAGE = "page";

    SQLiteDatabase db;

    @BindView(R.id.setting_delete_recyclerView) RecyclerView recyclerView;

    List<Map<String, Object>> mList;
    MyRecyclerViewAdapter adapter;

    private int mPage;

    public Setting2Fragment() {
        // Required empty public constructor
        ThisDatabaseHelper dbHelper = ThisDatabaseHelper.getDatabaseHelper();
        db = dbHelper.getWritableDatabase();
    }

    public static Setting2Fragment newInstance(int param1) {
        Setting2Fragment fragment = new Setting2Fragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting2, container, false);
        ButterKnife.bind(this, view);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }

        initDatas();

        if (mList.size() < 1) {
            Toast.makeText(getActivity(), "数据库无数据", Toast.LENGTH_SHORT).show();
            return view;
        }

        adapter = new MyRecyclerViewAdapter((SettingsActivity) getActivity(), this, mList);
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "onCreateView: 绑定了recyclerView的adapter");

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    private void initDatas() {

        mList = new ArrayList<>();

        Cursor serverCursor = db.rawQuery("select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE,
                                          new String[]{});
        if (serverCursor.moveToFirst()) {
            do {
                Integer serverId = serverCursor.getInt(serverCursor.getColumnIndex("id"));
                Log.d(TAG, "initDatas: serverId = " + serverId.toString());
                Cursor projectCursor = db.rawQuery(
                        "select * from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                                + " where serverId = ?", new String[]{serverId.toString()});
                if (projectCursor.moveToFirst()) {
                    do {
                        Map<String, Object> map = new HashMap<>();
                        map.put("list_project",
                                projectCursor.getString(projectCursor.getColumnIndex("project")));
                        map.put("list_server",
                                serverCursor.getString(serverCursor.getColumnIndex("server")));
                        mList.add(map);
                    } while (projectCursor.moveToNext());
                }
                projectCursor.close();
            } while (serverCursor.moveToNext());
            serverCursor.close();
        }
    }

    public void reloadDatas(String server, String project) {
        for (Map<String, Object> map : mList) {
            if (map.get("list_server").equals(server) && map.get("list_project").equals(project)) {
                mList.remove(map);
                adapter = new MyRecyclerViewAdapter((SettingsActivity) getActivity(), this, mList);
                recyclerView.setAdapter(adapter);
                break;
            }
        }
    }

}