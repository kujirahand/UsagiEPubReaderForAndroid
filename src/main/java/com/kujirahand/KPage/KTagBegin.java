package com.kujirahand.KPage;

import com.kujirahand.utils.StringMap;

/**
 * Created by kujira on 2016/04/07.
 */
public class KTagBegin {
    public int tagNo = 0;
    public String id;
    public String klass;
    public int pocketNo;
    private StringMap attr;
    private String unknownTagName = null;
    public KTagBegin parent = null;
    public KTagEnd tagEnd = null;


    public KTagBegin(String tag, StringMap attr) {
        tag = tag.toLowerCase();
        this.tagNo = KTagNo.fromString(tag);
        if (tagNo == KTagNo.UNKNOWN) unknownTagName = tag;
        setAttr(attr);
    }
    public KTagBegin(int tagNo) {
        this.tagNo = tagNo;
    }

    public void setAttr(StringMap map) {
        this.attr = map;
        if (map == null) {
            this.id = null;
            this.klass = null;
            return;
        }
        // allow attribute
        // id class
        String id = map.get("id");
        String klass = map.get("class");
        this.id = id;
        this.klass = klass;
    }

    public StringMap getAttr() {
        return this.attr;
    }

    public boolean isTag(int no) {
        return (this.tagNo == no);
    }

    public String getAttrValue(String key) {
        if (key.equals("id")) return this.id;
        if (key.equals("class")) return this.klass;
        return unescapeQuote(attr.get(key));
    }
    public void setAttrValue(String key, String val) {
        if (attr == null) attr = new StringMap();
        attr.put(key, val);
    }


    // Tag Info
    public static boolean isHxTag(int tagNo) {
        return (KTagNo.H1 <= tagNo && tagNo <= KTagNo.H6);
    }
    public static boolean isMetaTag(int tagNo) {
        return (KTagNo.HTML <= tagNo && tagNo < 0x10);
    }
    public static boolean isParagraphTag(int tagNo) {
        return ((0x10 <= tagNo) && (tagNo < 0x20)) || (tagNo == KTagNo.IMG);
    }
    public static boolean isLineTag(int tagNo) {
        return (0x20 <= tagNo) && (tagNo < 0x30);
    }

    public boolean isMetaTag() {
        return KTagBegin.isMetaTag(tagNo);
    }

    public String toString() {
        String tag = KTagNo.toString(tagNo);
        String s = "<" + tag;
        if (attr == null) {
            s += ">";
            return s;
        }
        if (tagNo == KTagNo.UNKNOWN) {
            attr.put("tagName", unknownTagName);
        }
        for (String key : attr.keySet()) {
            s += " " + key + "=" + "'" + escapeQuote(attr.get(key)) + "'";
        }
        s += ">";
        return s;
    }
    private String escapeQuote(String src) {
        if (src == null) return null;
        src = src.replace("\"", "&quot;");
        src = src.replace("'", "&apos;");
        return src;
    }
    private String unescapeQuote(String src) {
        if (src == null) return null;
        src = src.replace("&quot;", "\"");
        src = src.replace("&apos;", "'");
        return src;
    }

}
