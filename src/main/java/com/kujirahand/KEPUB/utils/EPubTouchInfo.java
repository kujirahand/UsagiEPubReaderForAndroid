package com.kujirahand.KEPUB.utils;

import android.graphics.Rect;

/**
 * Created by kujira on 2016/04/10.
 */
public class EPubTouchInfo {
    public float start_x = -1;
    public float start_y = -1;
    //
    public float move_pos_x = -1;
    public float move_pos_y = -1;
    public float move_pos = 0;
    public float x;
    public float y;
    public Rect box = null;
    //
    private int longTapCounter = 0;
    private boolean is_down = false;

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        if (box == null) return x;
        return x - box.left;
    }
    public float getY() {
        if (box == null) return y;
        return y - box.top;
    }
    public float getStartX() {
        if (box == null) return start_x;
        return start_x - box.left;
    }
    public float getStartY() {
        if (box == null) return start_y;
        return start_y - box.top;
    }

    public void down(float x, float y) {
        this.start_x = x;
        this.start_y = y;
        this.longTapCounter = 0;
        this.is_down = true;
    }

    public void move(float x, float y) {
        move_pos_x = start_x - x;
        move_pos_y = start_y - y;
        move_pos = Math.abs(move_pos_x) + Math.abs(move_pos_y);
        this.is_down = true;
    }

    public void up(float x, float y) {
        is_down = false;
    }

    public boolean isDown() {
        return is_down;
    }

    public boolean isLongTap() {
        return (longTapCounter >= 10);
    }

    public int getLongTapCounter() {
        return longTapCounter;
    }

    public int incLongTapCounter() {
        return ++longTapCounter;
    }
}
