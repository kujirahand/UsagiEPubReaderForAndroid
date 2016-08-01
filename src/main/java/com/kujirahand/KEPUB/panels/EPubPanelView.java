package com.kujirahand.KEPUB.panels;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.kujirahand.KEPUB.EPubConfig;
import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.async.EPubPageLoaderAsync;
import com.kujirahand.KEPUB.utils.SizeBox;
import com.kujirahand.KEPUB.utils.EPubLinkAreaList;
import com.kujirahand.KEPUB.EPubTextDrawer;
import com.kujirahand.KEPUB.utils.EPubLinkArea;
import com.kujirahand.KEPUB.utils.EPubTouchInfo;
import com.kujirahand.KEPUB.utils.ProcTime;
import com.kujirahand.KPage.EPubMarkerList;
import com.kujirahand.KPage.KCharMeasureIF;
import com.kujirahand.KPage.KLineInfo;
import com.kujirahand.KPage.KPage;
import com.kujirahand.KPage.KTagBegin;
import com.kujirahand.KPage.KTagEnd;
import com.kujirahand.KPage.KTagNo;
import com.kujirahand.usagireader.WebActivity;
import com.kujirahand.utils.DialogCallback;
import com.kujirahand.utils.DialogHelper;
import com.kujirahand.utils.StringMap;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by kujira on 2016/04/09.
 */
public class EPubPanelView extends EPubPanelViewBase implements KCharMeasureIF {

    private Paint text_p;
    private Paint back_p;
    private EPubLinkAreaList pageHistory = new EPubLinkAreaList();
    private EPubLinkArea curPage = null;
    private int lineCount;
    private float fontHeight;
    // for swipe anime
    private Bitmap curr_bmp = null;
    private Canvas curr_cv = null;
    private Bitmap next_bmp = null;
    private Canvas next_cv = null;
    private Bitmap prev_bmp = null;
    private Canvas prev_cv = null;

    private Bitmap left_bmp = null;
    private Canvas left_cv = null;
    private Bitmap right_bmp = null;
    private Canvas right_cv = null;

    private boolean dualPageMode = false;
    private long animeId4nextPage = -1;

    private SizeBox pageSize;
    private boolean selectMode = false;
    private RectF selectedRect = null;
    private EPubMarkerTool markerTool = null;

    protected EPubTextDrawer drawText;
    private String defaultMakerColor = "yellow";

    public EPubPanelView(EPubView view, SizeBox size, String panelId) {
        super(view, size, panelId);
    }

    public int getChapter() {
        return view.epubFile.getChapter();
    }

    public void addHistory(EPubLinkArea area) {
        if (pageHistory.size() > 0) {
            EPubLinkArea last = pageHistory.get(pageHistory.size() - 1);
            if (last.href.equals(area.href) && last.curPos == area.curPos) {
                return;
            }
        }
        pageHistory.add(area);
    }

    public EPubLinkArea popPageHistory() {
        if (pageHistory.size() == 0) {
            return null;
        }
        EPubLinkArea last = pageHistory.get(pageHistory.size() - 1);
        pageHistory.remove(pageHistory.size() - 1);
        return last;
    }

    public EPubLinkArea backHistory() {
        EPubLinkArea back = popPageHistory();
        if (back == curPage && back != null) {
            if (back.curPos == curPage.curPos) {
                back = popPageHistory();
            }
        }
        if (back != null) showLinkPage(back, false);
        return  back;
    }

    @Override
    public void setSize(SizeBox newbox) {
        super.setSize(newbox);
    }

    public void changeFontSize() {
        calcBox();
    }

    public void changeColorTheme() {
        String th = view.getPref().getString("colorTheme","white");
        if (th.equals("white")) {
            view.textColor = Color.BLACK;
            view.backColor = Color.WHITE;
            view.linkColor = Color.rgb(80, 80, 255);
            view.backRefColor = Color.rgb(240,240,240);
        }
        else if (th.equals("black")) {
            view.textColor = Color.WHITE;
            view.backColor = Color.BLACK;
            view.linkColor = Color.RED;
            view.backRefColor = Color.rgb(40,40,40);
        }
        else if (th.equals("silver")) {
            view.textColor = Color.rgb( 6, 21, 12);
            view.backColor = Color.rgb(224,213,213);
            view.linkColor = Color.rgb( 72,144,213);
            view.backRefColor = Color.rgb(214,203,203);
        }
        else if (th.equals("green")) {
            view.textColor = Color.rgb(255,248,194);
            view.backColor = Color.rgb( 69,151,112);
            view.linkColor = Color.rgb(235,235,235);
            view.backRefColor = Color.rgb(69,151,112);
        }
        else if (th.equals("brown")) {
            view.textColor = Color.rgb( 10, 20, 10);
            view.backColor = Color.rgb(192,149,103);
            view.linkColor = Color.rgb(31,67,110);
            view.backRefColor = Color.rgb(192,149,103);
        }
        back_p.setColor(view.backColor);
        text_p.setColor(view.textColor);
    }

    private void calcBox() {
        fontHeight = view.getFontHeight();
        lineCount = (int) Math.floor(size.getInnerHeight() / fontHeight);
        log("lineCount=" + lineCount);

        //
        // dual mode?
        if (size.box.width() > size.box.height()) {
            dualPageMode = true;
            int margin = view.dp2px(15);
            pageSize = new SizeBox();
            pageSize.box = new Rect(0,0,size.box.width() / 2, size.box.height());
            pageSize.margin = new Rect(
                    margin, size.margin.top,
                    (int)fontHeight, size.margin.bottom);
        } else {
            pageSize = size;
        }
        // for Draw
        drawText = new EPubTextDrawer(view, pageSize, lineCount);
        drawText.setFontSizePx(fontHeight, view.getFontSizePx());
        drawText.text_p = view.text_p;
        drawText.back_p = view.back_p;
    }

    @Override
    protected void createResource() {
        super.createResource();
        //
        text_p = view.text_p;
        back_p = view.back_p;
        changeColorTheme();
        //
        calcBox();
        //
        // create Bitmap and Canvas
        int w = size.getOuterWidth();
        int h = size.getOuterHeight();

        // create bitmap
        if (prev_bmp != null) prev_bmp.recycle();
        if (next_bmp != null) next_bmp.recycle();
        if (curr_bmp != null) curr_bmp.recycle();

        curr_bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        next_bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        prev_bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        curr_cv = new Canvas(curr_bmp);
        next_cv = new Canvas(next_bmp);
        prev_cv = new Canvas(prev_bmp);

        markerTool = new EPubMarkerTool(view);

        if (dualPageMode) {
            left_bmp = Bitmap.createBitmap(pageSize.getOuterWidth(), pageSize.getOuterHeight(), Bitmap.Config.ARGB_8888);
            right_bmp = Bitmap.createBitmap(pageSize.getOuterWidth(), pageSize.getOuterHeight(), Bitmap.Config.ARGB_8888);
            left_cv = new Canvas(left_bmp);
            right_cv = new Canvas(right_bmp);
        }
    }

    public void showChapter(final int chapter, final boolean showTop) {
        loadChapter(chapter, showTop);
    }

    protected void _showLinkPage(final EPubLinkArea area, boolean addHistory) {
        log("_showLinkPage.href=" + area.href);
        log("_showLinkPage.linkType=" + area.linkType);

        // check HTTP
        if (area.href.length() > 8) {
            if (area.href.substring(0, 7).equals("http://") ||
                    area.href.substring(0, 8).equals("https://")) {
                log("Show URL with browser=" + area.href);
                view.openWeb(area.href);
                return;
            }
        }

        // check Show WebActivity
        if (area.linkType.equals("@image")) {
            showWebActivity(area);
            return;
        }

        // user marker
        if (area.href.equals("@marker")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    editMarker(area);
                }
            }, 100);
            return;
        }

        // TOC
        if (area.href.equals("@TableOfContents")) {
            area.href = view.epubFile.getCacheDir() + "/__TableOfContents__usagireader.html";
            view.epubFile.saveTableOfContents(area.href);
        }

        // relative path to absolute path
        final String full_path = view.epubFile.parseRelativeURL(area.href);

        // normal link
        if (!area.isNoteref()) {
            if (addHistory) {
                // record Current Page
                EPubLinkArea cur = new EPubLinkArea(view.epubFile.getHtmlPath());
                cur.linkId = view.epubFile.linkId;
                cur.curPos = page.lineNo2index(scrollTop);
                addHistory(cur);
            }
            //
            EPubPageLoaderAsync loader = new EPubPageLoaderAsync(view, full_path, area.curPos, view.epubFile.linkId, this);
            loader.execute();
            return;
        }

        // show in refPanel
        Runnable callback_load = new Runnable() {
            @Override
            public void run() {
                // show refPanel
                EPubPageLoaderAsync loaderRef = new EPubPageLoaderAsync(view, full_path, 0, view.epubFile.linkId, view.refPanel);
                loaderRef.execute();
                return;
            }
        };
        if (!view.refPanel.isShowing()) {
            // パネルを開いたときにタップした位置を中心にしたい
            scrollTop += (int)(area.rect.top / fontHeight) - 3;
                    view.showRefPanel(true, callback_load );
        } else {
            new Handler().postDelayed(callback_load, 1);
        }
    }

    public void realodRefPage() {
        int top = view.refPanel.getScrollTop();
        KPage page = view.refPanel.getPage();
        if (page == null) return;
        String path = page.path;
        log("ref.top=" + top);
        log("ref.path=" + path);
        int index = page.lineNo2index(top);
        EPubPageLoaderAsync loaderRef = new EPubPageLoaderAsync(view, path, index, null, view.refPanel);
        loaderRef.execute();
    }

    private void editMarker(EPubLinkArea area) {
        panel_cv.drawBitmap(curr_bmp, 0, 0, null);
        //
        log("editMarker="+area.linkId);
        area = linkList.getByLinkId(area.linkId); // 先頭のareaを得る

        float x = area.rect.left - markerTool.width / 2;
        float sippo_x = x;
        if ((x + markerTool.width) > this.size.box.right) {
            x = this.size.box.right - markerTool.width;
        }
        float y = area.rect.top - markerTool.height;
        if (y < 0) y = 0;
        if (x < 0) x = 0;
        log("editMarker=" + x + "," + y);
        markerTool.show(panel_cv, x, y, sippo_x);
        markerTool.area = area;
    }

    private void editMarker2() {
        panel_cv.drawBitmap(curr_bmp, 0, 0, null);
        int si = markerTool.getSelectedIndex();
        switch (si) {
            case 0: // text
                editMarkerMemo(markerTool.area);
                break;
            case 1: // remove
                removeMarker(markerTool.area);
                break;
            case 2: // ul
            case 3: // red
            case 4: // yellow
            case 5: // blue
                editMarkerStyle(markerTool.area, si);
            default:
                break;
        }
    }

    private void showWebActivity(EPubLinkArea area) {
        log("showWebActivity");
        String path = view.epubFile.getPathFromHTML(area.href);
        try {
            Intent intent = new Intent(view.getContext(), WebActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(new File(path));
            intent.setDataAndType(uri, "text/html");
            view.getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Usagi", "path=" + path);
    }

    private Paint link_tap_p = new Paint();

    // Call when Tap link
    public void showLinkPage(final EPubLinkArea area, boolean addHistory) {
        //
        if (area == null) return;
        //
        if (area.href.charAt(0) != '@') {
            panel_cv.drawBitmap(curr_bmp, 0, 0, null);
            link_tap_p.setColor(Color.argb(80, 255, 150, 150));
            panel_cv.drawRect(area.rect, link_tap_p);
        }
        //
        log("showLinkPage=" + area.href);
        _showLinkPage(area, addHistory);
    }

    public void setScrollTopEx(int top, boolean forceRedraw) {
        if (curr_cv == null || curr_bmp == null) {
            log("@curr_bmp is null");
            return;
        }
        if (page == null) {
            log("@page is null");
            return;
        }
        if (!forceRedraw) {
            if (top == scrollTop) { // when new top is same position...
                view.invalidate();
                return;
            }
        }
        scrollTop = top;

        int perPage = lineCount;
        if (dualPageMode) perPage *= 2;

        // DRAW
        ProcTime.create("DrawPage(" + top + ")-" + page.title + "--");
        drawPage(curr_cv, top, true);
        panel_cv.drawBitmap(curr_bmp, 0, 0, null);
        drawDummyPage(next_cv);
        drawDummyPage(prev_cv);
        ProcTime.finish();

        int curPage = (int)(top / lineCount + 1);
        int maxPage = (int)(page.size() / lineCount);
        if (page.size() % lineCount > 0) maxPage++;

        // Other panel --- statusPanel
        view.statusPanel.setChapterNo(
                getChapter(), view.epubFile.chapters.size() - 1,
                curPage, maxPage,
                page.title);

        // headPanel
        view.headPanel.drawPanel();
        view.invalidate();

        // ---------------------------------
        view.setFullScreen(true);
        // drawPage4Anime(top, perPage);

        // save reading info
        int cur = page.lineNo2index(scrollTop);
        view.epubFile.ini.put("reading::html_path", page.path);
        view.epubFile.ini.putInt("reading::cur_index", cur);
        view.epubFile.saveIniFile();
    }

    @Override
    public void setScrollTop(int top) {
        setScrollTopEx(top, false);
    }

    private void drawPage4Anime(int top, int perPage) {
        // for ANIME
        final int nextTop = top + perPage;
        final int prevTop = top - perPage;
        final long animeId = System.nanoTime();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (animeId4nextPage == animeId) {
                    drawPage(next_cv, nextTop, false);
                    //log("drawed nextPage animeId =" + animeId);
                } else {
                    log("alreay changed nextPage animeId =" + animeId);
                }
            }
        }, 1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (animeId4nextPage == animeId) {
                    drawPage(prev_cv, prevTop, false);
                    //log("drawed prevPage animeId =" + animeId);
                } else {
                    log("alreay changed prevPage animeId =" + animeId);
                }
            }
        }, 2000);
        animeId4nextPage = animeId;
    }

    private void drawPage1p(Canvas canvas, int scroll_top, boolean makeLink, int shift_x) {
        drawText.shift_x = shift_x;
        drawText.drawPage(page, canvas, scroll_top, (makeLink ? linkList : null));
    }

    private void drawDummyPage(Canvas cv) {
        if (cv == null) return;
        cv.drawRect(cv.getClipBounds(), back_p);
    }

    private void drawPage(Canvas canvas, int scroll_top, boolean makeLinkMap) {
        ProcTime.create("drawPage(top:" + scroll_top + ")");
        if (dualPageMode) {
            drawPage1p(left_cv,  scroll_top, makeLinkMap, 0);
            drawPage1p(right_cv, scroll_top + lineCount, makeLinkMap, pageSize.getOuterWidth());
            canvas.drawBitmap(left_bmp, 0, 0, null);
            canvas.drawBitmap(right_bmp, pageSize.getOuterWidth(), 0, null);
        } else {
            drawPage1p(canvas, scroll_top, makeLinkMap, 0);
        }
        ProcTime.finish();
        // if (makeLinkMap) showClickArea(canvas);
    }

    public void showClickArea(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(Color.argb(50, 255, 0, 0));
        for (EPubLinkArea area : linkList) {
            canvas.drawRect(area.rect, p);
        }
    }


    private void loadChapter(int index, boolean showTop) {
        if (index < 0) index = 0;
        if (index >= view.epubFile.chapters.size()) index = view.epubFile.chapters.size() - 1;
        String full_path = view.epubFile.chapters.get(index);

        EPubPageLoaderAsync loader = new EPubPageLoaderAsync(view, full_path, (showTop ? 0 : -1), "", this);
        loader.execute();
    }

    private int down_scrollTop;

    @Override
    public boolean onTouchDown(EPubTouchInfo e) {
        down_scrollTop = scrollTop;
        return true;
    }

    private Paint touch_back_p = new Paint();
    private boolean touchMoveLockUpDown = false;

    @Override
    public boolean onTouchMove(EPubTouchInfo e) {
        if (e.move_pos < fontHeight) return true;
        if (selectMode) {
            return onTouchMoveSelect(e);
        }
        // log("onTouchMove=" + e.getX() + "," + e.getY());

        Paint p = touch_back_p;
        // 上下方向のスクロール
        if (Math.abs(e.move_pos_y) > Math.abs(e.move_pos_x) && e.move_pos > fontHeight) {
            float hhf = e.move_pos_y / fontHeight;
            int hh = (int)Math.round(hhf);
            int sTop = down_scrollTop + hh;
            setScrollTop(sTop);
            // 細かい差分だけスクロールする
            float hh_real = hh * fontHeight;
            float hh_diff = hh_real - e.move_pos_y;
            //
            panel_cv.drawBitmap(curr_bmp, 0, hh_diff, null);
            //
            // 上方向へのスクロールなら全画面を解除する
            // if (hh < 0) view.setFullScreen(false);
            view.invalidate();
            // 上下方向へのスクロールでロック
            touchMoveLockUpDown = true;
            return false;
        }
        if (touchMoveLockUpDown) return false;

        // Slide page (0:transparent ... 255: not transparent)
        float alpha = 255 * (Math.abs(e.move_pos_x) / size.getOuterWidth());
        alpha = Math.min(255, alpha);

        panel_cv.drawBitmap(curr_bmp, 0, 0, null);
        p.setColor(Color.argb((int)alpha, 150, 150, 150));
        panel_cv.drawRect(panel_cv.getClipBounds(), p);

        // next page
        if (e.move_pos_x > 0) {
            panel_cv.drawBitmap(next_bmp, size.getOuterWidth() - e.move_pos_x, 0, null);
        }
        // prev page
        else {
            panel_cv.drawBitmap(prev_bmp, (-1*size.getOuterWidth()) - e.move_pos_x, 0, null);
        }
        this.captureTouch = true;
        view.invalidate();
        return false;
    }


    public void showChapterNext(boolean useHistory) {
        if (useHistory) {
            addHistory(new EPubLinkArea(this.page.path));
        }
        showChapter(getChapter() + 1, true);
    }
    public void showChapterPrev(boolean useHistory) {
        if (useHistory) {
            addHistory(new EPubLinkArea(this.page.path));
        }
        int chap = getChapter() - 1;
        if (chap < 0) { // last page
            showChapter(0, true);
            return;
        }
        showChapter(chap, false);
    }

    public boolean onSwipe(EPubTouchInfo e) {
        view.log("onSwipe=" + e.move_pos_x);
        // move page
        int new_top = getScrollTop();
        int perPage = lineCount * (dualPageMode ? 2 : 1);
        if (e.move_pos_x > 0) { // next page
            new_top += perPage;
            if (new_top > page.size()) {
                showChapterNext(false);
                return false;
            }
        } else { // prev page
            new_top -= perPage;
            if (new_top < (-1 * perPage)) {
                showChapterPrev(false);
                return false;
            }
        }
        setScrollTop(new_top);
        view.onSaveInfo(null);
        return false;
    }

    public boolean onTouchUpSelect(EPubTouchInfo e) {
        log("onTouchUpSelect");
        this.setSelectMode(false);
        if (selectedRect != null) {
            setMarkerToPage();
        }
        else {
            drawPanel();
        }
        view.invalidate();
        return false;
    }

    private void setMarkerToPage() {
        log("@setMarkerToPage=" + selectedRect.toString());
        int row1 = (int)Math.floor((selectedRect.top - pageSize.margin.top) / fontHeight);
        int row2 = (int)Math.floor((selectedRect.bottom - pageSize.margin.top) / fontHeight);
        float x1 = selectedRect.left - pageSize.margin.left;
        float x2 = selectedRect.right - pageSize.margin.left;
        if (dualPageMode && x1 > pageSize.box.right) {
            x1 -= pageSize.getOuterWidth();
            x2 -= pageSize.getOuterWidth();
            row1 += lineCount;
            row2 += lineCount;
        }
        log("| - actual pos=" + x1 + "-" + x2);

        ProcTime.create("SetMakerToPage.insert");
        KLineInfo line1 = page.getLine(row1 + scrollTop);
        int idx1 = page.getIndexCharByPos(line1, x1, this);
        KLineInfo line2 = page.getLine(row2 + scrollTop);
        int idx2 = page.getIndexCharByPos(line2, x2, this) + 1;

        // idx1からidx2の間に別のマーカーがあるか？
        for (int i = idx1; i <= idx2; i++) {
            Object o = page.source.get(i);
            if (o instanceof KTagBegin) {
                KTagBegin tagBegin = (KTagBegin)o;
                if (tagBegin.tagNo == KTagNo.USER_MARKER) {
                    idx2 = i; break;
                }
            }
            if (o instanceof KTagEnd) {
                KTagEnd tagEnd = (KTagEnd)o;
                if (tagEnd.getTagNo() == KTagNo.USER_MARKER) {
                    idx2 = i; break;
                }
            }
        }
        // 補正
        // CJK統合漢字の全範囲（4E00～9FFF）
        // ひらがな(3040～309F)
        // todo: 何文字に句読点があれば
        log("marker=" + page.source.substring(idx1, idx2).getOnlyText());
        log("| - actual col=" + idx1 + "-" + idx2);
        log("| - actual row=" + row1 + "-" + row2);

        StringMap attr = new StringMap();
        attr.put("id", EPubMarkerList.genUId());
        attr.put("class", "user_marker");
        attr.put("color", defaultMakerColor);
        KTagBegin um_begin = new KTagBegin("user:marker", attr);
        KTagEnd um_end = new KTagEnd(um_begin);
        page.insertCharTo(idx1, um_begin);
        page.insertCharTo(idx2, um_end);
        ProcTime.finish();

        // redraw
        page.isModified = true;
        setScrollTopEx(scrollTop, true);
        try {
            page.clearIdMap();
            ProcTime.create("epubFile.savePage");
            view.epubFile.savePage(page);
            log(view.epubFile.getLog());
            ProcTime.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isAlphaNum(char c) {
        return ('a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || '0' <= c && c <= '9');
    }
    private static boolean isKatakana(char c) {
        // 30A0～30FF
        return (0x30A0 <= c && c <= 0x30FF);
    }

    private void removeMarker(EPubLinkArea cur) {
        ProcTime.create("removeMarker");
        String id = cur.linkId;
        page.clearIdMap();
        //
        // removeMarker
        int idx = page.source.findTagById(id, 0);
        if (idx < 0) {
            log("findTagById could not find id=" + id);
            return;
        }
        KTagBegin tagBegin = (KTagBegin)page.source.get(idx);
        int endIndex = page.source.findObject(tagBegin.tagEnd, idx, page.source.size());
        if (endIndex < 0) {
            log("findObject could not find endTag for id=" + id);
            return;
        }
        page.removeChar(endIndex);
        page.removeChar(idx);

        // save & redraw
        try {
            page.isModified = true;
            view.epubFile.savePage(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ProcTime.finish();
        //
        setScrollTopEx(scrollTop, true);
    }

    private void editMarkerMemo(EPubLinkArea cur) {
        String id = cur.linkId;
        // findId
        int idx = page.source.findTagById(id, 0, page.source.size());
        if (idx < 0) return;
        final KTagBegin tagBegin = (KTagBegin)page.source.get(idx);
        String memo = tagBegin.getAttrValue("memo");
        if (memo == null) memo = "";
        DialogHelper.memoDialog("Memo", memo, new DialogCallback(){
            @Override
            public void dialogResult(Object which) {
                Date d = new Date();
                long utime = d.getTime();
                String v = (String)which;
                tagBegin.setAttrValue("memo", v);
                tagBegin.setAttrValue("utime", ""+utime);
                // save & redraw
                try {
                    page.isModified = true;
                    view.epubFile.savePage(page);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setScrollTopEx(scrollTop, true);
            }
            @Override
            public void dialogCancel() {
            }
        });
    }

    private void editMarkerStyle(EPubLinkArea cur, int index) {
        String id = cur.linkId;
        // findId
        int idx = page.source.findTagById(id, 0, page.source.size());
        if (idx < 0) return;
        final KTagBegin tagBegin = (KTagBegin)page.source.get(idx);
        String[] color_styles = new String[] {
                "remove", "text",
                "ul", "red", "yellow", "blue"
        };
        defaultMakerColor = color_styles[index];
        tagBegin.setAttrValue("color", defaultMakerColor);
        // save & redraw
        try {
            page.isModified = true;
            view.epubFile.savePage(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setScrollTopEx(scrollTop, true);
    }

    @Override
    public void reloadPage() {
        String full_path = page.path;
        int cur_index = page.lineNo2index(scrollTop);
        log("cur_index=" + cur_index);
        //
        EPubPageLoaderAsync loader = new EPubPageLoaderAsync(view, full_path, cur_index, null, this);
        loader.useCache = false; // for refresh page
        loader.execute();
    }

    @Override
    public boolean onTouchUp(EPubTouchInfo e) {
        this.touchMoveLockUpDown = false;
        this.captureTouch = false;
        if (page == null) return true;
        if (selectMode) return onTouchUpSelect(e);

        // --- フリックかどうか判断する ---
        // (1) ページの1/2までスライドさせていたら、完全にフリックと見なす
        float flick_limit = size.getOuterWidth() / 2;
        if (dualPageMode) {
            flick_limit = pageSize.getOuterWidth() / 4;
        }
        if (Math.abs(e.move_pos_x) > flick_limit) {
            return onSwipe(e);
        }

        // --- 上下方向への移動 ---
        float updown_limit = fontHeight * 2;
        if (Math.abs(e.move_pos_y) > updown_limit) {
            int hh = (int)Math.round(e.move_pos_y / fontHeight);
            int sTop = down_scrollTop + hh;
            log("scroll=" + hh + "=" + sTop);
            setScrollTopEx(sTop, true); // force redraw ... スクロールの途中ということがあり得る
            if (sTop > page.size()) {
                showChapterNext(false);
                return false;
            }
            if (sTop < (-1 * lineCount)) {
                showChapterPrev(false);
                return false;
            }
            return false;
        }

        // --- TOOL CLICK ? ---
        if (markerTool.visible) {
            if (markerTool.isHit(e.getX(), e.getY())) {
                editMarker2();
                return false;
            }
        }

        // --- LINK CLICK? ---
        // TODO: タップ
        // tap LINK area
        EPubLinkArea area = linkList.getHitLink(e.getX(), e.getY());
        if (area != null) {
            showLinkPage(area, true);
            return false;
        } else {
            log("no link (checked:" + linkList.size() + ")");
        }

        // --- tap left bottom side
        /*
        int left_limit = (int)(size.getOuterWidth() / 6);
        int bottom_limit = (int)((size.getInnerHeight() / 3) * 2);
        int new_top;
        if (e.getX() < left_limit && e.getY() > bottom_limit) {
            new_top = scrollTop - lineCount * (dualPageMode ? 2 : 1);
            if (new_top < 0) {
                showChapterPrev(false);
            } else {
                setScrollTop(new_top);
            }
            return false;
        }
        // right bottom side
        int right_limit = (int)(size.box.right - left_limit);
        if (e.getX() > right_limit && e.getY() > bottom_limit) {
            new_top = scrollTop + lineCount * (dualPageMode ? 2 : 1);
            if (new_top > page.size()) {
                showChapterNext(false);
            } else {
                setScrollTop(new_top);
            }
            return false;
        }
        */

        // view.setFullScreen(!view.fullscreenMode);

        // move cancel
        panel_cv.drawRect(size.box, back_p);
        panel_cv.drawBitmap(curr_bmp, 0, 0, null);
        view.setFullScreen(true);
        view.invalidate();

        return false;
    }

    // long tap
    public void onLongTap(EPubTouchInfo e) {
        // タップした先にリンクがあるか？なければ、選択モード
        EPubLinkArea area = linkList.getHitLink(e.getX(), e.getY());
        if (area != null) {
            return;
        }
        // set selectmode
        setSelectMode(true);
        //
        // onTouchMoveSelect(e);
        // draw StartPoint
        marker_p.setAntiAlias(true);
        /*
        marker_p.setColor(Color.argb(100, 255, 0, 0));
        _drawMarkerBall(e.getX(), e.getY(), view.dp2px(30));
        */

        drawOperaGlass(e.getX(), e.getY() - fontHeight, e.getX(), e.getY() - fontHeight);
    }

    public boolean getSelectMode() {
        return this.selectMode;
    }

    private Paint marker_p = new Paint();
    private boolean onTouchMoveSelect(EPubTouchInfo e) {
        // draw background
        panel_cv.drawBitmap(curr_bmp, 0, 0, null);

        //
        float x = e.getX();
        float y = e.getY();
        float sx = e.getStartX();
        float sy = e.getStartY();
        float y_add = (fontHeight) / 2;
        float cdp = view.dp2pxF(5);

        float page_left = pageSize.margin.left;
        float page_right = pageSize.box.right - pageSize.margin.right;

        float right_x = size.box.right - size.margin.right;
        // check 右端
        if (dualPageMode) {
            float hw = size.getOuterWidth() / 2;
            if (sx < hw) { // left side
                float hw_right = pageSize.box.right - pageSize.margin.right;
                if (x > hw_right) x= hw_right;
            } else { // right side
                page_left  += hw;
                page_right += hw;
                if (x > right_x) x = right_x;
            }
        } else {
            if (x > right_x) x = right_x;
        }

        // y座標
        sy -= pageSize.margin.top;
        y -= pageSize.margin.top;
        int line1 = (int)Math.floor(sy / fontHeight);
        int line2 = (int)Math.floor(y / fontHeight);
        sy = line1 * fontHeight + pageSize.margin.top;
        y = line2 * fontHeight + pageSize.margin.top;

        // check cancel
        float w = x - sx;
        if (line1 == line2 && w < fontHeight * 0.8f) { // cancel
            selectedRect = null;
            view.invalidate();
            return true;
        }
        float h = y - sy;
        if (h < 0) {
            selectedRect = null;
            view.invalidate();
            return true;
        }

        // draw marker
        marker_p.setColor(Color.argb(50, 255, 255, 0));

        RectF markerR = new RectF();
        for (int i = line1; i <= line2; i++) {
            markerR.top = i * fontHeight + pageSize.margin.top;
            markerR.bottom = markerR.top + fontHeight;
            if (i == line1) {
                markerR.left = sx;
                markerR.right = (line1 == line2) ? x : page_right;
            }
            else if (i == line2) {
                markerR.left = page_left;
                markerR.right = x;
            }
            else {
                markerR.left = page_left;
                markerR.right = page_right;
            }
            panel_cv.drawRect(markerR, marker_p);
        }


        // マーカーの四隅に目印を描画
        marker_p.setColor(Color.RED);
        _drawMarkerBall(sx, sy, cdp);
        _drawMarkerBall(sx, sy + fontHeight, cdp);
        _drawMarkerBall(x, y, cdp);
        _drawMarkerBall(x, y + fontHeight, cdp);

        drawOperaGlass(x, y, sx, sy);
        view.invalidate();

        // memory selected area
        selectedRect = new RectF(sx, sy, x, y);
        return true;
    }

    private void drawOperaGlass(float x, float y, float sx, float sy) {
        // 虫眼鏡を描画
        //
        Rect src = new Rect();
        src.left  = (int)(x - fontHeight * 2);
        src.right = (int)(x + fontHeight * 2);
        src.top   = (int)(y);
        src.bottom= (int)(src.top + fontHeight);
        Rect dst = new Rect();
        dst.left = src.left;
        dst.right = src.right;
        dst.top = (int)(sy - fontHeight);
        dst.bottom = (int)(dst.top + fontHeight);
        panel_cv.drawBitmap(curr_bmp, src, dst, null);
        // rect
        Paint mp = new Paint();
        mp.setAntiAlias(true);
        mp.setColor(Color.GRAY);
        mp.setStyle(Paint.Style.STROKE);
        mp.setStrokeWidth(2);
        panel_cv.drawRect(dst, mp);
        // draw center
        mp.setColor(Color.argb(150, 255, 0, 0));
        panel_cv.drawLine(x, dst.top, x, dst.bottom, mp);
    }

    private void _drawMarkerBall(float x, float y, float r) {
        RectF b = new RectF(x-r, y-r, x+r, y+r);
        panel_cv.drawArc(b, 0, 360, false, marker_p);
    }


    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
        view.headPanel.setSelectMode(selectMode);
        view.invalidate();
    }

    @Override
    public void drawPanel() {
        panel_cv.drawBitmap(curr_bmp, 0, 0, null);
        // memo:
        // いろいろなメソッドが適宜 panel_cv に直接描画するので
        //  EPubPanelViewで、このメソッドはほぼ飾りである
    }


    @Override
    public float getStrWidth(String str) {
        float w = this.text_p.measureText(str);
        return w;
    }
    @Override
    public int getFrameWidth() {
        return pageSize.getInnerWidth();
    }

    @Override
    public int getLineCount() {
        return this.lineCount;
    }

    public boolean canHistoryBack() {
        return (pageHistory.size() > 0);
    }

    public void swapPage(KPage page, int scrollTop) {
        int cur_scrollTop = this.scrollTop;
        KPage cur_page = this.page;

        // ref to main
        EPubPageLoaderAsync loader1 = new EPubPageLoaderAsync(
                view, page.path, page.lineNo2index(scrollTop), null, this);
        loader1.useCache = EPubConfig.useCache;
        loader1.execute();

        // main to ref
        EPubPageLoaderAsync loader2 = new EPubPageLoaderAsync(
                view, cur_page.path, cur_page.lineNo2index(cur_scrollTop), null, view.refPanel);
        loader2.useCache = EPubConfig.useCache;
        loader2.execute();
    }
}
