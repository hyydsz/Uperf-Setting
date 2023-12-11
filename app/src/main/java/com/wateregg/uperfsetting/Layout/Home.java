package com.wateregg.uperfsetting.Layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.wateregg.uperfsetting.Dialog.BottomLogger;
import com.wateregg.uperfsetting.Dialog.ToastDialog;
import com.wateregg.uperfsetting.ModeString;
import com.wateregg.uperfsetting.R;

import org.json.JSONException;

public class Home extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        if (!ModeString.powerMode.ReadFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
            ToastDialog toastDialog = new ToastDialog(getString(R.string.read_file_fail));
            toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
        }

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.setting_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            ModeString.Module_Enable = ModeString.powerMode.json_handle();
            
            if (!ModeString.powerMode.ReadFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
                ToastDialog toastDialog = new ToastDialog(getString(R.string.read_file_fail));
                toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
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
                auto_mode_settings.setVisibility(tab.getTag() == ModeString.ModeType.auto ? View.VISIBLE : View.GONE);

                ModeString.powerMode.SystemMode = (ModeString.ModeType) tab.getTag();
                if (!ModeString.powerMode.WriteFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
                    ToastDialog toastDialog = new ToastDialog(getString(R.string.read_file_fail));
                    toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
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
                ModeString.powerMode.DefaultMode = (ModeString.ModeType) tab.getTag();
                if (!ModeString.powerMode.WriteFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
                    ToastDialog toastDialog = new ToastDialog(getString(R.string.read_file_fail));
                    toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
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
                ModeString.powerMode.OffScreenMode = (ModeString.ModeType) tab.getTag();
                if (!ModeString.powerMode.WriteFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
                    ToastDialog toastDialog = new ToastDialog(getString(R.string.read_file_fail));
                    toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
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
        String str_name = "";
        String str_author = "";
        String str_version = "";

        LinearLayout auto_mode_settings = view.findViewById(R.id.auto_mode_setting);
        LinearLayout module_setting = view.findViewById(R.id.module_setting);

        TextView name = view.findViewById(R.id.uperf_name);
        TextView author = view.findViewById(R.id.uperf_author);
        TextView version = view.findViewById(R.id.uperf_version);

        TextView module_start = view.findViewById(R.id.module_state);

        if (ModeString.Module_Enable) {
            try {
                str_name = String.format(getString(R.string.module_name), ModeString.uperf_json.getString("name"));
                str_author = String.format(getString(R.string.module_author), ModeString.uperf_json.getString("author"));
                str_version = String.format(getString(R.string.module_version), ModeString.uperf_json.getString("version"));
            }
            catch (JSONException | NullPointerException ignore) {
                ToastDialog toastDialog = new ToastDialog(getString(R.string.read_file_fail));
                toastDialog.show(getParentFragmentManager(), toastDialog.getTag());

                return;
            }

            module_start.setText(getString(R.string.has_been_started));
            module_setting.setVisibility(View.VISIBLE);
        }
        else {
            module_start.setText(getString(R.string.not_started));
            module_setting.setVisibility(View.GONE);
        }


        TabLayout system_mode = view.findViewById(R.id.system_mode);
        TabLayout normal_mode = view.findViewById(R.id.normal_mode);
        TabLayout standby_mode = view.findViewById(R.id.standyby_mode);

        name.setText(str_name);
        author.setText(str_author);
        version.setText(str_version);

        // 读取应用每个配置
        if (ModeString.powerMode.SystemMode != null) {
            int index = ModeString.FromModeToIndex(ModeString.powerMode.SystemMode);

            if (index == 0 || index == -1) {
                index = 0;

                auto_mode_settings.setVisibility(View.VISIBLE);
            }

            system_mode.selectTab(system_mode.getTabAt(index));
        }

        // 读取应用每个配置
        if (ModeString.powerMode.DefaultMode != null) {
            int index = ModeString.FromModeToIndex(ModeString.powerMode.DefaultMode);
            if (index == -1) index = 2 + 1;

            normal_mode.selectTab(normal_mode.getTabAt( index - 1));
        }

        // 读取应用每个配置
        if (ModeString.powerMode.OffScreenMode != null) {
            int index = ModeString.FromModeToIndex(ModeString.powerMode.OffScreenMode);
            if (index == -1) index = 2 + 1;

            standby_mode.selectTab(standby_mode.getTabAt(index - 1));
        }
    }

    public void addSystemSelectMode(TabLayout tabLayout) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(getString(R.string.auto));
        tab.setTag(ModeString.ModeType.auto);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.powersave));
        tab.setTag(ModeString.ModeType.powersave);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.balance));
        tab.setTag(ModeString.ModeType.balance);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.performance));
        tab.setTag(ModeString.ModeType.performance);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.fast));
        tab.setTag(ModeString.ModeType.fast);

        tabLayout.addTab(tab);
    }

    public void addSelectMode(TabLayout tabLayout) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(getString(R.string.powersave));
        tab.setTag(ModeString.ModeType.powersave);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.balance));
        tab.setTag(ModeString.ModeType.balance);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.performance));
        tab.setTag(ModeString.ModeType.performance);

        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        tab.setText(getString(R.string.fast));
        tab.setTag(ModeString.ModeType.fast);

        tabLayout.addTab(tab);
    }
}
