package com.kujirahand.KPage;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by kujira on 2016/04/06.
 */
public class KCharList extends ArrayList<Object> {
    private int pos = 0;
    public void setPosition(int pos) {
        if (pos < 0) pos = 0;
        this.pos = pos;
    }
    public int getPosition() {
        return pos;
    }
    public boolean isEOF() {
        return (pos >= size());
    }
    public Object next() {
    	return this.get(pos++);
    }
    public void prev() {
        if (pos < 0) pos = 0;
        pos--;
    }
    public Object peek() {
        if (pos >= size()) return null;
        return this.get(pos);
    }
    public Object peekNext() {
        int i = pos + 1;
        if (i >= size()) return null;
        return this.get(i);
    }
    public boolean isChar() {
        if (isEOF()) return false;
        Object n = this.get(pos);
        return (n instanceof Character);
    }
    public void appendStr(String s) {
    	for (int i = 0; i < s.length(); i++) {
    		Character c = s.charAt(i);
            // remove control char
			switch (Character.getType(c)) {
                case Character.PRIVATE_USE:
                case Character.FORMAT:
                case Character.UNASSIGNED:
                case Character.CONTROL:
                    continue;

			}
    		this.add(c);
    	}
    }
    public String getOnlyText() {
        String res = "";
        for (Object o : this) {
            if (o instanceof Character) {
                res += o;
                continue;
            }
        }
        return res;
    }
    public int getTextLen() {
        int res = 0;
        for (Object o : this) {
            if (o instanceof Character) {
                res++;
                continue;
            }
        }
        return res;
    }

    public KCharList substring(KLineInfo info) {
        return substring(info.start, info.end);
    }
    public KCharList substring(int index, int endIndex) {
        KCharList list = new KCharList();
        for (int i = index; i < endIndex; i++) {
            Object o = this.get(i);
            list.add(o);
        }
        return list;
    }
    public String substringOnlyStr(int index, int endIndex) {
        StringBuilder sb = new StringBuilder();
        KCharList list = new KCharList();
        for (int i = index; i < endIndex; i++) {
            Object o = this.get(i);
            if (o instanceof Character) sb.append(o);
        }
        return sb.toString();
    }

    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for (Object o : this) {
    		if (o instanceof Character) {
                sb.append((char)o);
    			continue;
    		}
    		if (o instanceof KTagBegin) {
    			KTagBegin tb = (KTagBegin)o;
                sb.append(tb.toString());
    			continue;
    		}
    		if (o instanceof KTagEnd) {
                KTagEnd te = (KTagEnd)o;
                sb.append("</");
                sb.append(te.getTagName());
                sb.append(">");
                continue;
    		}
    	}
    	return sb.toString();
    }

    public int getIndexCharByPos(float x, KCharMeasureIF mif) {
        int sz = this.size();
        String s;
        float cur_x = 0;
        for (int i = 0; i < sz; i++) {
            Object ch = get(i);
            if (!(ch instanceof Character)) continue;
            s = "" + (char)ch;
            cur_x += mif.getStrWidth(s);
            if (x < cur_x) return i;
        }
        return (sz > 0) ? sz - 1 : 0;
    }

    public int findTag(int tagNo, int startIndex) {
        int i = startIndex;
        int len = this.size();
        while (i < len) {
            Object o = get(i);
            if (o instanceof KTagBegin) {
                KTagBegin t = (KTagBegin)o;
                if (t.tagNo == tagNo) return i;
            }
            i++;
        }
        return -1;
    }
    public int findCloseTag(int tagNo, int startIndex) {
        int i = startIndex;
        int len = this.size();
        while (i < len) {
            Object o = get(i);
            if (o instanceof KTagEnd) {
                KTagEnd t = (KTagEnd)o;
                if (t.tagBegin.tagNo == tagNo) return i;
            }
            i++;
        }
        return -1;
    }
    public String findTagText(int tagNo, int startIndex) {
        int i = findTag(tagNo, startIndex);
        if (i < 0) return "";
        int j = findCloseTag(tagNo, i);
        if (j < 0) return "";
        return substringOnlyStr(i, j);
    }

    public int findTagById(String id) {
        return findTagById(id, 0);
    }
    public int findTagById(String id, int index) {
        return findTagById(id, index, this.size());
    }
    public int findTagById(String id, int index, int endIndex) {
        for (int i = index; i < endIndex; i++) {
            Object o = get(i);
            if (o instanceof KTagBegin) {
                KTagBegin t = (KTagBegin)o;
                if (t.id == null) continue;
                if (t.id.equals(id)) return i;
            }
        }
        return -1;
    }
    public int findObject(Object o, int index, int endIndex) {
        for (int i = index; i < endIndex; i++) {
            Object fo = get(i);
            if (o == fo) return i;
        }
        return -1;
    }

    // TODO: このメソッドのどこかにバグがある - 一時しようしないことに
    public KCharList getContinueTag(int index) {
        KCharList res = new KCharList();
        Stack<KTagEnd> tags = new Stack<KTagEnd>();
        int i = index;
        while (i >= 0) {
            Object o = get(i);
            if (o instanceof Character) {
                i--;
                continue;
            }
            if (o instanceof KTagEnd) {
                tags.push((KTagEnd)o);
                i--;
                continue;
            }
            KTagBegin tagBegin = (KTagBegin)o;
            if (tags.size() > 0 && tagBegin.tagEnd == tags.peek()) {
                tags.pop();
                i--;
                continue;
            }
            res.add(tagBegin);
            while (tagBegin.parent != null) {
                res.add(tagBegin.parent);
                tagBegin = tagBegin.parent;
            }
            break;
        }
        return res;
    }
}
