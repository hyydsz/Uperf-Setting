package com.wateregg.uperfsetting.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.wateregg.uperfsetting.MainActivity;
import com.wateregg.uperfsetting.R;

public class SettingDialog extends DialogFragment {
    public enum Colors {
        Blue,
        Orange,
        Yellow,
        Green,
        Red
    }

    public enum SliderThemes {
        Normal,
        Bilibili
    }

    private SharedPreferences sharedPreferences;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_dialog, container, false);

        sharedPreferences = getContext().getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String current_color =  sharedPreferences.getString("Color", Colors.Blue.name());
        String current_slider_theme = sharedPreferences.getString("SliderTheme", SliderThemes.Normal.name());

        RadioGroup more_colors = view.findViewById(R.id.more_colors);
        for (int i = 0; i < more_colors.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) more_colors.getChildAt(i);
            radioButton.setTag(Colors.values()[i].name());

            if (radioButton.getTag().equals(current_color)) {
                more_colors.check(radioButton.getId());
            }
        }

        RadioGroup slider_themes = view.findViewById(R.id.more_slider_themes);
        for (int i = 0; i < slider_themes.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) slider_themes.getChildAt(i);
            radioButton.setTag(SliderThemes.values()[i].name());

            if (radioButton.getTag().equals(current_slider_theme)) {
                slider_themes.check(radioButton.getId());
            }
        }

        Button button = view.findViewById(R.id.toast_dialog_ok);
        button.setOnClickListener(v -> {
            String color = sharedPreferences.getString("Color", Colors.Blue.name());
            String slider_theme = sharedPreferences.getString("SliderTheme", SliderThemes.Normal.name());

            RadioButton color_button = view.findViewById(more_colors.getCheckedRadioButtonId());
            if (color_button != null && !color.equals(color_button.getTag())) {
                editor.putString("Color", color_button.getTag().toString());
                editor.apply();

                MainActivity.RestartActivity.run();
            }

            RadioButton slider_theme_button = view.findViewById(slider_themes.getCheckedRadioButtonId());
            if (slider_theme_button != null && !slider_theme.equals(slider_theme_button.getTag())) {
                editor.putString("SliderTheme", slider_theme_button.getTag().toString());
                editor.apply();

                MainActivity.RestartActivity.run();
            }

            if (getDialog() != null) getDialog().cancel();
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.base_mode_select);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.setting_dialog);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        return dialog;
    }
}
