package com.kujirahand.KEPUB.panels;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.utils.SizeBox;
import com.kujirahand.KEPUB.menu.MainMenu;
import com.kujirahand.KEPUB.utils.EPubLinkArea;
import com.kujirahand.KEPUB.utils.EPubTouchInfo;
import com.kujirahand.usagireader.R;

/**
 * Created by kujira on 2016/04/12.
 */

/*
TODO: 左上メニューをアイコンに変更する
TODO: ブックマーク
*/


public class EPubPanelHeader extends EPubPanel {

    private Paint back_p; //
    private Paint menu_text_p = new Paint();
    private Paint border_p = new Paint();
    private Paint btn_p = new Paint();
    final private String DEFAULT_TITLE = "(UsagiReader - no title)";
    private String bookTitle = DEFAULT_TITLE;
    private Rect fontR;
    private boolean selectMode = false;
    private float config_btn_w;
    private Bitmap book_bmp = null;
    private Bitmap back_bmp = null;
    private Bitmap toc_bmp = null;


    public EPubPanelHeader(EPubView view, SizeBox size, String panelId) {
        super(view, size, panelId);
        this.back_p = view.back_p;
    }

    @Override
    protected void createResource() {
        super.createResource();
        //
        fontR = new Rect();
        menu_text_p.setTextSize(size.getInnerHeight() * 0.4f);
        menu_text_p.setTypeface(view.normal_tf);
        menu_text_p.setColor(view.textColor);
        menu_text_p.getTextBounds("愛", 0, 1, fontR);
        menu_text_p.setAntiAlias(true);
        //
        border_p.setColor(view.borderColor);
        border_p.setAntiAlias(true);
        //
        btn_p.setAntiAlias(true);
        //
        config_btn_w = size.getInnerHeight();
    }

    public void setTitle(String title) {
        this.bookTitle = (title == null) ? DEFAULT_TITLE : title;
        drawPanel();
    }

    public void setSelectMode(boolean b) {
        this.selectMode = b;
        drawPanel();
    }

    @Override
    public void drawPanel() {
        log("drawPanel()");
        if (selectMode) {
            drawPanelSelectMode();
            return;
        }

        // background
        float x, y, x2;
        panel_cv.drawRect(panel_cv.getClipBounds(), back_p);
        linkList.clear();

        float btn_margin = view.dp2px(15);

        //----------------
        // Books Button
        //----------------
        menu_text_p.setColor(view.linkColor);
        menu_text_p.setAntiAlias(true);
        menu_text_p.setFilterBitmap(true);
        x = size.margin.left;
        y = size.margin.top + ((size.getInnerHeight() - fontR.height()) / 2) + fontR.height();

        if (book_bmp == null) {
            book_bmp = BitmapFactory.decodeResource(view.getResources(), R.mipmap.book);
        }
        float ih = size.getInnerHeight() * 0.8f;
        float ih2 = (size.getInnerHeight() - ih) / 2;
        float iy = size.margin.top;
        Rect src = new Rect(0,0,book_bmp.getWidth(), book_bmp.getHeight());
        Rect des = new Rect((int)x, (int)(iy + ih2), (int)(x + ih), (int)(iy + ih2 + ih));
        panel_cv.drawBitmap(book_bmp, src, des, menu_text_p);
        x += des.width();
        EPubLinkArea linkBooks = new EPubLinkArea();
        linkBooks.href = "@books";
        linkBooks.rect = des;
        linkBooks.rect.top = size.box.top; // リンクに反応しやすく大きめにする
        linkBooks.rect.bottom = size.box.bottom;
        linkBooks.rect.left = size.box.left;
        linkList.add(linkBooks);

        //----------------
        // TOC Button
        //----------------
        if (toc_bmp == null) {
            toc_bmp = BitmapFactory.decodeResource(view.getResources(), R.mipmap.toc_btn);
        }
        x += btn_margin;
        Rect src_t = new Rect(0,0,toc_bmp.getWidth(), toc_bmp.getHeight());
        Rect des_t = new Rect((int)x, (int)(iy + ih2), (int)(x + ih), (int)(iy + ih2 + ih));
        panel_cv.drawBitmap(toc_bmp, src_t, des_t, menu_text_p);
        x += des_t.width();
        EPubLinkArea linkTOC = new EPubLinkArea();
        linkTOC.href = "@TableOfContents";
        linkTOC.rect = des_t;
        linkTOC.rect.top = size.box.top; // リンクに反応しやすく大きめにする
        linkTOC.rect.bottom = size.box.bottom;
        linkList.add(linkTOC);

        //----------------
        // Back Button
        //----------------
        if (back_bmp == null) {
            back_bmp = BitmapFactory.decodeResource(view.getResources(), R.mipmap.back_btn);
        }
        x += btn_margin;
        Rect src_b = new Rect(0,0,back_bmp.getWidth(), back_bmp.getHeight());
        Rect des_b = new Rect((int)x, (int)(iy + ih2), (int)(x + ih), (int)(iy + ih2 + ih));
        EPubLinkArea linkBack = new EPubLinkArea();
        linkBack.href = "@back";
        linkBack.rect = des_b;
        linkBack.rect.top = size.box.top; // リンクに反応しやすく大きめにする
        linkBack.rect.bottom = size.box.bottom;
        linkList.add(linkBack);
        if (view.viewPanel.canHistoryBack()) {
            panel_cv.drawBitmap(back_bmp, src_b, des_b, menu_text_p);
        } else {
            //
        }
        x += des_b.width();

        // 画面右端を描画
        int rx = drawConfigButton();

        //----------------
        // Index
        float index_w = 0;

        float x3 = x + index_w + view.dp2px(10);
        float title_w = 0;
        // book title
        if (bookTitle != null) {
            String title = bookTitle;
            float remain_w = size.getInnerWidth() - x - rx;
            boolean over = false;
            while (title.length() > 2) {
                title_w = menu_text_p.measureText(title + "..");
                if (remain_w > title_w) break;
                over = true;
                title = title.substring(0, title.length() - 2);
            }
            if (over) title += "..";
            // try to center
            float x4 = (size.getOuterWidth() - menu_text_p.measureText(bookTitle, 0, title.length())) / 2;
            if (x4 < x3) {
                x4 = x3;
            }
            menu_text_p.setColor(view.textColor);
            panel_cv.drawText(title, x4, y, menu_text_p);

            EPubLinkArea title_a = new EPubLinkArea("@TableOfContents");
            linkList.add(title_a);
            title_a.rect.left = (int)x4;
            title_a.rect.right = (int)(x4 + title_w);
            title_a.rect.top = (int)size.box.top;
            title_a.rect.bottom = (int)(size.box.bottom);
            log("link x=" + x4 + "," + title_w);
        }


        // ヘッダ下の線
        drawBorder();
    }

    int drawConfigButton() {
        // config button
        RectF rr = new RectF();
        float hh = config_btn_w;
        rr.top = size.box.top;
        rr.bottom = size.box.bottom;
        rr.right = size.box.right;
        rr.left = rr.right - hh;
        // btn_p.setColor(Color.rgb(255,255,255));
        // panel_cv.drawRect(rr, btn_p);
        // link
        EPubLinkArea area = new EPubLinkArea();
        area.setRectFCopy(rr);
        area.href = "@MainMenu";
        linkList.add(area);
        // 3 circle
        btn_p.setColor(Color.rgb(100,100,100));
        float h3 = hh / 5;
        float x = hh / 2 + rr.left;
        float pt = view.dp2px(2);
        for (int i = 2; i <= 4; i++) {
            float y = h3 * i + rr.top;
            panel_cv.drawCircle(x, y, pt, btn_p);
        }


        float icon_margin = view.dp2pxF(10);
        //
        // [Aa] Button
        //
        RectF aaF = new RectF();
        aaF.top = size.box.top;
        aaF.bottom = size.box.bottom;
        aaF.right = rr.left - icon_margin;
        aaF.left = aaF.right - hh;
        // link
        EPubLinkArea aa_area = new EPubLinkArea();
        aa_area.setRectFCopy(aaF);
        aa_area.href = "@ThemeMenu";
        linkList.add(aa_area);
        // draw Char
        float charSize = hh * 0.6f;
        btn_p.setColor(view.iconColor);
        btn_p.setTextSize(charSize);
        int y = (int)Math.floor(aaF.top + charSize + view.dp2px(6));
        panel_cv.drawText("Aa", aaF.left, y, btn_p);

        return (int)(size.getOuterWidth() - aaF.left);
    }

    void drawBorder() {
        border_p.setStrokeWidth(1);
        panel_cv.drawLine(0, size.box.bottom-1, size.getOuterWidth(), size.box.bottom-1, border_p);
    }

    //
    void drawPanelSelectMode() {
        // clear
        panel_cv.drawRect(panel_cv.getClipBounds(), back_p);
        linkList.clear();
        //
        String msg = "SELECT MODE";
        menu_text_p.setColor(view.linkColor);
        menu_text_p.getTextBounds(msg, 0, msg.length(), fontR);
        float y = size.margin.top + ((size.getInnerHeight() - fontR.height()) / 2) + fontR.height();
        panel_cv.drawText(msg, size.margin.left, y, menu_text_p);
        //
        border_p.setStrokeWidth(1);
        panel_cv.drawLine(0, size.box.bottom-1, size.getOuterWidth(), size.box.bottom-1, border_p);
    }

    @Override
    public boolean onTouchUp(EPubTouchInfo e) {
        for (EPubLinkArea link : linkList) {
            if (link.isHit(e.getX(), e.getY())) {
                if (link.href.equals("@books")) {
                    log("@books");
                    view.finishReading();
                    return false;
                }
                if (link.href.equals("@back")) {
                    log("@back");
                    view.viewPanel.backHistory();
                    return false;
                }
                if (link.href.equals("@TableOfContents")) {
                    view.viewPanel.showLinkPage(new EPubLinkArea("@TableOfContents"), false);
                    return false;
                }
                if (link.href.equals("@MainMenu")) {
                    MainMenu.showMain();
                    return false;
                }
                if (link.href.equals("@ThemeMenu")) {
                    MainMenu.showThemeMenu();
                    return false;
                }
            } else {
                log("no link");
            }
        }
        return true;
    }

}
