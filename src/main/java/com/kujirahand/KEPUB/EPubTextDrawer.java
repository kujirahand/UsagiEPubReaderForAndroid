package com.kujirahand.KEPUB;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.kujirahand.KCSS.CSSItems;
import com.kujirahand.KEPUB.utils.EPubDrawPos;
import com.kujirahand.KEPUB.utils.EPubLinkArea;
import com.kujirahand.KEPUB.utils.EPubLinkAreaList;
import com.kujirahand.KEPUB.utils.ProcTime;
import com.kujirahand.KEPUB.utils.SizeBox;
import com.kujirahand.KPage.KCharList;
import com.kujirahand.KPage.KLineInfo;
import com.kujirahand.KPage.KPage;
import com.kujirahand.KPage.KTagBegin;
import com.kujirahand.KPage.KTagEnd;
import com.kujirahand.KPage.KTagNo;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by kujira on 2016/04/14.
 */

public class EPubTextDrawer {

    protected EPubView view;
    protected SizeBox pageSize;

    protected int lineCount;
    protected float fontSizePx;
    protected float fontHeight;
    protected float fontSize_sup;

    public Paint back_p = new Paint();
    public Paint text_p = new Paint();
    public Paint text_sup_p  = new Paint();
    public Paint text_link_p = new Paint();
    Paint memo_p = new Paint();

    public Paint marker_p = new Paint();

    public int shift_x = 0;

    private boolean is_sup = false;
    private Paint cur_style;
    private EPubLinkArea link = null;
    private CSSItems css;
    private int curLineNo = 0;
    private KCharList curLine;
    private KPage page;
    private int image_line_num = 0;
    private boolean is_link;
    private boolean isHeader = false;
    private boolean isStyle = false;
    private boolean is_bold = false;
    private boolean is_li = false;
    private boolean is_backgroundColor = false;

    private EPubLinkAreaList linkList;

    public EPubTextDrawer(EPubView view, SizeBox pageSize, int lineCount) {
        this.view = view;
        this.pageSize = pageSize;
        this.lineCount = lineCount;
    }

    public void setFontSizePx(float fontHeight, float fontSizePx) {
        this.fontHeight = fontHeight;
        this.fontSizePx = fontSizePx;
        //
        fontSize_sup = fontSizePx * 0.6f;
        // type face
        text_p.setTypeface(view.normal_tf);
        text_sup_p.setTypeface(view.normal_tf);
        text_link_p.setTypeface(view.normal_tf);
        // size
        text_p.setTextSize(fontSizePx);
        text_sup_p.setTextSize(fontSize_sup);
        text_link_p.setTextSize(fontSizePx);
        // color
        text_p.setColor(view.textColor);
        text_sup_p.setColor(view.textColor);
        text_link_p.setColor(view.linkColor);
        text_link_p.setUnderlineText(true);
        // anti alias
        text_p.setAntiAlias(true);
        text_sup_p.setAntiAlias(true);
        text_link_p.setAntiAlias(true);
        //
        marker_p.setAntiAlias(true);
        marker_p.setColor(Color.argb(100, 255, 255, 100));
    }

    public void setDefaultStyle() {
        cur_style.setColor(view.textColor);
        cur_style.setUnderlineText(false);
        is_bold = false;
        is_backgroundColor = false;
    }

    public void log(String msg) {
        Log.d("Usagi.EPubTextDrawer", msg);
    }

    private Stack<MarkerInfo> markerStack = new Stack<MarkerInfo>();

    public void drawPage(KPage page, Canvas canvas, int scroll_top, EPubLinkAreaList linkList) {
        this.page = page;
        this.linkList = linkList;
        this.link = null;
        // fill background
        canvas.drawRect(canvas.getClipBounds(), back_p);
        // log("drawPage(" + scroll_top + ")");

        KCharList source = page.source;
        cur_style = text_p;
        cur_style.setTextSize(fontSizePx);
        cur_style.setTypeface(view.normal_tf);
        setDefaultStyle();

        image_line_num = 0;
        this.css = page.css;

        memo_p.setAntiAlias(true);
        memo_p.setColor(view.linkColor);

        markerStack.clear();
        EPubDrawPos cur = new EPubDrawPos(pageSize.margin.left, pageSize.margin.top);

        // リンクリストはキャッシュの時は作らない。加えて、2画面表示で右側の時はクリアしない
        if (linkList != null) {
            if (shift_x == 0 ) linkList.clear();
        }

        int lineNo;
        for (int i = 0; i < lineCount; i++) {
            lineNo = scroll_top + i;
            if (lineNo < 0) continue;
            if (page.lines.size() <= lineNo) break;
            curLineNo = lineNo;
            // line start
            cur.x = pageSize.margin.left;
            cur.y = pageSize.margin.top + (i * fontHeight) + fontSizePx;

            KLineInfo lineInfo = page.lines.get(lineNo);
            // --- タグの継続を調べる
            // 最寄りのタグを得る
            if (i == 0) {
                // バグがある
                /*
                KCharList contTags = source.getContinueTag(lineInfo.start);
                log("contTags=" + contTags.size());
                for (Object oc : contTags) {
                    beginTag((KTagBegin)oc, canvas, cur, lineInfo);
                    // log("contTag="+((KTagBegin)oc).toString());
                }
                */
                Stack<KTagBegin> stack = new Stack<KTagBegin>();
                for (int k = 0; k <= lineInfo.start; k++) {
                    Object ook = source.get(k);
                    if (ook instanceof KTagBegin) {
                        stack.push((KTagBegin)ook);
                    }
                    if (ook instanceof KTagEnd) {
                        stack.pop();
                    }
                }
                if (stack.size() > 0) {
                    KTagBegin[] contTags = new KTagBegin[stack.size()];
                    stack.toArray(contTags);
                    for (int k = 0; k < stack.size(); k++) {
                        beginTag(contTags[k], canvas, cur, lineInfo);
                    }
                }
            }

            // link continue?
            if (link != null) {
                EPubLinkArea link2 = new EPubLinkArea(link);
                linkList.add(link2);
                link = link2;
                link.rect.left = (int)cur.x;
                link.rect.top = (int)(cur.y - fontHeight);
                link.rect.right = (int)(link.rect.left + fontHeight * 2);
                link.rect.bottom = (int)(link.rect.top + fontHeight);
                link.shiftAreaX(shift_x);
            }
            int ci = lineInfo.start;
            while (ci < lineInfo.end) {
                Object o = source.get(ci);
                // text
                if (o instanceof Character) {
                    if (isHeader || isStyle) {
                        ci++;
                        continue;
                    }
                    StringBuilder sb = new StringBuilder();
                    while (ci < lineInfo.end) {
                        Object o2 = page.source.get(ci);
                        if (!(o2 instanceof Character)) break;
                        sb.append((char)o2);
                        ci++;
                    }
                    drawChar(canvas, cur, sb);
                    continue;
                }
                // element
                else if (o instanceof KTagBegin) {
                    beginTag((KTagBegin)o, canvas, cur, lineInfo);
                }
                else if (o instanceof KTagEnd) {
                    endTag((KTagEnd)o, canvas, cur);
                }
                ci++;
            }
            // 行末の処理
            image_line_num--;
            if (link != null) {
                link.rect.right = pageSize.box.right - pageSize.margin.right + shift_x;
            }


            /*
            if (linkList != null) {
                log("line:" + (lineNo) + ":" + lineInfo.toString(page.source));
                log("    |" + (lineNo) + ":" + lineInfo.getOnlyText(page.source));
                log("    | " + lineInfo.start + "-" + lineInfo.end);
            }
            */
        }
    }

    private void beginTag(KTagBegin ktag, Canvas canvas, EPubDrawPos cur, KLineInfo lineInfo) {

        // check tag
        switch (ktag.tagNo) {
            case KTagNo.DIV:
            case KTagNo.P:
                // check class
                String klass = ktag.klass;
                if ("qu".equals(klass)) {
                    cur_style.setColor(Color.rgb(0, 100, 0));
                }
                break;
            case KTagNo.UL:
                break;
            case KTagNo.LI:
                // 左端にドットを描く
                float xli = pageSize.box.left + pageSize.margin.left - text_p.measureText("- ");
                canvas.drawText("-", xli, cur.y, text_p);
                is_li = true;
                break;
            case KTagNo.H1:
            case KTagNo.H2:
            case KTagNo.H3:
            case KTagNo.H4:
            case KTagNo.H5:
            case KTagNo.H6:
            case KTagNo.STRONG:
            case KTagNo.B:
                if (ktag.klass != null && ktag.klass.indexOf("shadedHeader") >= 0) {
                    is_backgroundColor = true;
                }
                is_bold = true;
                setStyleBold(true);
                break;
            case KTagNo.SUP:
                is_sup = true;
                cur_style = text_sup_p;
                break;
            case KTagNo.U:
                cur_style.setUnderlineText(true);
                break;
            case KTagNo.IMG:
                if (lineInfo.pocketNo == 0) {
                    // draw caption label
                    String alt = ktag.getAttrValue("alt");
                    if (alt != null) {
                        Paint p = new Paint();
                        p.setAntiAlias(true);
                        p.setTextSize(view.dp2px(10));
                        int ww = pageSize.getInnerWidth();
                        int x = pageSize.margin.left;
                        int y = (int)cur.y;
                        for (int i = 0; i < alt.length(); i++) {
                            String s = alt.substring(i, i + 1);
                            float ws = p.measureText(s, 0, 1);
                            if (x + ws > ww) {
                                x = pageSize.margin.left;
                                y += view.dp2px(10);
                            }
                            canvas.drawText(s, x, y, p);
                            x += ws;
                        }
                    }
                } else if (lineInfo.pocketNo >= 1) {
                    if (image_line_num > 0) return;
                    image_line_num = 6;
                    int pNo = lineInfo.pocketNo - 1;
                    String src = ktag.getAttrValue("src");
                    if (src == null) return;
                    image_line_num = 6 - pNo;
                    showImage(canvas, src, cur.x, cur.y - fontHeight * pNo);
                    if (lastShowImageRect != null) {
                        if (linkList != null) {
                            EPubLinkArea imglink = new EPubLinkArea();
                            imglink.href = src;
                            imglink.linkType = "@image";
                            imglink.setRectCopy(lastShowImageRect);
                            imglink.shiftAreaX(shift_x);
                            linkList.add(imglink);
                        }
                    }
                }
                break;
            case KTagNo.A:
                is_link = true;
                cur_style = text_link_p;
                if (linkList != null) {
                    link = new EPubLinkArea();
                    link.rect.left = (int) (cur.x);
                    link.rect.top = (int) (cur.y - fontHeight);
                    link.rect.bottom = (int) (link.rect.top + fontHeight * 1.3);
                    link.rect.right = (int) (cur.x + fontHeight);
                    link.shiftAreaX(shift_x);
                    link.href = ktag.getAttrValue("href");
                    link.linkType = ktag.getAttrValue("epub:type");
                    if (link.linkType == null) link.linkType = "";
                    linkList.add(link);
                }
                break;
            case KTagNo.USER_MARKER:
                // log("<user:marker>");
                // TODO: マーカーは交差しないようにする
                if (markerStack.size() > 0) markerStack.clear();


                MarkerInfo mi = new MarkerInfo();
                mi.ktag = ktag;
                mi.x = cur.x;
                mi.y = cur.y;
                mi.memo = ktag.getAttrValue("memo");
                mi.setColorStr(ktag.getAttrValue("color"));
                markerStack.add(mi);
                //
                // set link area
                if (linkList != null) {
                    link = new EPubLinkArea();
                    link.rect.left = (int) (cur.x);
                    link.rect.top = (int) (cur.y - fontHeight);
                    link.rect.bottom = (int) (link.rect.top + fontHeight * 1.3);
                    link.rect.right = (int) (cur.x + fontHeight);
                    link.curPos = page.lineNo2index(curLineNo);
                    if (curLine != null) { link.curPos += curLine.getPosition(); }
                    link.shiftAreaX(shift_x);
                    link.href = "@marker";
                    link.linkType = "marker";
                    link.linkId = ktag.id;
                    linkList.add(link);
                }
                break;
            case KTagNo.HEAD:
                isHeader = true;
                break;
            case KTagNo.STYLE:
                isStyle = true;
                break;
        }
    }

    private void setTextStyle(StyleItem si) {
        if (si == null) return;
        if (si.bold >= 0) setStyleBold((si.bold == 1) ? true : false);
        if (si.underline >= 0) cur_style.setUnderlineText((si.underline == 1) ? true : false);
        if (si.color >= 0 ) {
            cur_style.setColor(si.color);
        }
    }


    private void endTag(KTagEnd etag, Canvas canvas, EPubDrawPos cur) {
        //
        switch (etag.getTagNo()) {
            case KTagNo.P:
            case KTagNo.DIV:
                cur_style = text_p;
                setDefaultStyle();
                markerStack.clear();
                link = null; is_link = false;
                /*
                KTagBegin ktag = etag.tagBegin;
                if ("qu".equals(ktag.klass)) {
                    text_p.setColor(view.textColor);
                }
                */

                break;
            case KTagNo.UL:
                break;
            case KTagNo.LI:
                is_li = false;
                break;
            case KTagNo.STRONG:
            case KTagNo.B:
            case KTagNo.H1:
            case KTagNo.H2:
            case KTagNo.H3:
            case KTagNo.H4:
            case KTagNo.H5:
            case KTagNo.H6:
                is_bold = false;
                setStyleBold(false);
                is_backgroundColor = false;
                break;
            case KTagNo.SUP:
                is_sup = false;
                cur_style = text_p;
                break;
            case KTagNo.U:
                cur_style.setUnderlineText(false);
                break;
            case KTagNo.A:
                is_link = false;
                cur_style = text_p;
                if (link != null) {
                    link.rect.right = (int)(cur.x + shift_x);
                    // Fix size when too small
                    if (link.rect.width() < fontHeight) {
                        float w2 = (fontHeight - link.rect.width()) / 2;
                        link.rect.left -= w2;
                        link.rect.right += w2;
                    }
                    link = null;
                }
                break;
            case KTagNo.USER_MARKER:
                if (markerStack.size() == 0) return;
                markerStack.remove(markerStack.size() - 1);
                if (link != null) {
                    link.rect.right = (int)(cur.x + shift_x);
                    // Fix size when too small
                    if (link.rect.width() < fontHeight) {
                        float w2 = (fontHeight - link.rect.width()) / 2;
                        link.rect.left -= w2;
                        link.rect.right += w2;
                    }
                    link = null;
                }
                break;
            case KTagNo.HEAD:
                isHeader = false;
                break;
            case KTagNo.STYLE:
                isStyle = false;
                break;
        }
    }

    private void drawChar(Canvas canvas, EPubDrawPos cur, CharSequence ch) {
        float x = cur.x;
        float y = cur.y;
        //
        String cs = ""+ ch;
        float csw = text_p.measureText(cs);
        // check marker
        if (is_sup) {
            y -= fontHeight / 2;
        }
        else if (is_backgroundColor) {
            float yadd = fontHeight / 4;
            float yy1 = cur.y - fontHeight + yadd;
            float yy2 = yy1 + fontHeight;
            marker_p.setColor(Color.argb(100, 200,130,200));
            canvas.drawRect(pageSize.box.left, yy1, pageSize.box.right, yy2, marker_p);
            is_backgroundColor = false;
        }
        else if (markerStack.size() > 0) {
            MarkerInfo mi = markerStack.peek();
            drawCharMarker(canvas, cur, csw, mi);
        }
        // draw char
        canvas.drawText(cs, x, y, cur_style);
        cur.x += csw;
    }
    
    // draw marker
    private void drawCharMarker(Canvas canvas, EPubDrawPos cur, float csw, MarkerInfo mi) {
        float yadd = fontHeight / 4;
        float yy = cur.y - fontHeight + yadd;
        float yy2 = yy + fontHeight;
        // marker background color
        switch (mi.color) {
            case MarkerInfo.COLOR_BLUE:
                marker_p.setColor(Color.argb(100, 100,100,255));
                break;
            case MarkerInfo.COLOR_RED:
                marker_p.setColor(Color.argb(100, 255,100,100));
                break;
            case MarkerInfo.COLOR_YELLOW:
                marker_p.setColor(Color.argb(100, 255,255,100));
                break;
            case MarkerInfo.COLOR_UL:
                marker_p.setColor(Color.argb(100, 255,60,60));
                marker_p.setStrokeWidth(2);
                float yy3 = yy2 - fontHeight * 0.15f;
                canvas.drawLine(cur.x, yy3, cur.x + csw, yy3, marker_p);
                break;
        }
        // 一文字ずつ
        if (mi.color != MarkerInfo.COLOR_UL) {
            canvas.drawRect(cur.x, yy, cur.x + csw, yy2, marker_p);
        }
        // draw marker memo
        if (cur.x != mi.x || cur.y != mi.y) return;
        String memo = mi.memo;
        if (memo == null || memo.length() == 0) return;
        if (memo.length() > 20) {
            memo = memo.substring(0, 18) + "..";
        }
        float fh3 = fontHeight / 3;
        memo_p.setTextSize(fh3);
        float w = memo_p.measureText(memo);
        float x = cur.x;
        if ((x + w) > pageSize.box.right) {
            x = pageSize.box.right - w;
        }
        canvas.drawText(memo, x, yy + fh3 / 4, memo_p);
    }

    private Rect lastShowImageRect = null;

    private void showImage(Canvas canvas, String img_src, float x, float y) {
        if (img_src == null) return;
        String path = view.epubFile.getPathFromHTML(img_src);
        log("image draw = " + path);
        try {
            Bitmap bmp;
            // Get only size
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opt);
            int real_w = opt.outWidth;
            int real_h = opt.outHeight;
            // for Memory set scale
            float w, r, h;
            h = fontHeight * 5;
            r = h / real_h;
            w = r * real_w;
            int scale = 1 + (int)Math.floor(real_h / h);
            opt.inJustDecodeBounds = false;
            opt.inSampleSize = scale;
            x = pageSize.margin.left + (pageSize.getInnerWidth() - w) / 2;
            bmp = BitmapFactory.decodeFile(path, opt);
            // copy to canvas
            Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
            Rect dst = new Rect((int)x, (int)y, (int) Math.ceil(x + w), (int) Math.ceil(y + h));
            log("image.pos=(" + x + "," + y + ") size=" + w + "," + h);
            //
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            canvas.drawBitmap(bmp, src, dst, paint);
            lastShowImageRect = dst;
            bmp.recycle();
        } catch (Exception e) {
            log("image could not load = " + path);
        }
    }

    private void setStyleBold(boolean b) {
        cur_style.setTypeface(b ? view.bold_tf : view.normal_tf);
    }

    protected class MarkerInfo {
        public float x;
        public float y;
        public KTagBegin ktag;
        public String memo = null;
        public int color = 0;
        final public static int COLOR_UL = 0;
        final public static int COLOR_RED = 1;
        final public static int COLOR_YELLOW = 2;
        final public static int COLOR_BLUE = 3;
        final public static int COLOR_TITLE_BACK = 4;
        public void setColorStr(String c) {
            if (c == null) c = "yellow";
            if (c.equals("yellow")) this.color = COLOR_YELLOW;
            else if (c.equals("blue")) this.color = COLOR_BLUE;
            else if (c.equals("red")) this.color = COLOR_RED;
            else if (c.equals("ul")) this.color = COLOR_UL;
        }
    }

    protected class StyleItem {
        public int color = -1;
        public int bgcolor = -1;
        public int underline = -1;
        public int sup = -1;
        public int bold = -1;
    }
}
