package com.kujirahand.utils;

import java.io.IOException;

/**
 * Created by kujira on 2016/04/22.
 */
public class IniFile {
    protected StringMap items = new StringMap();
    protected String SectionDelimiter = "::";

    public void put(String key, String value) {
        items.put(key, value);
    }
    public void putInt(String key, int value) {
        items.put(key, "" + value);
    }
    public String get(String key) {
        return items.get(key);
    }
    public String get(String key, String def) {
        String res = get(key);
        if (res == null) return def;
        return res;
    }
    public int getInt(String key, int def) {
        String res = get(key);
        if (res == null) return def;
        return Integer.valueOf(res);
    }
    public String getSecKey(String section, String key) {
        String nkey = section + SectionDelimiter + key;
        return get(nkey);
    }
    public void parseStr(String src) {
        items.clear();
        src = src.replace("\r\n", "\n");
        src = src.replace("\r", "\n");
        //
        KStrTokenizer tok = new KStrTokenizer(src);
        String section = "";
        //
        while (!tok.isEOS()) {
            tok.skipSpaceReturn();
            char c = tok.peek();
            if (c == ';') { // comment
                tok.getToken('\n', true);
                continue;
            }
            if (c == '[') { // section
                tok.next(); // skip '['
                section = tok.getToken2(']', '\n', true);
                continue;
            }
            String key = tok.getToken2('=', '\n', true);
            if (tok.peek() == '=') tok.next();
            String val = tok.getToken('\n', true);
            val = val.replace("\\n", "\n");
            key = key.trim();
            val = val.trim();
            if (section.length() > 0) {
                key = section + SectionDelimiter + key;
            }
            items.put(key, val);
        }
    }

    public void clear() {
        items.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : items.keySet()) {
            String val = items.get(key);
            val = val.replace("\n","\\n");
            sb.append(key + "=" + val + "\n");
        }
        return sb.toString();
    }
    public void loadFromFile(String path) throws IOException {
        String src = KFile.load(path);
        parseStr(src);
    }
    public void saveToFile(String path) throws IOException {
        KFile.save(path, this.toString());
    }
}
