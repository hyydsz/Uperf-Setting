package com.wateregg.uperfsetting;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wateregg.uperfsetting.Dialog.ToastDialog;
import com.wateregg.uperfsetting.Layout.AppSettings;
import com.wateregg.uperfsetting.Layout.Home;
import com.wateregg.uperfsetting.Layout.Layout;
import com.wateregg.uperfsetting.Layout.Setting;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final static int STORAGE_REQUEST_CODE = 1;
    public final String[] Read_Write_Permissions = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private TabLayoutMediator tabLayoutMediator;
    private boolean is_background = false;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ModeString.powerMode = new PowerMode();
        ModeString.Module_Enable = ModeString.powerMode.json_handle();

        initBackgroundCallBack();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
        {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, STORAGE_REQUEST_CODE);

                return;
            }
        }
        else
        {
            if (checkSelfPermission(Read_Write_Permissions[0]) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Read_Write_Permissions[1]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(Read_Write_Permissions, STORAGE_REQUEST_CODE);

                return;
            }
        }

        start_init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STORAGE_REQUEST_CODE && resultCode == PackageManager.PERMISSION_GRANTED) {
            start_init();
        }
    }

    public void start_init() {

        setContentView(R.layout.navigation_menu);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.container);

        Class[] classes = { Home.class, AppSettings.class, Setting.class};
        String[] names = { getString(R.string.uperf_settings), getString(R.string.uperf_app_settings), getString(R.string.uperf_power_settings) };
        int[] icons = { R.mipmap.home, R.mipmap.app, R.mipmap.setting };

        viewPager2.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager2.setAdapter(new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), classes));

        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
           tab.setIcon(icons[position]);
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (ModeString.Module_Enable) {
                    TextView textView = findViewById(R.id.title_view);
                    textView.setText(names[tab.getPosition()]);

                    return;
                }

                if (tab.getPosition() == 1 || tab.getPosition() == 2) {
                    tabLayout.selectTab(tabLayout.getTabAt(0));

                    ToastDialog toastDialog = new ToastDialog(getString(R.string.cannot_open_app_setting));
                    toastDialog.show(getSupportFragmentManager(), toastDialog.getTag());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayoutMediator.attach();
    }

    @Override
    protected void onDestroy() {
        tabLayoutMediator.detach();

        super.onDestroy();
    }

    private void initBackgroundCallBack() {
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                if (is_background) {
                    is_background = false;

                    if (viewPager2 != null) {
                        FragmentAdapter fragmentAdapter = (FragmentAdapter) viewPager2.getAdapter();
                        fragmentAdapter.Refresh(tabLayout.getSelectedTabPosition());
                    }
                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                is_background = true;
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }

    private class FragmentAdapter extends FragmentStateAdapter {
        private Class[] classes;
        private List<Layout> layouts = new ArrayList<>();

        public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Class[] classes) {
            super(fragmentManager, lifecycle);

            this.classes = classes;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            try {
                Fragment fragment = FragmentFactory.loadFragmentClass(classes[position].getClassLoader(), classes[position].getName()).newInstance();
                layouts.add((Layout) fragment);

                return fragment;

            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }

        public void Refresh(int position) {
            layouts.get(position).Background_Refresh();
        }

        @Override
        public int getItemCount() {
            return classes.length;
        }
    }
}
