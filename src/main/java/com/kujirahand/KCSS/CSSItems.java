package com.kujirahand.KCSS;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by kujira on 2016/04/18.
 */
public class CSSItems {
    public String mediaName = "screen";
    public HashMap<String, StyleMap> items = new HashMap<String, StyleMap>();
    public HashMap<String, CSSItems> mediaList = new HashMap<String, CSSItems>();
    private String rawSource = null;

    public CSSItems() {
        mediaList.put(mediaName, this);
    }

    public void setRawString(String src) {
        rawSource = src;
    }

    public CSSItems changeMedia(String name) {
        CSSItems css = mediaList.get(name);
        if (css == null) {
            css = new CSSItems();
            mediaList.put(name, css);
        }
        return css;
    }

    public StyleMap getStyleItem(String element) {
        StyleMap sm = items.get(element);
        if (sm == null) {
            sm = new StyleMap();
            items.put(element, sm);
        }
        return sm;
    }

    public void splitCommaName() {
        for (String id : items.keySet()) {
            if (id.indexOf(',') > 0) {
                StyleMap sm = items.get(id);
                String[] ids = id.split(",");
                for (String sid : ids) {
                    items.put(sid, sm);
                }
                items.remove(id);
            }
        }
    }

    public int findColor(String query, String key, int def) {
        StyleMap sm = items.get(query);
        if (sm == null) return def;
        String v = sm.get(key);
        if (v == null) return def;
        return StyleMap.getColorFromString(v);
    }

    public String findFontWeight(String query, String def) {
        String key = "font-weight";
        StyleMap sm = items.get(query);
        if (sm == null) return def;
        String v = sm.get(key);
        if (v == null) return def;
        return v;
    }

    public String toString() {
        if (rawSource != null) {
            return rawSource;
        }
        StringBuilder sb = new StringBuilder();
        int j = 0;
        String[] keys = new String[items.size()];
        for (String id : items.keySet()) {
            keys[j++] = id;
        }
        Arrays.sort(keys);
        //
        for (String id : keys) {
            sb.append(id + "{\n");
            StyleMap sm = items.get(id);
            int size = sm.size();
            int i = 0;
            String[] lines = new String[size];
            for (String key : sm.keySet()) {
                String v = sm.get(key);
                lines[i++] = "  " + key + ":" + v + ";";
            }
            Arrays.sort(lines);
            for (String n : lines) {
                sb.append(n + "\n");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }
}
