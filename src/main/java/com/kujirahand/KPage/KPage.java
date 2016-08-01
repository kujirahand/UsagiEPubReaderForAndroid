package com.kujirahand.KPage;

import com.kujirahand.KCSS.CSSItems;
import com.kujirahand.KEPUB.panels.EPubPanelViewBase;
import com.kujirahand.KXml.KXmlNode;
import com.kujirahand.KXml.KXmlParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Stack;

/**
 * Created by kujira on 2016/04/05.
 */
public class KPage {
    private KXmlNode header = null;
    public String title = "No title";
    public String path = "";
    private HashMap<String, Integer> idMap = null;
    public boolean isModified = false;
    public int frameWidth = 0;
    public CSSItems css = new CSSItems();

    // new source
    public KCharList source;
    public ArrayList<KLineInfo> lines = new ArrayList<KLineInfo>();

    //
    public KPage() {
    }

    public String sourceSplitByLines() {
        StringBuilder sb = new StringBuilder();
        for (KLineInfo i : lines) {
            KCharList line = source.substring(i.start, i.end);
            sb.append(line.toString() + "\n");
        }
        return sb.toString();
    }
    public String sourceSplitByLinesTextOnly() {
        StringBuilder sb = new StringBuilder();
        for (KLineInfo i : lines) {
            KCharList line = source.substring(i.start, i.end);
            sb.append(line.getOnlyText() + "\n");
        }
        return sb.toString();
    }

    public void createIdMap() {
        idMap = new HashMap<String, Integer>();
        for (int i = 0; i < lines.size(); i++) {
            KLineInfo info = lines.get(i);
            for (int j = info.start; j < info.end; j++) {
                Object o = source.get(j);
                if (o instanceof KTagBegin) {
                    KTagBegin tb = (KTagBegin)o;
                    String id = tb.id;
                    if (id == null) continue;
                    // check double
                    Integer tmp = idMap.get(id);
                    if (tmp != null) continue;
                    // put
                    idMap.put(id, i);
                }
            }
        }
    }

    public int findId(String id) {
        if (idMap == null) createIdMap();
        Integer r = idMap.get(id);
        if (r == null) return -1;
        return r;
    }
    public void clearIdMap() {
        if (idMap != null) {
            idMap.clear();
            idMap = null;
        }
    }
    

    public int size() {
        return lines.size();
    }

    public int lineNo2index(int lineNo) {
        if (lineNo < 0) return 0;
        if (lines.size() <= lineNo) lineNo = lines.size() - 1;
        int index = lines.get(lineNo).start;
        return index;
    }

    public int index2lineNo(int index) {
        if (index <= 0) return 0;
        int sz = lines.size();
        for (int i = 0; i < sz; i++) {
            KLineInfo li = lines.get(i);
            if (li.start <= index && index < li.end) return i;
        }
        return sz - 1;
    }

    public void insertCharTo(int index, Object o) {
        int lineNo = index2lineNo(index);
        KLineInfo kli = this.getLine(lineNo);
        kli.end++;
        source.add(index, o);
        int sz = lines.size();
        for (int i = lineNo + 1; i < sz; i++) {
            kli = this.lines.get(i);
            kli.start++;
            kli.end++;
        }
    }

    public void removeChar(int index) {
        int lineNo = index2lineNo(index);
        KLineInfo kli = this.getLine(lineNo);
        kli.end--;
        source.remove(index);
        int sz = lines.size();
        for (int i = lineNo + 1; i < sz; i++) {
            kli = this.lines.get(i);
            kli.start--;
            kli.end--;
        }
    }

    public String toString() {
    	return source.toString();
    }

    public String toStringForSave() {
        return source.toString();
        /*
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>" + title + "</title>");
        if (css.items.size() > 0) {
            sb.append("<style>" + css.toString() + "</style>");
        }
        sb.append("</head>");
        sb.append("<body>");
        sb.append(source.toString());
        sb.append("</body></html>");
        return sb.toString();
        */
    }

    public KLineInfo getLine(int i) {
        if (i < 0 || i >= this.size()) return null;
        return lines.get(i);
    }

    public String getLineTextOnly(int i) {
        KLineInfo info = lines.get(i);
        return info.getOnlyText(source);
    }
    public String getLineStr(int i) {
        KLineInfo info = lines.get(i);
        return info.toString(source);
    }

    public int getIndexCharByPos(KLineInfo info, float x, EPubPanelViewBase panel) {
        float len = 0;
        for (int i = info.start; i < info.end; i++) {
            Object o = source.get(i);
            if (!(o instanceof Character)) continue;
            char c = (char)o;
            float w = panel.getStrWidth(""+c);
            if (x < len + w / 2) return i;
            len += w;
        }
        return info.end - 1;
    }
}
