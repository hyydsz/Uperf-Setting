package com.wateregg.uperfsetting.Layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wateregg.uperfsetting.Dialog.ModeSelect;
import com.wateregg.uperfsetting.ModeString;
import com.wateregg.uperfsetting.R;

import java.util.ArrayList;
import java.util.List;

public class AppSettings extends Layout {
    private int package_filter_type = 0;
    private int mode_filter_type = 0;

    private RecyclerView recyclerView;
    private Editable SearchText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_setting, container, false);

        recyclerView = view.findViewById(R.id.app_list);

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.app_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            if (!ModeString.powerMode.ReadFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE)) {
                Toast.makeText(view.getContext(), "读取应用配置失败", Toast.LENGTH_LONG).show();
                return;
            }

            RefreshAppList(view, refreshLayout);
        });

        Spinner package_spinner = view.findViewById(R.id.package_type);
        package_spinner.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.simple_spinner_item, R.id.spinner_text, new String[] {getString(R.string.user), getString(R.string.system), getString(R.string.all)}));
        package_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view_, int position, long id) {
                package_filter_type = position;
                refreshLayout.setRefreshing(true);

                RefreshAppList(view, refreshLayout);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner mode_spinner = view.findViewById(R.id.mode_type);
        mode_spinner.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.simple_spinner_item, R.id.spinner_text, new String[] {
                getString(R.string.all),
                getString(R.string.system_normal_mode),
                getString(R.string.powersave_mode),
                getString(R.string.balance_mode),
                getString(R.string.performance_mode),
                getString(R.string.fast_mode)}));

        mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view_, int position, long id) {
                mode_filter_type = position;
                refreshLayout.setRefreshing(true);

                RefreshAppList(view, refreshLayout);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        EditText editText = view.findViewById(R.id.package_search);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SearchText = editText.getText();

                AppAdapter appAdapter = (AppAdapter) recyclerView.getAdapter();
                if (appAdapter != null) {
                    appAdapter.setNameFilter(SearchText);
                }
            }
        });

        return view;
    }

    private void RefreshAppList(View view, SwipeRefreshLayout refreshLayout) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                PackageManager pm = view.getContext().getPackageManager();

                List<App> apps = new ArrayList<>();
                List<ApplicationInfo> applications = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);

                for (ApplicationInfo info : applications) {
                    App app = new App();

                    app.icon = info.loadIcon(pm);
                    app.name = info.loadLabel(pm).toString();
                    app.package_name = info.packageName;

                    Intent launchIntent = pm.getLaunchIntentForPackage(info.packageName);

                    app.mode = ModeString.ModeType.system_normal;

                    String mode = ModeString.powerMode.appModes.getOrDefault(info.packageName, null);
                    if (mode != null) {
                        app.mode = ModeString.ModeType.valueOf(mode);
                    }

                    app.system_app = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0 || (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 || launchIntent == null;
                    switch (package_filter_type) {
                        case 0:
                            if (app.system_app) continue;
                            break;

                        case 1:
                            if (!app.system_app) continue;
                            break;
                    }

                    switch (mode_filter_type) {
                        case 1:
                            if (ModeString.ModeType.system_normal != app.mode) continue;
                            break;

                        case 2:
                            if (ModeString.ModeType.powersave != app.mode) continue;
                            break;

                        case 3:
                            if (ModeString.ModeType.balance != app.mode) continue;
                            break;

                        case 4:
                            if (ModeString.ModeType.performance != app.mode) continue;
                            break;

                        case 5:
                            if (ModeString.ModeType.fast != app.mode) continue;
                            break;
                    }

                    apps.add(app);
                }

                AppAdapter appAdapter = new AppAdapter(view.getContext(), apps);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    recyclerView.setAdapter(appAdapter);
                    refreshLayout.setRefreshing(false);
                });
            }
        }.start();
    }

    @Override
    public void Background_Refresh() {

    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        public List<App> apps;
        public List<App> backup_apps;
        public Context context;

        public AppAdapter(Context context, List<App> apps) {
            this.apps = apps;
            this.backup_apps = apps;
            this.context = context;

            if (SearchText != null) {
                setNameFilter(SearchText);
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setNameFilter(Editable filter) {
            List<App> lists = new ArrayList<>();
            for (App app : backup_apps) {
                if (app.name.contains(filter)) lists.add(app);
            }

            apps = lists;

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            App app = apps.get(position);

            holder.app_icon.setImageDrawable(app.icon);
            holder.app_name.setText(app.name);

            switch (app.mode) {
                case powersave:
                    holder.app_mode.setText(context.getString(R.string.powersave_mode));
                    holder.app_mode.setTextColor(context.getColor(R.color.powersave_color));

                    break;

                case balance:
                    holder.app_mode.setText(context.getString(R.string.balance_mode));
                    holder.app_mode.setTextColor(context.getColor(R.color.balance_color));

                    break;

                case performance:
                    holder.app_mode.setText(context.getString(R.string.performance_mode));
                    holder.app_mode.setTextColor(context.getColor(R.color.performance_color));

                    break;

                case fast:
                    holder.app_mode.setText(context.getString(R.string.fast_mode));
                    holder.app_mode.setTextColor(context.getColor(R.color.fast_color));

                    break;

                case system_normal:
                    holder.app_mode.setText(context.getString(R.string.system_normal_mode));
                    holder.app_mode.setTextColor(context.getColor(R.color.system_normal_color));

                    break;
            }

            holder.itemView.setOnClickListener(v -> {
                ModeSelect modeSelect = new ModeSelect(app.package_name, holder.app_mode);
                modeSelect.show(AppSettings.this.getParentFragmentManager(), modeSelect.getTag());
            });
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView app_icon;
            public TextView app_name;
            public TextView app_mode;

            public ViewHolder(View itemView) {
                super(itemView);

                app_icon = itemView.findViewById(R.id.app_icon);
                app_name = itemView.findViewById(R.id.app_name);
                app_mode = itemView.findViewById(R.id.app_mode);
            }
        }
    }

    private static class App {
        public Drawable icon;
        public String package_name;
        public String name;
        public ModeString.ModeType mode;
        public boolean system_app;
    }
}
