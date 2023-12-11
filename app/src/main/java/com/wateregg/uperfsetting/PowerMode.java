package com.wateregg.uperfsetting;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class PowerMode {
    public HashMap<String, String> appModes = new HashMap<>();

    public String DefaultMode;
    public String OffScreenMode;
    public String SystemMode;

    public boolean ReadFile(String path, String name) {
        if (ModeString.Module_Enable) {
            appModes.clear();

            File file = new File(ModeString.uperf_state_path);
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                SystemMode = bufferedReader.readLine();

                if (SystemMode == null) {
                    SystemMode = ModeString.BALANCE;
                }
            }
            catch (IOException ignore) {
                return false;
            }

            file = new File(path, name);
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith("#")) continue;

                    if (line.startsWith("*")) {
                        DefaultMode = line.substring(2);
                    }
                    else if (line.startsWith("-")) {
                        OffScreenMode = line.substring(2);
                    }
                    else {
                        String[] strings = line.split(" ", 2);

                        try {
                            appModes.put(strings[0], strings[1]);
                        }
                        catch (ArrayIndexOutOfBoundsException ignore) { }
                    }
                }
            }
            catch (IOException ignore) {
                return false;
            }

            boolean fix_mode = false;

            if (DefaultMode == null) {
                DefaultMode = ModeString.BALANCE;

                fix_mode = true;
            }

            if (OffScreenMode == null) {
                OffScreenMode = ModeString.BALANCE;

                fix_mode = true;
            }

            if (fix_mode) {
                WriteFile(path, name);
            }
        }

        return true;
    }

    public boolean WriteFile(String path, String name) {
        if (ModeString.Module_Enable) {
            File file = new File(ModeString.uperf_state_path);
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                bufferedWriter.write(SystemMode);
                bufferedWriter.flush();
            }
            catch (FileNotFoundException ignore) {
                return false;
            }
            catch (IOException ignore) { }

            StringBuilder sb = new StringBuilder();
            appModes.forEach((s, s2) -> {
                sb.append(s);
                sb.append(" ");
                sb.append(s2);
                sb.append("\n");
            });

            sb.append("* ");
            sb.append(DefaultMode);
            sb.append("\n");

            sb.append("- ");
            sb.append(OffScreenMode);
            sb.append("\n");

            file = new File(path, name);
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                bufferedWriter.write(sb.toString());
                bufferedWriter.flush();
            }
            catch (FileNotFoundException ignore) {
                return false;
            }
            catch (IOException ignore) { }
        }

        return true;
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
}
