package com.kujirahand.KEPUB.utils;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by kujira on 2016/04/12.
 */
public class EPubLinkArea {
    public Rect rect = new Rect();
    public String href;
    public String linkType = "";
    public int curPos = 0;
    public String linkId = "";

    public EPubLinkArea() {
    }
    public EPubLinkArea(EPubLinkArea pre) {
        copyFrom(pre);
    }

    public void copyFrom(EPubLinkArea pre) {
        this.setRectCopy(pre.rect);
        this.href = pre.href;
        this.linkType = pre.linkType;
        this.curPos = pre.curPos;
        this.linkId = pre.linkId;
    }

    public void setRectCopy(Rect r) {
        rect.top = r.top;
        rect.left = r.left;
        rect.right = r.right;
        rect.bottom = r.bottom;
    }

    public void setRectFCopy(RectF r) {
        rect.top = (int)r.top;
        rect.left = (int)r.left;
        rect.right = (int)r.right;
        rect.bottom = (int)r.bottom;
    }

    public void shiftAreaX(int shift) {
        rect.left += shift;
        rect.right += shift;
    }

    public EPubLinkArea(String href) {
        this.href = href;
    }
    public boolean isHit(float x, float y) {
        return (rect.left <= x && x <= rect.right && rect.top <= y && y <= rect.bottom);
    }
    public boolean isNoteref() {
        return (linkType.equals("noteref") || linkType.equals("footnote"));
    }
}
