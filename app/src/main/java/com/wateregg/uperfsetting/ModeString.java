package com.wateregg.uperfsetting;

import org.json.JSONObject;

public class ModeString {
    public final static String POWERCFG = "/data/powercfg.json";
    public final static String PERAPP_POWERMODE = "perapp_powermode.txt";
    public final static String UPERF_LOG = "uperf_log.txt";

    public static String uperf_state_path;
    public static String uperf_last_path;
    public static JSONObject uperf_json;
    public static PowerMode powerMode;

    public static boolean Module_Enable = false;

    public final static String AUTO = "auto";
    public final static String POWERSAVE = "powersave";
    public final static String BALANCE = "balance";
    public final static String PERFORMANCE = "performance";
    public final static String FAST = "fast";
    public final static String SYSTEM_NORMAL = "system_normal";

    public static int FromModeToIndex(String current_mode) {
        switch (current_mode) {
            case ModeString.AUTO:
                return 0;

            case ModeString.POWERSAVE:
                return 1;

            case ModeString.BALANCE:
                return 2;

            case ModeString.PERFORMANCE:
                return 3;

            case ModeString.FAST:
                return 4;

            default:
                return -1;
        }
    };
}
