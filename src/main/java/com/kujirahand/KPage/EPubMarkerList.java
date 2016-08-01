package com.kujirahand.KPage;

import com.kujirahand.KXml.KXmlParser;
import com.kujirahand.utils.KFile;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kujira on 2016/04/20.
 */
public class EPubMarkerList extends ArrayList<EPubMarker> {

    final protected static String UID_HEADER = "umarker";
    public static String genUId() {
        Date date = new Date();
        long timeId = date.getTime();
        int randId = (int)Math.floor(Math.random() * 10000);
        return UID_HEADER + "_" + timeId + "_" + randId;
    }

    public String toTSV() {
        StringBuilder sb = new StringBuilder();
        for (EPubMarker marker : this) {
            String line = marker.toTSV() + "\n";
            sb.append(line);
        }
        return sb.toString();
    }

    public void parseTSV(String tsv) {
        String[] lines = tsv.split("\n");
        for (String line : lines) {
            String[] cells = line.split("\t");
            if (cells.length < 3) continue; // broken line
            EPubMarker m = new EPubMarker();
            m.id = cells[0];
            m.path = cells[1];
            m.text = cells[2];
            this.add(m);
        }
    }

    public String toHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Marker List</title></head><body>");
        sb.append("<h1>Marker List</h1><ul>");
        for (EPubMarker m : this) {
            sb.append("<li><a href='" + m.path + "#" + m.id + "'>");
            sb.append(KXmlParser.encodeSpecialChars(m.text));
            sb.append("</a></li>\n");
        }
        sb.append("</ul>\n");
        sb.append("</body></html>\n");
        return sb.toString();
    }
}




