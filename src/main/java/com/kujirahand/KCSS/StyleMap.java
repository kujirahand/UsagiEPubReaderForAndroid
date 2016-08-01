package com.kujirahand.KCSS;

import android.util.ArrayMap;

import com.kujirahand.utils.StringMap;

import java.util.Arrays;

/**
 * Created by kujira on 2016/04/18.
 */
public class StyleMap extends StringMap {

    public int getColor(String key) {
        String v = get(key);
        if (v == null) return 0;
        return getColorFromString(v);
    }
    public int getColor(String key, int def) {
        String v = get(key);
        if (v == null) return def;
        return getColorFromString(v);
    }
    public String getItem(String key, String def) {
        String v = get(key);
        if (v == null) return def;
        return v;
    }

    public static int getColorFromString(String code) {
        if (code == null) return 0;
        if (code.equals("")) return 0;
        if (code.charAt(0) == '#') {
            code = code.substring(1);
            return Integer.valueOf(code, 16);
        }

        if (code.equals("red")) return 0xFF0000;
        if (code.equals("green")) return 0x00FF00;
        if (code.equals("blue")) return 0x0000FF;

        return 0;
    }

    public String toString() {
        int size = this.size();
        int i = 0;
        String[] lines = new String[size];
        for (String key : this.keySet()) {
            String v = get(key);
            lines[i++] = key + ":" + v + ";";
        }
        Arrays.sort(lines);
        StringBuilder sb = new StringBuilder();
        for (String n : lines) {
            sb.append(n + "\n");
        }
        return sb.toString();
    }
}
