package com.bolo4963gmail.motoandroid.javaClass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bolo4963gmail.motoandroid.R;
import com.bolo4963gmail.motoandroid.Setting2Fragment;
import com.bolo4963gmail.motoandroid.SettingsActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by 10733 on 2016/10/13.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private LayoutInflater mInflater;
    private SettingsActivity mActivity;
    private Setting2Fragment mFragment;
    private List<Map<String, Object>> mList;

    public MyRecyclerViewAdapter(SettingsActivity activity,
                                 Setting2Fragment fragment,
                                 List<Map<String, Object>> list) {
        mActivity = activity;
        mFragment = fragment;
        mList = list;
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.setting_recycler_view, parent, false);
        return new MyViewHolder(view, mActivity, mFragment);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.projectTv.setText((String) mList.get(position).get("list_project"));
        holder.serverTv.setText((String) mList.get(position).get("list_server"));

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView projectTv;
    public TextView serverTv;
    public Button deleteButton;
    public CheckBox checkBox;

    private AlertDialog alertDialog;
    private SettingsActivity mActivity;
    private Setting2Fragment mFragment;

    private SQLiteDatabase db;

    public MyViewHolder(View itemView, SettingsActivity activity, Setting2Fragment fragment) {
        super(itemView);

        mActivity = activity;
        mFragment = fragment;

        db = ThisDatabaseHelper.getDatabaseHelper().getWritableDatabase();

        projectTv = (TextView) itemView.findViewById(R.id.recycler_project);
        serverTv = (TextView) itemView.findViewById(R.id.recycler_server);
        deleteButton = (Button) itemView.findViewById(R.id.recycler_delete_button);
        checkBox = (CheckBox) itemView.findViewById(R.id.recycler_check_box);

        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog = new AlertDialog.Builder(mActivity).setCancelable(true)
                        .setTitle("确认")
                        .setMessage("确认删除？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cursor serverCursor = db.rawQuery(
                                        "select * from " + ThisDatabaseHelper.SERVER_NAMES_TABLE
                                                + " where server = ?",
                                        new String[]{serverTv.getText().toString()});
                                if (serverCursor.moveToFirst()) {
                                    Integer serverId =
                                            serverCursor.getInt(serverCursor.getColumnIndex("id"));
                                    Cursor projectCursor = db.rawQuery("select * from "
                                                                               + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                                                                               + " where serverId = ? and project = ?",
                                                                       new String[]{
                                                                               serverId.toString(),
                                                                               projectTv.getText().toString()
                                                                       });
                                    if (projectCursor.moveToFirst()) {
                                        Integer projectId = projectCursor.getInt(
                                                projectCursor.getColumnIndex("id"));
                                        db.execSQL("delete from " + ThisDatabaseHelper.RESULT_TABLE
                                                           + " where projectId = ?",
                                                   new String[]{projectId.toString()});
                                    }
                                    projectCursor.close();
                                    db.execSQL(
                                            "delete from " + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                                                    + " where serverId = ? and project = ?",
                                            new String[]{
                                                    serverId.toString(),
                                                    projectTv.getText().toString()
                                            });

                                    projectCursor = db.rawQuery("select * from "
                                                                        + ThisDatabaseHelper.PROJECT_NAMES_TABLE
                                                                        + " where serverId = ?",
                                                                new String[]{serverId.toString()});
                                    if (!projectCursor.moveToFirst()) {
                                        db.execSQL("delete from "
                                                           + ThisDatabaseHelper.SERVER_NAMES_TABLE
                                                           + " where server = ?",
                                                   new String[]{serverTv.getText().toString()});
                                    }
                                    projectCursor.close();
                                }
                                serverCursor.close();
                                mActivity.setIfRestart(true);
                                mFragment.reloadDatas(serverTv.getText().toString(),
                                                      projectTv.getText().toString());
                                alertDialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

}
