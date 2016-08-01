package com.kujirahand.KEPUB.panels;


import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.utils.SizeBox;
import com.kujirahand.KEPUB.utils.EPubDrawPos;
import com.kujirahand.KEPUB.utils.EPubLinkArea;
import com.kujirahand.KEPUB.utils.EPubTouchInfo;

/**
 * Created by kujira on 2016/04/09.
 */

public class EPubPanelStatus extends EPubPanel {

    final int COLOR_BTN_ACTIVE = Color.rgb(100, 100, 150);
    final int COLOR_BTN_INACTIVE = Color.rgb(230, 230, 230);
    private String pageInfo;
    private String backPath = null;
    private Paint info_back_p = new Paint();
    private Paint info_text_p = new Paint();
    private Paint border_p = new Paint();
    private Rect fontR = new Rect();
    Paint btn_p = new Paint();
    private float btn_h;
    private float btn_inner_margin;

    public EPubPanelStatus(EPubView view, SizeBox size, String panelId) {
        super(view, size, panelId);
    }

    @Override
    public void createResource() {
        super.createResource();
        //
        info_text_p.setAntiAlias(true);
        info_text_p.setTextSize(view.dp2px(10));
        //
        info_back_p.getTextBounds("C", 0, 1, fontR);
        //
        btn_p.setAntiAlias(true);
        //
        btn_h = size.getInnerHeight();
        btn_inner_margin = view.dp2px(6);
    }

    @Override
    public void drawPanel() {
        // 色の更新
        info_back_p.setColor(view.backColor);
        info_text_p.setColor(view.textColor);
        border_p.setColor(view.borderColor);
        // 背景
        panel_cv.drawRect(panel_cv.getClipBounds(), info_back_p);
        linkList.clear();
        // 上側のline
        border_p.setStrokeWidth(1);
        panel_cv.drawLine(0, 0, size.box.right, 0, border_p);

        // btns
        EPubDrawPos cur = new EPubDrawPos(0, 0);
        cur.x = size.margin.left;
        // drawBackButton(cur);
        cur.x += view.dp2px(5);
        //
        // drawListButton(cur);
        cur.x += view.dp2px(5);

        // text
        cur.y = (size.getOuterHeight() - fontR.height()) / 2 + fontR.height();
        panel_cv.drawText(pageInfo, cur.x, cur.y, info_text_p);

    }

    private void drawListButton(EPubDrawPos cur) {
        // Buttons outer
        float x1 = cur.x;
        float y1 = size.margin.top;
        float x2 = x1 + btn_h;
        float y2 = y1 + btn_h;
        RectF btn = new RectF(x1,y1, x2,y2);
        // 外枠
        float m = btn_inner_margin;
        float ix1 = x1 + m;
        float iy1 = y1 + m;
        float ix2 = x2 - m;
        float iy2 = y2 - m;
        RectF i_rect = new RectF(ix1, iy1, ix2, iy2);
        float i_round = view.dp2px(3);
        btn_p.setColor(COLOR_BTN_ACTIVE);
        btn_p.setStyle(Paint.Style.STROKE);
        btn_p.setStrokeWidth(1);
        panel_cv.drawRoundRect(i_rect, i_round, i_round, btn_p);

        // [ミ]
        btn_p.setStrokeWidth(1);
        btn_p.setStrokeJoin(Paint.Join.ROUND);
        float h = i_rect.height() / 4;
        for (int i = 1; i <= 3; i++) {
            float line_x1 = ix1 + m;
            float line_y1 = iy1 + i * h;
            float line_x2 = ix2 - m;
            panel_cv.drawLine(line_x1, line_y1, line_x2, line_y1, btn_p);
        }

        // link
        EPubLinkArea btn_area = new EPubLinkArea("@TableOfContents");
        btn_area.setRectFCopy(btn);
        linkList.add(btn_area);
        //
        cur.x += btn.width();
    }

    private void drawBackButton(EPubDrawPos cur) {
        // Buttons outer
        float x1 = cur.x;
        float y1 = size.margin.top;
        float x2 = x1 + btn_h;
        float y2 = y1 + btn_h;
        RectF btn = new RectF(x1,y1, x2,y2);

        // Arrow
        float m = btn_inner_margin;
        float bx1 = x1 + m;
        float by1 = y1 + btn_h / 2;
        float bx2 = x2 - m;
        float by2 = y2 - m;
        float bx3 = bx2;
        float by3 = y1+m;
        btn_p.setStrokeWidth(1);
        if (view.viewPanel.canHistoryBack()) {
            btn_p.setColor(COLOR_BTN_ACTIVE);
        } else {
            btn_p.setColor(COLOR_BTN_INACTIVE);
        }
        btn_p.setStyle(Paint.Style.STROKE);
        Path tri = new Path();
        tri.moveTo(bx1, by1);
        tri.lineTo(bx2, by2);
        tri.lineTo(bx3, by3);
        tri.close();
        panel_cv.drawPath(tri, btn_p);

        // link
        EPubLinkArea btn_area = new EPubLinkArea("@back");
        btn_area.setRectFCopy(btn);
        linkList.add(btn_area);

        cur.x += btn.width();
    }

    public void setChapterNo(int chapter, int chapter_size, int page, int page_size, String title) {
        pageInfo = "";
        {
            pageInfo += "Ch." + chapter + "/" + chapter_size + " ";
            pageInfo += "(p." + page + "/" + page_size + ") ";
        }
        pageInfo += " - " + title;
        log(pageInfo);
        drawPanel();
    }

    @Override
    public boolean onTouchUp(EPubTouchInfo e) {
        for (EPubLinkArea link : linkList) {
            if (link.isHit(e.getX(), e.getY())) {
                if (link.href.equals("@back")) {
                    view.log("back button pressed");
                    view.viewPanel.backHistory();
                    return true;
                }
                if (link.href.equals("@TableOfContents")) {
                    view.viewPanel.showLinkPage(link, false);
                    return true;
                }
            }
        }
        return false;
    }
}
