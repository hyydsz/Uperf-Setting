package com.wateregg.uperfsetting.Layout;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.litao.slider.NiftySlider;
import com.wateregg.uperfsetting.Dialog.ToastDialog;
import com.wateregg.uperfsetting.ModeString;
import com.wateregg.uperfsetting.R;
import com.wateregg.uperfsetting.Sliders.SliderTheme;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.Unit;

public class Setting extends Layout {
    private JSONObject CPUSettingJson;
    private RecyclerView recyclerView;

    private SwipeRefreshLayout refreshLayout;
    private View view;

    private final String LATENCY_TIME = "cpu.latencyTime";
    private final String SLOW_LIMIT_POWER = "cpu.slowLimitPower";
    private final String FAST_LIMIT_POWER = "cpu.fastLimitPower";
    private final String FAST_LIMIT_CAPACITY = "cpu.fastLimitCapacity";
    private final String CPU_MARGIN = "cpu.margin";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting, container, false);
        this.view = view;

        recyclerView = view.findViewById(R.id.power_mode_list);

        refreshLayout = view.findViewById(R.id.power_setting_refresh);
        refreshLayout.setOnRefreshListener(this::Background_Refresh);

        InitConfigCopy(view);

        refreshLayout.setRefreshing(true);
        Refresh(view);

        return view;
    }

    private void InitConfigCopy(View view) {
        Button copy = view.findViewById(R.id.power_setting_copy);
        copy.setOnClickListener(v -> {
            try {
                JSONObject presets = CPUSettingJson.getJSONObject("presets");

                byte[] bytes = presets.toString().getBytes(StandardCharsets.UTF_8);
                String bytes_to_base64 = Base64.getEncoder().encodeToString(bytes);

                ClipboardManager clipboardManager = (ClipboardManager) view.getContext().getSystemService(Service.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("PowerData", bytes_to_base64));

                Toast.makeText(view.getContext(), view.getContext().getString(R.string.copy_success), Toast.LENGTH_LONG).show();
            }
            catch (JSONException | NullPointerException ignore) {
                ToastDialog toastDialog = new ToastDialog(view.getContext().getString(R.string.copy_fail));
                toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
            }
        });

        Button paste = view.findViewById(R.id.power_setting_paste);
        paste.setOnClickListener(v -> {
            EditText editText = view.findViewById(R.id.power_setting_input);
            if (!editText.getText().toString().isEmpty()) {
                try {
                    byte[] base64_to_bytes = Base64.getDecoder().decode(editText.getText().toString());
                    String bytes_to_string = new String(base64_to_bytes, StandardCharsets.UTF_8);

                    JSONObject parse_json = new JSONObject(bytes_to_string);

                    CPUSettingJson.put("presets", parse_json);

                    File file = new File(ModeString.uperf_last_path, ModeString.UPERF_JSON);
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                    bufferedWriter.write(CPUSettingJson.toString(2));
                    bufferedWriter.flush();
                    bufferedWriter.close();

                    Background_Refresh();

                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.parse_success), Toast.LENGTH_LONG).show();
                }
                catch (IllegalArgumentException | JSONException | IOException ignore) {
                    ToastDialog toastDialog = new ToastDialog(view.getContext().getString(R.string.parse_fail));
                    toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
                }
            }
        });
    }

    private void Refresh(View view) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                Handler handler = new Handler(Looper.getMainLooper());

                File file = new File(ModeString.uperf_last_path, ModeString.UPERF_JSON);
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                    StringBuilder sb = new StringBuilder();

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    CPUSettingJson = new JSONObject(sb.toString());

                } catch (IOException | JSONException e) {

                    CPUSettingJson = null;

                    handler.post(() -> {
                        refreshLayout.setRefreshing(false);

                        ToastDialog toastDialog = new ToastDialog(getString(R.string.read_file_fail));
                        toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
                    });

                    return;
                }

                handler.postDelayed(() -> {
                    Power[] powers = new Power[] {
                            new Power(view.getContext(), ModeString.ModeType.powersave),
                            new Power(view.getContext(), ModeString.ModeType.balance),
                            new Power(view.getContext(), ModeString.ModeType.performance),
                            new Power(view.getContext(), ModeString.ModeType.fast)
                    };

                    recyclerView.setAdapter(new PowerAdapter(powers));
                    refreshLayout.setRefreshing(false);
                }, 100);
            }

        }.start();
    }

    @Override
    public void Background_Refresh() {
        if (recyclerView.getAdapter() != null) {
            ((PowerAdapter) recyclerView.getAdapter()).clear();
        }

        Refresh(view);
    }

    private class PowerAdapter extends RecyclerView.Adapter<PowerAdapter.ViewHolder> {
        private List<Power> powers;

        public PowerAdapter(Power[] powers) {
            this.powers = Arrays.stream(powers).collect(Collectors.toList());
        }

        @SuppressLint("NotifyDataSetChanged")
        public void clear() {
            powers.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Power power = powers.get(position);

            holder.power_mode_name.setText(power.power_mode_name);

            if (CPUSettingJson == null) return;

            power_load(holder, power.power_type);

            holder.slider_latencyTime.addOnValueChangeListener((niftySlider, aFloat, aBoolean) -> power_update(holder));
            holder.slider_slowLimitPower.addOnValueChangeListener((niftySlider, aFloat, aBoolean) -> power_update(holder));
            holder.slider_fastLimitPower.addOnValueChangeListener((niftySlider, aFloat, aBoolean) -> power_update(holder));
            holder.slider_fastLimitCapacity.addOnValueChangeListener(((niftySlider, aFloat, aBoolean) -> power_update(holder)));
            holder.slider_cpu_margin.addOnValueChangeListener(((niftySlider, aFloat, aBoolean) -> power_update(holder)));

            holder.slider_latencyTime.addOnSliderTouchStopListener(niftySlider -> power_save(holder, power.power_type));
            holder.slider_slowLimitPower.addOnSliderTouchStopListener(niftySlider -> power_save(holder, power.power_type));
            holder.slider_fastLimitPower.addOnSliderTouchStopListener(niftySlider -> power_save(holder, power.power_type));
            holder.slider_fastLimitCapacity.addOnSliderTouchStopListener(niftySlider -> power_save(holder, power.power_type));
            holder.slider_cpu_margin.addOnSliderTouchStopListener(niftySlider -> power_save(holder, power.power_type));

            SliderTheme.Companion.addSliderTheme(holder.slider_latencyTime);
            SliderTheme.Companion.addSliderTheme(holder.slider_slowLimitPower);
            SliderTheme.Companion.addSliderTheme(holder.slider_fastLimitPower);
            SliderTheme.Companion.addSliderTheme(holder.slider_fastLimitCapacity);
            SliderTheme.Companion.addSliderTheme(holder.slider_cpu_margin);
        }

        private Unit power_update(ViewHolder holder) {
            holder.current_latencyTime.setText(String.format(holder.itemView.getContext().getString(R.string.unit_ms), holder.slider_latencyTime.getValue()));
            holder.current_slowLimitPower.setText(String.format(holder.itemView.getContext().getString(R.string.unit_power), holder.slider_slowLimitPower.getValue()));
            holder.current_fastLimitPower.setText(String.format(holder.itemView.getContext().getString(R.string.unit_power), holder.slider_fastLimitPower.getValue()));
            holder.current_fastLimitCapacity.setText(String.format(holder.itemView.getContext().getString(R.string.unit_power_s), holder.slider_fastLimitCapacity.getValue()));
            holder.current_cpu_margin.setText(String.format(holder.itemView.getContext().getString(R.string.unit_percent), holder.slider_cpu_margin.getValue()));

            return null;
        }

        private void power_load(ViewHolder holder, ModeString.ModeType powerMode) {
            try {
                JSONObject presets = CPUSettingJson.getJSONObject("presets");
                JSONObject power_mode = presets.getJSONObject(powerMode.name());
                JSONObject cpu = power_mode.getJSONObject("*");

                holder.slider_latencyTime.setValue((float) cpu.getDouble(LATENCY_TIME) * 1000, true);
                holder.slider_slowLimitPower.setValue((float) cpu.getDouble(SLOW_LIMIT_POWER), true);
                holder.slider_fastLimitPower.setValue((float) cpu.getDouble(FAST_LIMIT_POWER), true);
                holder.slider_fastLimitCapacity.setValue((float) cpu.getDouble(FAST_LIMIT_CAPACITY), true);
                holder.slider_cpu_margin.setValue((float) cpu.getDouble(CPU_MARGIN) * 100, true);

                power_update(holder);

            } catch (JSONException ignored) {
                ToastDialog toastDialog = new ToastDialog(getString(R.string.read_file_fail));
                toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
            }
        }

        @SuppressLint("DefaultLocale")
        private Unit power_save(ViewHolder holder, ModeString.ModeType powerMode) {
            try {
                JSONObject presets = CPUSettingJson.getJSONObject("presets");
                JSONObject power_mode = presets.getJSONObject(powerMode.name());
                JSONObject cpu = power_mode.getJSONObject("*");

                cpu.put(LATENCY_TIME, Double.parseDouble(String.format("%.1f", holder.slider_latencyTime.getValue() / 1000)));
                cpu.put(SLOW_LIMIT_POWER, Double.parseDouble(String.format("%.1f", holder.slider_slowLimitPower.getValue())));
                cpu.put(FAST_LIMIT_POWER, Double.parseDouble(String.format("%.1f", holder.slider_fastLimitPower.getValue())));
                cpu.put(FAST_LIMIT_CAPACITY, Double.parseDouble(String.format("%.0f", holder.slider_fastLimitCapacity.getValue())));
                cpu.put(CPU_MARGIN, Double.parseDouble(String.format("%.1f", holder.slider_cpu_margin.getValue() / 100)));

                power_mode.put("*", cpu);
                presets.put(powerMode.name(), power_mode);
                CPUSettingJson.put("presets", presets);

                File file = new File(ModeString.uperf_last_path, ModeString.UPERF_JSON);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                bufferedWriter.write(CPUSettingJson.toString(2));
                bufferedWriter.flush();
                bufferedWriter.close();

            } catch (JSONException | IOException ignored) {
                ToastDialog toastDialog = new ToastDialog(getString(R.string.write_file_fail));
                toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
            }

            return null;
        }

        @Override
        public int getItemCount() {
            return powers.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public TextView power_mode_name;

            public TextView current_latencyTime;
            public NiftySlider slider_latencyTime;

            public TextView current_slowLimitPower;
            public NiftySlider slider_slowLimitPower;

            public TextView current_fastLimitPower;
            public NiftySlider slider_fastLimitPower;

            public TextView current_fastLimitCapacity;
            public NiftySlider slider_fastLimitCapacity;

            public TextView current_cpu_margin;
            public NiftySlider slider_cpu_margin;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                power_mode_name = itemView.findViewById(R.id.power_mode_name);

                current_latencyTime = itemView.findViewById(R.id.current_latencyTime);
                current_slowLimitPower = itemView.findViewById(R.id.current_slowLimitPower);
                current_fastLimitPower = itemView.findViewById(R.id.current_fastLimitPower);
                current_fastLimitCapacity = itemView.findViewById(R.id.current_fastLimitCapacity);
                current_cpu_margin = itemView.findViewById(R.id.current_cpu_margin);

                slider_latencyTime = itemView.findViewById(R.id.slider_latencyTime);
                slider_slowLimitPower = itemView.findViewById(R.id.slider_slowLimitPower);
                slider_fastLimitPower = itemView.findViewById(R.id.slider_fastLimitPower);
                slider_fastLimitCapacity = itemView.findViewById(R.id.slider_fastLimitCapacity);
                slider_cpu_margin = itemView.findViewById(R.id.slider_cpu_margin);
            }
        }
    }

    private static class Power {
        public String power_mode_name;
        public ModeString.ModeType power_type;

        public Power(Context context, ModeString.ModeType type) {
            power_type = type;

            switch (type) {
                case powersave:
                    power_mode_name = context.getString(R.string.powersave_mode);
                    break;

                case balance:
                    power_mode_name = context.getString(R.string.balance_mode);
                    break;

                case performance:
                    power_mode_name = context.getString(R.string.performance_mode);
                    break;

                case fast:
                    power_mode_name = context.getString(R.string.fast_mode);
                    break;
            }
        }
    }
}
