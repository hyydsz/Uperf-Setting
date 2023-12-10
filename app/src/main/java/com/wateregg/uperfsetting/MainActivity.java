package com.wateregg.uperfsetting;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wateregg.uperfsetting.Layout.AppSettings;
import com.wateregg.uperfsetting.Layout.Home;
import com.wateregg.uperfsetting.Layout.Setting;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    public final static int STORAGE_REQUEST_CODE = 1;

    private TabLayoutMediator tabLayoutMediator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (json_handle()) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, STORAGE_REQUEST_CODE);

                return;
            }

            start_init();
        }
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

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager2 = findViewById(R.id.container);

        String[] classes = { Home.class.getName(), AppSettings.class.getName(), Setting.class.getName()};
        String[] names = { getString(R.string.uperf_settings), getString(R.string.uperf_app_settings), getString(R.string.uperf_power_settings)};
        int[] icons = { R.mipmap.home, R.mipmap.app, R.mipmap.setting };

        viewPager2.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

        viewPager2.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return Fragment.instantiate(MainActivity.this, classes[position]);
            }

            @Override
            public int getItemCount() {
                return classes.length;
            }
        });

        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
           tab.setIcon(icons[position]);
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView textView = findViewById(R.id.title_view);
                textView.setText(names[tab.getPosition()]);
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

    public boolean json_handle()  {
        String string_json = getFileDataByRoot(ModeString.POWERCFG);
        if (string_json != null) {
            try {
                ModeString.uperf_json = new JSONObject(string_json);

                ModeString.uperf_state_path = ModeString.uperf_json.getString("state");
                ModeString.uperf_last_path = new File(ModeString.uperf_state_path).getParent();

                return true;
            }
            catch (JSONException ignored) { }
        }

        return false;
    }

    public String getFileDataByRoot(String path) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("cat " + path + "\n");
            os.writeBytes("exit 0\n");
            os.flush();

            process.waitFor();

            os.close();

            if (process.exitValue() == 0) {
                InputStreamReader input = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(input);

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }

                return builder.toString();
            }

        } catch (IOException | InterruptedException ignore) { }

        return null;
    }

    @Override
    protected void onDestroy() {
        tabLayoutMediator.detach();

        super.onDestroy();
    }
}
