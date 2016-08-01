package com.kujirahand.KPage;

/**
 * Created by kujira on 2016/04/20.
 */
public class EPubMarker {
    public String id;
    public String path;
    public String text;

    public String toTSV() {
        return id + "\t" + path + "\t" + text;
    }
    public String toString() {
        return toTSV();
    }
}
