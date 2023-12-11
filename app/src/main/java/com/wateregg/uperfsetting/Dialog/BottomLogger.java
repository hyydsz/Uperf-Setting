package com.wateregg.uperfsetting.Dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wateregg.uperfsetting.ModeString;
import com.wateregg.uperfsetting.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BottomLogger extends BottomSheetDialogFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NORMAL, R.style.base_bottom_logger);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_logger, container, false);

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.logger_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            refresh_logger(view);
            refreshLayout.setRefreshing(false);
        });

        refresh_logger(view);

        return view;
    }

    private void refresh_logger(View view) {
        File file = new File(ModeString.uperf_last_path, ModeString.UPERF_LOG);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            List<Logger> loggers = new ArrayList<>();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Logger logger = new Logger();

                String[] strings = line.split(" | ", 2);

                logger.time = strings[0];
                logger.message = strings[1].substring(2);

                loggers.add(logger);
            }

            Collections.reverse(loggers);

            RecyclerView recyclerView = view.findViewById(R.id.logger_list);
            recyclerView.setAdapter(new LoggerAdapter(loggers));
        }
        catch (IOException ignore) {
            ToastDialog toastDialog = new ToastDialog(getString(R.string.read_log_file_fail));
            toastDialog.show(getParentFragmentManager(), toastDialog.getTag());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            BottomSheetBehavior<FrameLayout> bottomSheetBehavior = dialog.getBehavior();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetBehavior.setMaxHeight(getMaxHeight());
            bottomSheetBehavior.setDraggable(false);
        }
    }

    protected int getMaxHeight() {
        int height = getResources().getDisplayMetrics().heightPixels;

        // 4 / 5
        return height - height / 5;
    }

    private static class LoggerAdapter extends RecyclerView.Adapter<LoggerAdapter.ViewHolder> {

        public List<Logger> loggers;

        public LoggerAdapter(List<Logger> loggers) {
            this.loggers = loggers;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_logger_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Logger logger = loggers.get(position);
            holder.logger_message.setText(logger.message);
            holder.logger_time.setText(logger.time);
        }

        @Override
        public int getItemCount() {
            return loggers.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView logger_message;
            public TextView logger_time;

            public ViewHolder(View itemView) {
                super(itemView);

                logger_message = itemView.findViewById(R.id.logger_message);
                logger_time = itemView.findViewById(R.id.logger_time);
            }
        }
    }

    private static class Logger {
        public String time;
        public String message;
    }
}
