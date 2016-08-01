package com.kujirahand.utils;

/**
 * Created by kujira on 2016/05/13.
 */
public class KStrTokenizer2 {

    final public static char EOS = 0;
    private StringBuilder src;
    private int len;
    private int pos;

    public KStrTokenizer2(CharSequence str) {
        setSource(str);
    }

    public void setSource(CharSequence src) {
        this.src = new StringBuilder(src);
        this.len = src.length();
        this.pos = 0;
    }

    public void reset() {
        this.pos = 0;
    }

    public char peek() {
        if (len <= pos) return EOS;
        return src.charAt(pos);
    }

    public void next() {
        pos++;
    }
    public void next(int len) {
        pos += len;
    }

    public void prev() {
        pos--;
    }
    public int getPosition() {
        return pos;
    }

    public char getChar() {
        if (len <= pos) return 0;
        return src.charAt(pos++);
    }

    public void skipSpace() {
        while (len > pos) {
            if (peek() == ' ' || peek() == '\t') {
                pos++;
                continue;
            }
            break;
        }
    }

    public void skipSpaceReturn() {
        while (len > pos) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                pos++;
                continue;
            }
            break;
        }
    }

    public void skipReturn() {
        while (len > pos) {
            char c = peek();
            if (c == '\r' || c == '\n') {
                pos++;
                continue;
            }
            break;
        }
    }

    public boolean isEOS() {
        return (len <= pos);
    }

    public String getToken(char delimiter, boolean skipDelimiter) {
        StringBuilder sb = new StringBuilder();
        while (pos < len) {
            char c = peek();
            if (c == delimiter) {
                if (skipDelimiter) pos++;
                break;
            }
            sb.append(c);
            pos++;
        }
        return sb.toString();
    }

    public String getTokenArray(char[] delimiters, boolean skipDelimiter) {
        StringBuilder sb = new StringBuilder();
        while (pos < len) {
            char c = peek();
            boolean flag_match = false;
            for (char de : delimiters) {
                if (c == de) {
                    flag_match = true;
                    break;
                }
            }
            if (flag_match) {
                if (skipDelimiter) pos++;
                break;
            }
            sb.append(c);
            pos++;
        }
        return sb.toString();
    }

    public String getToken2(char delim1, char delim2, boolean skipDelimiter) {
        StringBuilder sb = new StringBuilder();
        while (pos < len) {
            char c = peek();
            if (c == delim1 || c == delim2) {
                if (skipDelimiter) pos++;
                break;
            }
            sb.append(c);
            pos++;
        }
        return sb.toString();
    }
    public String getToken3(char delim1, char delim2, char delim3, boolean skipDelimiter) {
        StringBuilder sb = new StringBuilder();
        while (pos < len) {
            char c = peek();
            if (c == delim1 || c == delim2 || c == delim3) {
                if (skipDelimiter) pos++;
                break;
            }
            sb.append(c);
            pos++;
        }
        return sb.toString();
    }

    public String getTokenStr(String delimiter, boolean skipDelimiter) {
        StringBuilder sb = new StringBuilder();
        char delimiter_c = delimiter.charAt(0);
        while (pos < len) {
            char c = peek();
            if (c == delimiter_c) {
                int i2 = pos + delimiter.length();
                if (i2 > src.length()) i2 = src.length();
                String sub = src.substring(pos, i2);
                if (sub.equals(delimiter)) {
                    if (skipDelimiter) pos += delimiter.length();
                    break;
                }
            }
            sb.append(c);
            pos++;
        }
        return sb.toString();
    }

    public boolean compareStr(String target) {
        String sub = src.substring(pos, pos + target.length());
        return target.equals(sub);
    }

    public String getCurStr(int len) {
        int last = pos + len;
        if (last > src.length()) last = src.length();
        String r = src.substring(pos, last);
        pos = last;
        return r;
    }
}
