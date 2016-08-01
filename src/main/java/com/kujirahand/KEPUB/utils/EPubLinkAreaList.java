package com.kujirahand.KEPUB.utils;

import java.util.ArrayList;

/**
 * Created by kujira on 2016/04/17.
 */
public class EPubLinkAreaList extends ArrayList<EPubLinkArea> {

    public EPubLinkArea getHitLink(float x, float y) {
        for (EPubLinkArea area : this) {
            if (area.isHit(x, y)) {
                return area;
            }
        }
        return null;
    }

    public EPubLinkArea getByLinkId(String id) {
        if (id == null) return null;
        for (EPubLinkArea area : this) {
            if (id.equals(area.linkId)) return area;
        }
        return null;
    }

}
