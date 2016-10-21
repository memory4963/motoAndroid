package com.bolo4963gmail.motoandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.bolo4963gmail.motoandroid.javaClass.ThisDatabaseHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link Setting1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Setting1Fragment extends Fragment {

    private static final String ARG_PAGE = "page";

    private int mPage;

    @BindView(R.id.switch1) Switch switchSeccess;
    @BindView(R.id.switch2) Switch switchFailure;

    public Setting1Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment Setting1Fragment.
     */
    public static Setting1Fragment newInstance(int param1) {
        Setting1Fragment fragment = new Setting1Fragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting1, container, false);
        ButterKnife.bind(this, view);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }

        /**
         * set the switch's checked thing
         */
        final SharedPreferences sharedPreferences = getActivity().
                getSharedPreferences(ThisDatabaseHelper.SWITCH_SHARED_PREFERENCES,
                                     Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(ThisDatabaseHelper.BUILD_SUCCESS, true)) {
            switchSeccess.setChecked(true);
        } else {
            switchSeccess.setChecked(false);
        }
        if (sharedPreferences.getBoolean(ThisDatabaseHelper.BUILD_FAILURE, true)) {
            switchFailure.setChecked(true);
        } else {
            switchFailure.setChecked(false);
        }

        switchSeccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ThisDatabaseHelper.BUILD_SUCCESS, true);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ThisDatabaseHelper.BUILD_SUCCESS, false);
                    editor.apply();
                }

                SettingsActivity activity = (SettingsActivity) getActivity();
                activity.setIfRefresh(true);
            }
        });

        switchFailure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ThisDatabaseHelper.BUILD_FAILURE, true);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ThisDatabaseHelper.BUILD_FAILURE, false);
                    editor.apply();
                }

                SettingsActivity activity = (SettingsActivity) getActivity();
                activity.setIfRefresh(true);
            }
        });
        return view;
    }

}
