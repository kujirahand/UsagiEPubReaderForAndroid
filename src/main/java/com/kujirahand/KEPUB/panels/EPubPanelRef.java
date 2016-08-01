package com.kujirahand.KEPUB.panels;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.utils.SizeBox;
import com.kujirahand.KEPUB.async.EPubPageLoaderAsync;
import com.kujirahand.KEPUB.utils.EPubLinkArea;
import com.kujirahand.KEPUB.EPubTextDrawer;
import com.kujirahand.KEPUB.utils.EPubTouchInfo;
import com.kujirahand.KPage.KCharMeasureIF;
import com.kujirahand.KPage.KTagBegin;
import com.kujirahand.usagireader.R;
import com.kujirahand.utils.DialogCallback;
import com.kujirahand.utils.DialogHelper;

import java.io.IOException;

// TODO: ★参照パネルのリンクをクリックできるようにする

/**
 * Created by kujira on 2016/04/14.
 */
public class EPubPanelRef extends EPubPanelViewBase implements KCharMeasureIF {

    protected Paint ref_back_p = new Paint();
    protected Paint left_line_p = new Paint();
    public Paint text_p = new Paint();
    protected Rect fontR = new Rect();

    private int lineCount;
    private float fontHeight;
    private float fontSizePx;
    private float tool_h;
    private EPubTextDrawer drawText;
    private Bitmap close_bmp = null;
    private Bitmap reload_bmp = null;
    private Bitmap swap_bmp = null;

    public EPubPanelRef(EPubView view, SizeBox size, String panelId) {
        super(view, size, panelId);
    }

    public void changeFontSize() {
        fontSizePx = view.text_p.getTextSize() * 0.7f;
        view.log("refPanel.textSize=" + fontSizePx);
        text_p.setTypeface(view.normal_tf);
        text_p.setTextSize(fontSizePx);
        text_p.setAntiAlias(true);
        text_p.setColor(view.textColor);
        text_p.getTextBounds("愛gM",0, 2, fontR);
        fontHeight = fontR.height() * 1.4f;
        //
        ref_back_p.setColor(view.backRefColor);
        left_line_p.setColor(view.borderColor);
        //
        lineCount = (int)Math.floor(size.getInnerHeight() /fontHeight);
        drawText = new EPubTextDrawer(view, this.size, lineCount);
    }

    @Override
    protected void createResource() {
        super.createResource();
        changeFontSize();
        //
        tool_h = view.dp2px(30);
        this.size.margin.top = (int)tool_h;
        //
        close_p.setAntiAlias(true);
        close_p.setFilterBitmap(true);

    }

    @Override
    public void drawPanel() {
        // draw nothing
        // back
        panel_cv.drawRect(panel_cv.getClipBounds(), ref_back_p);
        // left line
        panel_cv.drawLine(0, 0, 0, size.box.bottom, left_line_p);
        // draw close button
        drawRefHeadButton();
    }

    @Override
    public void setScrollTop(int scrollTop) {
        if (this.page == null) {
            log("refPanel.setScrollTop) sorry, page==null)");
            return;
        }
        drawText.setFontSizePx(fontHeight, fontSizePx);

        log("refPanel.setScrollTop=" + scrollTop + "(" + page.title + ")");
        if (scrollTop < 0) scrollTop = 0;
        if (scrollTop >= page.size()) scrollTop = page.size() - 1;
        this.scrollTop = scrollTop;

        // draw text
        drawText.text_p = this.text_p;
        drawText.back_p.setColor(view.backRefColor);
        drawText.drawPage(page, panel_cv, scrollTop, this.linkList);

        // left line
        panel_cv.drawLine(0, 0, 0, size.box.bottom, left_line_p);

        // draw close button
        drawRefHeadButton();

        view.invalidate();
    }

    private Paint close_p = new Paint();
    private void drawRefHeadButton() {
        float spc = view.dp2px(4);
        float ih = tool_h - spc * 2;
        float x = size.box.width() - ih - spc;
        float y = spc;

        if (close_bmp == null) {
            close_bmp = BitmapFactory.decodeResource(view.getResources(), R.mipmap.r_close_btn);
        }
        Rect close_src_r = new Rect(0,0, close_bmp.getWidth(), close_bmp.getHeight());
        RectF close_des_r = new RectF(x, y, x + ih, y + ih);
        panel_cv.drawBitmap(close_bmp, close_src_r, close_des_r, close_p);

        EPubLinkArea close_a = new EPubLinkArea("@close");
        close_a.setRectFCopy(close_des_r);
        linkList.add(close_a);

        // swap_btn
        if (swap_bmp == null) {
            swap_bmp = BitmapFactory.decodeResource(view.getResources(), R.mipmap.r_swap_btn);
        }
        x = spc;
        Rect swap_src_r = new Rect(0,0, swap_bmp.getWidth(), swap_bmp.getHeight());
        RectF swap_des_r = new RectF(x, y, x + ih, y + ih);
        panel_cv.drawBitmap(swap_bmp, swap_src_r, swap_des_r, close_p);

        EPubLinkArea swap_a = new EPubLinkArea("@swap");
        swap_a.setRectFCopy(swap_des_r);
        linkList.add(swap_a);

        /*
        // reload_btn
        x += swap_des_r.width() + spc;
        if (reload_bmp == null) {
            reload_bmp = BitmapFactory.decodeResource(view.getResources(), R.mipmap.reload);
        }
        Rect reload_src_r = new Rect(0,0, reload_bmp.getWidth(), reload_bmp.getHeight());
        RectF reload_des_r = new RectF(x, y, x + ih, y + ih);
        panel_cv.drawBitmap(reload_bmp, reload_src_r, reload_des_r, close_p);

        EPubLinkArea reload_a = new EPubLinkArea("@reload");
        reload_a.setRectFCopy(reload_des_r);
        linkList.add(reload_a);
        */

    }

    @Override
    public boolean onTouchUp(EPubTouchInfo e) {
        float x = e.getX();
        float y = e.getY();

        if (page == null) return false;
        if (topMoved) return false;

        EPubLinkArea area = linkList.getHitLink(x, y);
        if (area == null) {
            log("no link = " + linkList.size());
        } else {
            if (area.href.equals("@close")) {
                view.showRefPanel(false, null);
                return true;
            }
            if (area.href.equals("@reload")) {
                reloadPage();
                return true;
            }
            if (area.href.equals("@swap")) {
                view.viewPanel.swapPage(this.page, scrollTop);
                return true;
            }
            if (area.href.equals("@marker")) {
                editMarkerMemo(area);
                return true;
            }
            view.viewPanel.showLinkPage(area, false);
            return true;
        }

        return false;
    }

    public void editMarkerMemo(EPubLinkArea area) {
        int idx = page.source.findTagById(area.linkId);
        if (idx < 0) return;
        final KTagBegin tag = (KTagBegin)page.source.get(idx);
        final String memo = tag.getAttrValue("memo");
        DialogHelper.memoDialog("Memo", memo, new DialogCallback() {
            @Override
            public void dialogResult(Object which) {
                String newMemo = (String)which;
                if (newMemo.equals(memo)) return;
                tag.setAttrValue("memo", newMemo);
                // save & redraw
                try {
                    page.isModified = true;
                    view.epubFile.savePage(page);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void dialogCancel() {
            }
        });

    }

    private int down_top = 0;
    private boolean topMoved = false;

    @Override
    public boolean onTouchDown(EPubTouchInfo e) {
        down_top = scrollTop;
        topMoved = false;
        return false;
    }

    @Override
    public boolean onTouchMove(EPubTouchInfo e) {
        if (page == null) return true;

        if (e.move_pos < fontHeight) {
            return true;
        }
        int line_add = (int)(e.move_pos_y / fontHeight);
        scrollTop = down_top + line_add;
        setScrollTop(scrollTop);
        topMoved = true;

        // TODO:アニメーションで行の差分を表示
        /*
        float y_real = line_add * fontHeight;
        float yinc = y_real - e.move_pos_y;
        panel_cv.drawBitmap(panel_bmp, 0, yinc, null);
        // 上記だと、panel_bmpをずらすことなので、画面が崩れる
        // 実装するなら、新たにbmpを一画面増やしてそれを下げるようにする
        */

        return false;
    }

    public void onLongTap(EPubTouchInfo e) {
        /*
        // ロングタップでこのページを表示するをやめる
        int cur_idx = page.lineNo2index(scrollTop);
        EPubLinkArea area = new EPubLinkArea();
        area.curPos = cur_idx;
        area.href = page.path;
        view.viewPanel.showLinkPage(area, true);
        */
        final EPubPanelRef self = this;
        String[] list = new String[]{
                /* 0 */ lang(R.string.SWAP_LR_PANEL),
                /* 1 */ lang(R.string.RELOAD)
        };
        DialogHelper.selectListNo("", "", list, new DialogCallback(){
            @Override
            public void dialogResult(Object which) {
                int no = (Integer)which;
                if (no == 0) {
                    view.viewPanel.swapPage(self.page, scrollTop);
                    return;
                }
                if (no == 1) {
                    view.viewPanel.realodRefPage();
                    return;
                }
            }
            @Override
            public void dialogCancel() {

            }
        });
    }

    @Override
    public float getStrWidth(String str) {
        return this.text_p.measureText(str);
    }

    @Override
    public int getFrameWidth() {
        return size.getInnerWidth();
    }

    @Override
    public int getLineCount() {
        return this.lineCount;
    }

    @Override
    public void reloadPage() {
        if (page == null) return;
        String full_path = page.path;
        int cur_index = page.lineNo2index(scrollTop);
        log("cur_index=" + cur_index);
        //
        EPubPageLoaderAsync loader = new EPubPageLoaderAsync(view, full_path, cur_index, null, this);
        loader.useCache = false; // for refresh page
        loader.execute();
    }
}
