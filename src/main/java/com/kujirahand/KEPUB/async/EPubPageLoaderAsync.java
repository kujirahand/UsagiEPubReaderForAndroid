package com.kujirahand.KEPUB.async;

import com.kujirahand.KEPUB.EPubConfig;
import com.kujirahand.KEPUB.EPubFile;
import com.kujirahand.KEPUB.EPubPageLoader;
import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.panels.EPubPanelViewBase;
import com.kujirahand.KPage.KPage;
import com.kujirahand.usagireader.R;

/**
 * Created by kujira on 2016/04/15.
 */
public class EPubPageLoaderAsync extends AsyncTaskInt3 {

    private String html_path;
    private int cur_inex;
    private EPubView view;
    private EPubPanelViewBase panel;
    private KPage page = null;
    private String errMsg = "";
    private String linkId = "";
    public boolean useCache = EPubConfig.useCache;

    /**
     *
     * @param view
     * @param html_path
     * @param cur_inex 何文字目を表示するか(-1を指定すると最終ページを表示)
     * @param linkId
     * @param panel
     */
    public EPubPageLoaderAsync(EPubView view, String html_path, int cur_inex, String linkId, EPubPanelViewBase panel) {
        super();
        this.view = view;
        this.html_path = html_path;
        this.linkId = linkId;
        this.panel = panel;
        this.cur_inex = cur_inex;
        //
        panel.resetScrollTop();
        view.log("@EPubPageLoaderAsync.create=" + html_path);
        //
        view.setOverlayText(panel.lang(R.string.NOW_LOADING));
    }

    // Completed Task
    @Override
    protected void onPostExecute(Integer result) {
        view.log("@EPubPageLoaderAsync.onPostExecute=" + result);

        if (result < 0 || page == null) {
            view.log("@ERROR.reason="+errMsg);
            view.showError("Sorry, could not open contents file.");
            view.setOverlayText(null);
            return;
        }
        view.log("EPubPageLoaderAsync(EPubFile.log)="+EPubFile.getLog());

        int lineNo;
        int lineCount = panel.getLineCount();
        if (cur_inex == 0) {
            lineNo = 0;
        } else if (cur_inex < 0){
            lineNo = (int)Math.floor(page.size() / lineCount) * lineCount;
        } else {
            lineNo = page.index2lineNo(cur_inex);
        }
        // search linkId
        if (linkId != null && !linkId.equals("")) {
            lineNo = page.findId(linkId);
            if (lineNo < 0) {
                view.log("broken link id = " + linkId);
                lineNo = 0;
            } else {
                view.log("linkId["+linkId+"] found top=" + lineNo);
            }
        }
        panel.setPage(this.page);
        panel.setScrollTop(lineNo);
        view.setOverlayText(null);
    }

    @Override
    protected Integer doInBackground(Integer... value) {
        try {
            // check chapter
            String fullpath = html_path;
            if (fullpath == null) {
                if (view.epubFile.chapters.size() > 0) {
                    fullpath = view.epubFile.chapters.get(0);
                } else {
                    errMsg = "no chapter";
                    return -1;
                }
            }
            // load html
            this.page = EPubPageLoader.load(view.epubFile, fullpath, panel.getPage(), panel, useCache);
            if (this.page == null) {
                errMsg = "epubFile.load failed.";
                return -1;
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
