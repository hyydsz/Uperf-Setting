package com.wateregg.uperfsetting;

import org.json.JSONObject;

public class ModeString {
    public final static String POWERCFG = "/data/powercfg.json";
    public final static String UPERF_JSON = "uperf.json";
    public final static String PERAPP_POWERMODE = "perapp_powermode.txt";
    public final static String UPERF_LOG = "uperf_log.txt";

    public static String uperf_state_path;
    public static String uperf_last_path;
    public static JSONObject uperf_json;
    public static PowerMode powerMode;

    public static boolean Module_Enable = false;

    // 这个需要跟App设置的五个模式顺序一样
    public enum ModeType {
        system_normal,
        powersave,
        balance,
        performance,
        fast,
        auto,
    }

    public static int FromModeToIndex(ModeType current_mode) {
        switch (current_mode) {
            case auto:
                return 0;

            case powersave:
                return 1;

            case balance:
                return 2;

            case performance:
                return 3;

            case fast:
                return 4;

            default:
                return -1;
        }
    }
}
