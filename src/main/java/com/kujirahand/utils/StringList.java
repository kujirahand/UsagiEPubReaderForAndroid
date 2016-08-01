package com.kujirahand.utils;

import java.util.ArrayList;

/**
 * Created by kujira on 2016/04/03.
 */
public class StringList extends ArrayList<String> {
    public String join(String delimiter) {
        String result = "";
        int last = size() - 1;
        for (int i = 0; i <= last; i++) {
            result += get(i);
            if (i != last) result += delimiter;
        }
        return result;
    }

    public int indexOf(String value) {
        for (int i = 0; i < this.size(); i++) {
            if (value.equals(this.get(i))) return i;
        }
        return -1;
    }

    public void push(String value) { this.add(value); }

    public String pop() {
        if (size() == 0) return null;
        String s = get(size() - 1);
        this.remove(size() - 1);
        return s;
    }

    public static StringList split(String src, String delimiter) {
        StringList result = new StringList();
        while (src.length() > 0) {
            int i = src.indexOf(delimiter);
            if (i < 0) {
                result.add(src);
                break;
            }
            String sub = src.substring(0, i);
            result.add(sub);
            src = src.substring(i + delimiter.length());
        }
        return result;
    }
}
