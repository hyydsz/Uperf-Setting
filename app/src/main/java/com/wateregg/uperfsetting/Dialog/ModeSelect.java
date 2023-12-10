package com.wateregg.uperfsetting.Dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.wateregg.uperfsetting.ModeString;
import com.wateregg.uperfsetting.R;

public class ModeSelect extends DialogFragment {

    private String package_name;
    private TextView mode_textview;

    public ModeSelect(String package_name, TextView mode_textview) {
        this.package_name = package_name;
        this.mode_textview = mode_textview;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mode_select, container, false);
        Button cancel = view.findViewById(R.id.mode_select_cancel);
        Button ok = view.findViewById(R.id.mode_select_ok);

        cancel.setOnClickListener(v -> {
            if (getDialog() != null) getDialog().cancel();
        });

        ok.setOnClickListener(v -> {
            RadioGroup radioGroup = view.findViewById(R.id.mode_select_radio_group);
            RadioButton radioButton = view.findViewById(radioGroup.getCheckedRadioButtonId());

            String mode = radioButton.getTag().toString();

            switch (mode) {
                case ModeString.POWERSAVE:
                    mode_textview.setText(view.getContext().getString(R.string.powersave_mode));
                    mode_textview.setTextColor(view.getContext().getColor(R.color.powersave_color));

                    ModeString.powerMode.appModes.put(package_name, mode);
                    break;

                case ModeString.BALANCE:
                    mode_textview.setText(view.getContext().getString(R.string.balance_mode));
                    mode_textview.setTextColor(view.getContext().getColor(R.color.balance_color));

                    ModeString.powerMode.appModes.put(package_name, mode);
                    break;

                case ModeString.PERFORMANCE:
                    mode_textview.setText(view.getContext().getString(R.string.performance_mode));
                    mode_textview.setTextColor(view.getContext().getColor(R.color.performance_color));

                    ModeString.powerMode.appModes.put(package_name, mode);
                    break;

                case ModeString.FAST:
                    mode_textview.setText(view.getContext().getString(R.string.fast_mode));
                    mode_textview.setTextColor(view.getContext().getColor(R.color.fast_color));

                    ModeString.powerMode.appModes.put(package_name, mode);
                    break;

                default:
                    mode_textview.setText(view.getContext().getString(R.string.system_normal_mode));
                    mode_textview.setTextColor(view.getContext().getColor(R.color.system_normal_color));

                    ModeString.powerMode.appModes.remove(package_name);
                    break;
            }

            ModeString.powerMode.WriteFile(ModeString.uperf_last_path, ModeString.PERAPP_POWERMODE);

            if (getDialog() != null) getDialog().cancel();
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.base_mode_select);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.mode_select);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        return dialog;
    }
}
