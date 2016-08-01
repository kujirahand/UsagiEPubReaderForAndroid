package com.kujirahand.KPage;

import com.kujirahand.utils.KFile;
import com.kujirahand.utils.KStrTokenizer;
import com.kujirahand.utils.StringMap;

import java.io.IOException;
import java.util.Stack;

/**
 * Created by kujira on 2016/05/13.
 */
public class KTagLoader {
    private KCharList charList;
    private KStrTokenizer tok;

    public static KCharList load(String path) throws IOException {
        KTagLoader loader = new KTagLoader();
        return loader.loadFromFile(path);
    }
    public static KCharList loadFromStr(String src) {
        KTagLoader loader = new KTagLoader();
        return loader.parseStr(src);
    }

    public KTagLoader() {
        charList = new KCharList();
    }
    public KCharList loadFromFile(String path) throws IOException {
        charList.clear();
        String src = KFile.load(path);
        tok = new KStrTokenizer(src);
        parse();
        return charList;
    }
    public KCharList parseStr(CharSequence src) {
        charList.clear();
        tok = new KStrTokenizer(src);
        parse();
        return charList;
    }
    private void parse() {
        // doctype
        tok.skipSpaceReturn();
        if (tok.compareStr("<!")) parseDocType();
        if (tok.compareStr("<?")) parseDocType();
        tok.skipSpaceReturn();

        // read until end
        String tagName, tmpName;
        KTagBegin tagBegin;
        KTagEnd tagEnd;
        Stack<KTagBegin> stack = new Stack<KTagBegin>();
        KStrTokenizer tok2 = new KStrTokenizer("");
        boolean hasSpace = false;
        while (!tok.isEOS()) {
            char ch = tok.peek();
            if (ch == ' ') {
                hasSpace = true;
                tok.next();
                tok.skipSpaceReturn();
                continue;
            }
            if (ch == '\r' || ch == '\n' || ch == '\t') {
                tok.next();
                tok.skipSpaceReturn();
                continue;
            }
            // tag
            if (ch == '<') {
                hasSpace = false;
                tok.next();
                char c2 = tok.peek();
                if (c2 == '!') { // comment
                    if (tok.compareStr("--")) parseComment(); // <!-- ... -->
                    else if (tok.compareStr("[")) parseCDATA(); // <![CDATA[ .. ]>
                    else tok.getTokenStr(">", true);
                    continue;
                }
                // close tag
                if (c2 == '/') {
                    tok.next();
                    tmpName = tok.getToken('>', true);
                    if (stack.size() == 0) continue; // タグが入れ子になっていなければ無視
                    tagBegin = stack.peek();
                    int endTagNo = KTagNo.fromString(tmpName);
                    if (endTagNo == tagBegin.tagNo) {
                        stack.pop();
                        tagEnd = new KTagEnd(tagBegin);
                        charList.add(tagEnd);
                        continue;
                    }
                    // 段落の終わりがある場合
                    if (endTagNo == KTagNo.DIV || endTagNo == KTagNo.P) {
                        while (stack.size() > 0) {
                            tagBegin = stack.peek();
                            if (tagBegin.tagNo == endTagNo) {
                                stack.pop(); //
                                charList.add(new KTagEnd(tagBegin));
                                break;
                            }
                            if (tagBegin.tagNo <= 0x9) break; // html/body/head
                            stack.pop();
                            charList.add(new KTagEnd(tagBegin));
                        }
                        continue;
                    }
                    // ここに来るのは、Openタグが無かったエラータグの場合
                    // System.out.println("error.tag=</" + tmpName + ">");
                    continue;
                }
                // begin tag
                String src2 = tok.getToken('>', true);
                tok2.setSource(src2);
                tok2.skipSpaceReturn();
                tmpName = tok2.getNameToken();
                int tagNo = KTagNo.fromString(tmpName);
                tagBegin = new KTagBegin(tagNo);
                if (stack.size() > 0) {
                    tagBegin.parent = stack.peek();
                }
                stack.push(tagBegin);
                this.charList.add(tagBegin);
                StringMap attr = new StringMap();
                while (!tok2.isEOS()) {
                    tok2.skipSpaceReturn();
                    if (tok2.peek() == '/') { // 終了タグ?
                        tagEnd = new KTagEnd(tagBegin);
                        charList.add(tagEnd);
                        stack.pop(); // pop begin tag
                        break;
                    }
                    String key = tok2.getToken3(' ', '\t', '=', false);
                    String val = "";
                    tok2.skipSpaceReturn();
                    char eq = tok2.peek();
                    if (eq == '=') {
                        tok2.next(); // =
                        tok2.skipSpaceReturn();
                        char qu = tok2.peek();
                        if (qu == '"') {
                            tok2.next();
                            val = tok2.getToken('"', true);
                        }
                        else if (qu == '\'') {
                            tok2.next();
                            val = tok2.getToken('\'', true);
                        }
                        attr.put(key, val);
                    }
                }
                if (attr.size() > 0) tagBegin.setAttr(attr);
                continue;
            }
            // text
            if (hasSpace) {
                hasSpace = false;
                charList.add(' ');
            }
            charList.add(ch);
            tok.next();
        }
    }
    private void parseDocType() {
        // skip doctype
        tok.getToken('>', true);
    }
    private void parseComment() {
        // skip comment
        tok.getTokenStr("-->", true);
    }
    private void parseCDATA() {
        // skip comment
        tok.getTokenStr("]>", true);
    }
}
