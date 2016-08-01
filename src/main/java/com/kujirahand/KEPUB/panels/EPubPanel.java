package com.kujirahand.KEPUB.panels;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.utils.EPubLinkArea;
import com.kujirahand.KEPUB.utils.SizeBox;
import com.kujirahand.KEPUB.utils.EPubLinkAreaList;
import com.kujirahand.KEPUB.utils.EPubTouchInfo;

/**
 * Created by kujira on 2016/04/09.
 */
public class EPubPanel {

    protected EPubView view;
    protected SizeBox size;
    protected Bitmap panel_bmp;
    protected Canvas panel_cv;
    protected String panelId;
    public boolean captureTouch = false;
    protected EPubLinkAreaList linkList = new EPubLinkAreaList();

    public EPubPanel(EPubView view, SizeBox size, String panelId) {
        this.view = view;
        this.size = size;
        this.panelId = panelId;
        log("@Panel.constructor=" + panelId);
    }

    public void initPanel() {
        createResource();
    }

    protected void createResource() {
        log("@createResource");
        if (panel_bmp != null) panel_bmp.recycle();
        int w = size.getOuterWidth();
        int h = size.getOuterHeight();
        if (w == 0) w = 2;
        if (h == 0) h = 2;
        panel_bmp = Bitmap.createBitmap(
                w, h,
                Bitmap.Config.ARGB_8888);
        panel_cv = new Canvas(panel_bmp);
    }

    public void log(String log) {
        view.log("#p["+panelId+"] " + log);
    }

    public void drawTo(Canvas root_cv) {
        root_cv.drawBitmap(panel_bmp, size.box.left, size.box.top, null);
    }

    public void drawPanel() {
        int ts = (int) view.dp2pxF(13);
        Paint p = new Paint();
        p.setColor(Color.RED);
        panel_cv.drawRect(panel_cv.getClipBounds(), p);
        p.setColor(Color.WHITE);
        p.setTextSize(ts);
        panel_cv.drawText("[TODO]", 10, ts, p);
    }

    public Canvas getPanelCanvas() {
        return this.panel_cv;
    }

    public void setSize(SizeBox newbox) {
        this.size = newbox;
        createResource();
    }

    public SizeBox getSize() {
        return this.size;
    }

    public int getOuterWidth() {
        return size.getOuterWidth();
    }

    public int getOuterHeight() {
        return size.getOuterHeight();
    }

    public int getInnerWidth() {
        return size.getInnerWidth();
    }

    public int getInnerHeight() {
        return size.getInnerHeight();
    }

    public void recycle() {
        if (panel_bmp != null) panel_bmp.recycle();
    }

    public boolean isHit(float x, float y) {
        return size.isHit(x, y);
    }

    public boolean onTouchDown(EPubTouchInfo e) {
        return true;
    }

    public boolean onTouchMove(EPubTouchInfo e) {
        return true;
    }

    public boolean onTouchUp(EPubTouchInfo e) {
        return true;
    }

    public boolean isShowing() {
        return (size.getOuterWidth() > 0) && (size.getOuterHeight() > 0);
    }

    public String lang(int id) {
        return view.getContext().getString(id);
    }
}
