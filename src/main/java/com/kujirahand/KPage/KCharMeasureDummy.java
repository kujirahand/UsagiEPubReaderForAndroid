package com.kujirahand.KPage;

/**
 * Created by kujira on 2016/04/13.
 */
public class KCharMeasureDummy implements KCharMeasureIF {
    public int width = 10;
    @Override
    public float getStrWidth(String str) {
        return str.length();
    }
    @Override
    public int getFrameWidth() {
        return width;
    }
    @Override
    public String getAbsolutePath(String href) {
        return href;
    }
}
