package com.kujirahand.KCSS;

import com.kujirahand.utils.KStrTokenizer;

/**
 * Created by kujira on 2016/04/18.
 */
public class CSSParser {

    final int CSS_NONE = 0;

    protected KStrTokenizer tok;
    protected CSSItems cssItems = new CSSItems();
    protected CSSItems curItems;

    protected String cur_elem;

    char[] SPACE_CHARS = new char[]{' ', '\t', '\r', '\n'};

    public CSSParser() {
    }

    public CSSItems getCSS() {
        return cssItems;
    }


    // 複数のCSSを読み込むことを想定しない場合
    public static CSSItems parseString(String src) {
        CSSParser parser = new CSSParser();
        CSSItems css = parser.parse(src);
        return css;
    }

    protected void setSource(String src) {
        tok = new KStrTokenizer(src);
        tok.reset();
    }

    public CSSItems parse(String src) {
        this.setSource(src);
        curItems = cssItems;
        tok.reset();
        parseLoop();
        return cssItems;
    }

    public void parseLoop() {
        while (!tok.isEOS()) {
            char c = tok.peek();
            if (c == '}') {
                tok.next();
                return;
            }
            readTop();
        }
    }

    protected void readTop() {
        tok.skipSpaceReturn();
        char c = tok.peek();
        // media query
        if (c == '@') { // not support
            if (tok.compareStr("@media")) {
                tok.next(6);
                String oldMedia = cssItems.mediaName;
                String newMedia = tok.getToken('{', true);
                curItems = cssItems.changeMedia(newMedia);
                parseLoop();
                cssItems.changeMedia(oldMedia);
                return;
            }
            if (tok.compareStr("@import")) { // not support
                tok.getToken(';', true);
                return;
            }
            if (tok.compareStr("@page")) { // not support
                tok.next(5);
                tok.skipSpaceReturn();
                if (tok.peek() == '{') {
                    tok.next();
                    cur_elem = "@page";
                    readKeyValueList();
                    return;
                }
             }
            // unknown
            tok.getToken('\n', true); // next line
            return;
        }
        if (c == '/') {
            skipComment();
        }
        cur_elem = tok.getTokenArray(SPACE_CHARS, true);
        cur_elem = cur_elem.trim();
        tok.skipSpaceReturn();
        char c2 = tok.peek();
        if (c2 == '{') {
            tok.next();
            readKeyValueList();
        }
    }

    protected void skipComment() {
        tok.skipSpaceReturn();
        if (tok.compareStr("/*")) {
            tok.getTokenStr("*/", true);
        }
    }


    char[] VALUE_DELIMITERS = new char[]{';','}'};
    protected void readKeyValueList() {
        StyleMap styles = curItems.getStyleItem(cur_elem);

        while (!tok.isEOS()) {
            tok.skipSpaceReturn();
            char c = tok.peek();
            if (c == '}') {
                tok.next();
                return;
            }
            if (c == '/') {
                skipComment();
                continue;
            }
            String key = tok.getToken(':', true);
            String val = tok.getTokenArray(VALUE_DELIMITERS, false);
            if (tok.peek() == ';') tok.next();
            key = key.trim();
            val = val.trim();
            styles.put(key, val);
        }
    }
}
