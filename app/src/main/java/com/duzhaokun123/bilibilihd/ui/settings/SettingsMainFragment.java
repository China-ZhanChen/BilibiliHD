package com.duzhaokun123.bilibilihd.ui.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.duzhaokun123.bilibilihd.R;
import com.duzhaokun123.bilibilihd.pBilibiliApi.api.PBilibiliClient;
import com.google.android.material.navigation.NavigationView;

import java.io.File;

public class SettingsMainFragment extends Fragment {

    private NavigationView mNavSettingsMain;

    private Fragment mFragmentSettingsDevelop;

    private PBilibiliClient pBilibiliClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_main, container, false);

        if (mFragmentSettingsDevelop == null) {
            mFragmentSettingsDevelop = new SettingsDevelopFragment();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavSettingsMain = view.findViewById(R.id.nav_settings_main);
        pBilibiliClient = PBilibiliClient.Companion.getPBilibiliClient();
        mNavSettingsMain.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.develop:
                        if (((SettingsActivity)getActivity()).getmFlSettingSecond() == null){
                            getActivity().getSupportFragmentManager().beginTransaction().hide(getActivity().getSupportFragmentManager().findFragmentByTag("main")).replace(R.id.fl_settings_first, mFragmentSettingsDevelop).addToBackStack("develop").commitAllowingStateLoss();
                        } else {
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fl_settings_second, mFragmentSettingsDevelop).commitAllowingStateLoss();
                        }
                        break;
                    case R.id.logout:
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.logout)
                                .setMessage(R.string.logout_ask)
                                .setIcon(R.drawable.ic_info)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pBilibiliClient.logout();
                                        File file = new File(getContext().getFilesDir(), "LoginResponse");
                                        file.delete();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                        break;
                }
                return false;
            }
        });
    }
}