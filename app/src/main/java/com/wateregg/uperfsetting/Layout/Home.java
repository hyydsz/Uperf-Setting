package com.wateregg.uperfsetting.Layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.wateregg.uperfsetting.Dialog.BottomLogger;
import com.wateregg.uperfsetting.ModeString;
import com.wateregg.uperfsetting.PowerMode;
import com.wateregg.uperfsetting.R;

import org.json.JSONException;

public class Home extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        ModeString.powerMode = new PowerMode();
        if (!ModeString.powerMode.ReadFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
            Toast.makeText(view.getContext(), "读取应用配置失败", Toast.LENGTH_LONG).show();
        }

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.setting_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            if (!ModeString.powerMode.ReadFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
                Toast.makeText(view.getContext(), "读取应用配置失败", Toast.LENGTH_LONG).show();
                return;
            }

            refreshMode(view);
            refreshLayout.setRefreshing(false);
        });

        TabLayout system_mode = view.findViewById(R.id.system_mode);
        TabLayout normal_mode = view.findViewById(R.id.normal_mode);
        TabLayout standby_mode = view.findViewById(R.id.standyby_mode);

        LinearLayout auto_mode_settings = view.findViewById(R.id.auto_mode_setting);

        addSystemSelectMode(system_mode);
        addSelectMode(normal_mode);
        addSelectMode(standby_mode);

        Button open_log = view.findViewById(R.id.open_log);
        open_log.setOnClickListener(v -> {
            BottomLogger bottomLogger = new BottomLogger();
            bottomLogger.show(getParentFragmentManager(), bottomLogger.getTag());
        });

        refreshMode(view);

        system_mode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                auto_mode_settings.setVisibility(tab.getTag() == ModeString.AUTO ? View.VISIBLE : View.GONE);
                ModeString.powerMode.SystemMode = tab.getTag().toString();
                if (!ModeString.powerMode.WriteFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
                    Toast.makeText(view.getContext(), "写入应用配置失败", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        normal_mode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ModeString.powerMode.DefaultMode = tab.getTag().toString();
                if (!ModeString.powerMode.WriteFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
                    Toast.makeText(view.getContext(), "写入应用配置失败", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        standby_mode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ModeString.powerMode.OffScreenMode = tab.getTag().toString();
                if (!ModeString.powerMode.WriteFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
                    Toast.makeText(view.getContext(), "写入应用配置失败", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    public void refreshMode(View view) {
        String str_name;
        String str_author;
        String str_version;

        try {
            str_name = String.format(getString(R.string.module_name), ModeString.uperf_json.getString("name"));
            str_author = String.format(getString(R.string.module_author), ModeString.uperf_json.getString("author"));
            str_version = String.format(getString(R.string.module_version), ModeString.uperf_json.getString("version"));
        }
        catch (JSONException ignore) {
            Toast.makeText(view.getContext(), "读取Uperf文件失败", Toast.LENGTH_LONG).show();
            return;
        }

        TextView name = view.findViewById(R.id.uperf_name);
        TextView author = view.findViewById(R.id.uperf_author);
        TextView version = view.findViewById(R.id.uperf_version);

        TabLayout system_mode = view.findViewById(R.id.system_mode);
        TabLayout normal_mode = view.findViewById(R.id.normal_mode);
        TabLayout standby_mode = view.findViewById(R.id.standyby_mode);

        LinearLayout auto_mode_settings = view.findViewById(R.id.auto_mode_setting);

        name.setText(str_name);
        author.setText(str_author);
        version.setText(str_version);

        if (ModeString.powerMode.SystemMode != null) {
            int index = ModeString.FromModeToIndex(ModeString.powerMode.SystemMode);

            if (index == 0) {
                auto_mode_settings.setVisibility(View.VISIBLE);
            }

            system_mode.selectTab(system_mode.getTabAt(index));
        }

        if (ModeString.powerMode.DefaultMode != null) {
            normal_mode.selectTab(normal_mode.getTabAt(ModeString.FromModeToIndex(ModeString.powerMode.DefaultMode) - 1));
        }

        if (ModeString.powerMode.OffScreenMode != null) {
            standby_mode.selectTab(standby_mode.getTabAt(ModeString.FromModeToIndex(ModeString.powerMode.OffScreenMode) - 1));
        }
    }

    public void addSystemSelectMode(TabLayout tabLayout) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(getString(R.string.auto));
        tab.setTag(ModeString.AUTO);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.powersave));
        tab.setTag(ModeString.POWERSAVE);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.balance));
        tab.setTag(ModeString.BALANCE);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.performance));
        tab.setTag(ModeString.PERFORMANCE);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.fast));
        tab.setTag(ModeString.FAST);

        tabLayout.addTab(tab);
    }

    public void addSelectMode(TabLayout tabLayout) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(getString(R.string.powersave));
        tab.setTag(ModeString.POWERSAVE);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.balance));
        tab.setTag(ModeString.BALANCE);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.performance));
        tab.setTag(ModeString.PERFORMANCE);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.fast));
        tab.setTag(ModeString.FAST);

        tabLayout.addTab(tab);
    }
}
