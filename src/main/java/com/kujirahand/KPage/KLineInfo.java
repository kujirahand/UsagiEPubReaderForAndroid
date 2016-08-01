package com.kujirahand.KPage;

import com.kujirahand.KEPUB.panels.EPubPanelView;
import com.kujirahand.KEPUB.panels.EPubPanelViewBase;

/**
 * Created by kujira on 2016/05/01.
 */
public class KLineInfo {
    public int start;
    public int end; // 実際は index-1 (substringと同じ)
    public int pocketNo = 0;
    public KLineInfo(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int width() {
        int i = this.end - this.start - 1;
        if (i < 0) i = 0;
        return i;
    }
    public String toString(KCharList src) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            Object o = src.get(i);
            if (o instanceof Character) {
                sb.append((char)o);
                continue;
            }
            if (o instanceof KTagBegin) {
                sb.append(((KTagBegin)o).toString());
                continue;
            }
            if (o instanceof KTagEnd) {
                sb.append(((KTagEnd)o).toString());
                continue;
            }
        }
        return sb.toString();
    }

    public String getOnlyText(KCharList src) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            Object o = src.get(i);
            if (o instanceof Character) {
                sb.append((char)o);
                continue;
            }
        }
        return sb.toString();
    }

}
