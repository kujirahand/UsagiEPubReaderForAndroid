package com.kujirahand.KPage;

/**
 * Created by kujira on 2016/04/07.
 */
public class KTagEnd {
    public KTagBegin tagBegin = null;

    public KTagEnd(KTagBegin tagBegin) {
        if (tagBegin == null) throw new NullPointerException("It needs KTagBegin");
        this.tagBegin = tagBegin;
        tagBegin.tagEnd = this;
    }
    public boolean isTag(int no) { return tagBegin.tagNo == no; }
    public int getTagNo() {
        return tagBegin.tagNo;
    }
    public String getTagName() {
        return KTagNo.toString(tagBegin.tagNo);
    }
    public String getId() {
        if (tagBegin == null) return null;
        return tagBegin.id;
    }
    public boolean isHxTag() {
        return KTagBegin.isHxTag(tagBegin.tagNo);
    }
    public String toString() {
        return "</" + KTagNo.toString(tagBegin.tagNo) + ">";
    }
}
