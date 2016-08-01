package com.kujirahand.KEPUB.utils;

import android.graphics.Rect;

/**
 * Created by kujira on 2016/04/07.
 */
public class SizeBox {
    public Rect margin;
    public Rect box;

    public SizeBox() {
        margin = new Rect();
        box = new Rect();
    }

    public int getOuterWidth() {
        return box.width();
    }
    public int getOuterHeight() {
        return box.height();
    }
    public int getInnerWidth() {
        return (box.right - box.left) - margin.left - margin.right;
    }
    public int getInnerHeight() {
        return box.bottom - box.top - margin.top - margin.bottom;
    }

    public boolean isHit(float x, float y) {
        return (box.left <= x && x <= box.right && box.top <= y && y <= box.bottom);
    }
}
