package com.wateregg.uperfsetting.Dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.wateregg.uperfsetting.R;

public class ToastDialog extends DialogFragment {
    private String message;

    public ToastDialog(String message) {
        this.message = message;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.toast_dialog, container, false);

        TextView message = view.findViewById(R.id.toast_dialog_message);
        message.setText(this.message);

        Button button = view.findViewById(R.id.toast_dialog_ok);
        button.setOnClickListener(v -> {
            if (getDialog() != null) getDialog().cancel();
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.base_mode_select);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.toast_dialog);

        Window window = dialog.getWindow();
        if (window != null) {
            int width = getResources().getDisplayMetrics().widthPixels;

            window.setLayout(width / 3, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        return dialog;
    }
}
