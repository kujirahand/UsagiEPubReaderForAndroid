package com.kujirahand.KEPUB.panels;

import com.kujirahand.KEPUB.EPubFile;
import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.utils.SizeBox;
import com.kujirahand.KPage.KCharMeasureIF;
import com.kujirahand.KPage.KPage;

/**
 * Created by kujira on 2016/04/15.
 */
public class EPubPanelViewBase extends EPubPanel implements KCharMeasureIF {

    protected KPage page;
    protected int scrollTop = -1; // Default value must be -1 cause cache

    public EPubPanelViewBase(EPubView view, SizeBox size, String panelId) {
        super(view, size, panelId);
    }

    @Override
    public float getStrWidth(String str) {
        return 0;
    }

    @Override
    public int getFrameWidth() {
        return 0;
    }

    public void setScrollTop(int scrollTop) {
        this.scrollTop = scrollTop;
        view.invalidate();
    }
    public int getScrollTop() {
        return this.scrollTop;
    }
    public void resetScrollTop() {
        this.scrollTop = Integer.MIN_VALUE;
    }

    public void setPage(KPage page) {
        this.page = page;
    }
    public KPage getPage() {
        return this.page;
    }

    public int getLineCount() {
        return 1;
    }

    public void reloadPage() {
        log("TODO: implement reloadPage");
    }

    @Override
    public String getAbsolutePath(String href) {
        return EPubFile.getAbsolutePath(href);
    }

}
