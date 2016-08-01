package com.kujirahand.KEPUB.panels;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.utils.EPubLinkArea;
import com.kujirahand.usagireader.R;

/**
 * Created by kujira on 2016/05/06.
 */
public class EPubMarkerTool {
    final private int tool_count = 6;
    public boolean visible;
    public EPubLinkArea area = null;
    private Bitmap tool_bmp;
    private Canvas tool_cv;
    private EPubView view;
    public float width;
    public float height;
    private float x;
    private float y;
    private float sippo_x;
    private float sippo;
    private float icon_width;
    private Rect toolRect = new Rect();
    private int selectedIndex = -1;
    private Paint back_p;

    public EPubMarkerTool(EPubView view) {
        this.view = view;
        visible = false;

        icon_width = view.dp2px(45);
        sippo = view.dp2px(15);
        width = tool_count * icon_width;
        height = icon_width;
        tool_bmp = Bitmap.createBitmap((int)width, (int)(icon_width+sippo), Bitmap.Config.ARGB_8888);
        tool_cv = new Canvas(tool_bmp);

        float xx = 0;
        float round_px = view.dp2px(5);
        back_p = new Paint();
        back_p.setFilterBitmap(true);
        back_p.setColor(Color.argb(230, 255,255,255));
        back_p.setAntiAlias(true);
        RectF r = new RectF(0, 0, width, height);
        tool_cv.drawRoundRect(r, round_px, round_px, back_p);

        Paint p = new Paint();
        p.setFilterBitmap(true);
        p.setAntiAlias(true);
        // icon
        int[] icon_ids = new int[]{
                R.mipmap.mt_edit,
                R.mipmap.mt_remove,
                R.mipmap.mt_ul
        };
        float spc = icon_width * 0.1f;
        xx = 0;
        for (int i = 0; i < 3; i++) {
            Bitmap bmp = BitmapFactory.decodeResource(view.getResources(), icon_ids[i]);
            Rect src_r = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
            RectF dst_r = new RectF(xx + spc, 0 + spc, xx + icon_width - spc*2, icon_width - spc*2);
            tool_cv.drawBitmap(bmp, src_r, dst_r, p);
            xx += icon_width;
        }
        // color
        float rr = icon_width * 0.3f;
        int[] colors = new int[]{ Color.RED, Color.YELLOW, Color.BLUE};
        for (int j = 0; j < 3; j++) {
            p.setColor(colors[j]);
            float cx = xx + (icon_width / 2);
            tool_cv.drawCircle(cx, icon_width / 2, rr, p);
            xx += icon_width;
        }
    }

    public boolean isHit(float x, float y) {
        if (toolRect.left <= x && x <= toolRect.right &&
                toolRect.top <= y && y <= toolRect.bottom) {
            float xx = x - toolRect.left;
            selectedIndex = (int)Math.floor(xx / icon_width);
            return true;
        }
        selectedIndex = -1;
        return false;
    }

    public void show(Canvas canvas, float x, float y, float sippo_x) {
        visible = true;
        this.x = x;
        this.y = y;
        this.sippo_x = sippo_x;
        toolRect = new Rect((int)this.x, (int)this.y, (int)(this.x + width), (int)(this.y + height));
        // icons
        draw(canvas);
        view.invalidate();
    }
    protected void draw(Canvas panel_cv) {
        if (!visible) return;
        panel_cv.drawBitmap(tool_bmp, x, y, null);
        // TODO: sippoの表示位置を調節する(右端のときとか)
        // sippo
        float ccx = width / 2;
        float cxw = view.dp2px(5);
        Path path = new Path();
        path.moveTo(x+ccx,       y+icon_width + sippo);
        path.lineTo(x+ccx - cxw, y+icon_width);
        path.lineTo(x+ccx + cxw, y+icon_width);
        path.close();
        panel_cv.drawPath(path, back_p);

    }
    public int getSelectedIndex() {
        return this.selectedIndex;
    }
}
