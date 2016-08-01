package com.kujirahand.KEPUB.async;

import android.os.Handler;

import com.kujirahand.KEPUB.EPubConfig;
import com.kujirahand.KEPUB.EPubFile;
import com.kujirahand.KEPUB.EPubPageLoader;
import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.menu.MainMenu;
import com.kujirahand.KEPUB.utils.ProcTime;
import com.kujirahand.KPage.KPage;
import com.kujirahand.usagireader.R;
import com.kujirahand.utils.DialogCallback;
import com.kujirahand.utils.DialogHelper;

/**
 * Created by kujira on 2016/04/15.
 */
public class EPubFileLoaderAsync extends AsyncTaskInt3 {

    private String epub_path;
    private String html_path;
    private int cur_index;
    private EPubView view;
    private KPage page = null;
    private String errMsg = "";
    private EPubFile epubFile = null;
    private String link_id;
    private ProcTime time;

    public EPubFileLoaderAsync(EPubView view, String epub_path, String html_path, int cur_index, String link_id) {
        super();
        this.view = view;
        this.epub_path = epub_path;
        this.html_path = html_path;
        this.cur_index = cur_index;
        this.link_id = link_id;
        view.log("@epubLoader.create=" + epub_path);
        this.time = ProcTime.create("EPubFileLoaderAsync.onPostExecute");
    }

    // Completed Task
    @Override
    protected void onPostExecute(Integer result) {
        view.log("@epubLoader.onPostExecute=" + result);
        view.log("EPubFileLoaderAsync(epubFile.log)=" + EPubFile.getLog());
        view.epubLoader = null;
        view.epubFile = epubFile;
        view.setOverlayText(null);
        this.time.end();

        if (result < 0 || page == null) {
            view.log("@ERROR.reason=" + errMsg);
            showError();
            return;
        }

        view.headPanel.setTitle(view.epubFile.bookTitle);
        view.viewPanel.setPage(this.page);
        int scrollTop = page.index2lineNo(cur_index);
        // check link_id
        if (link_id != null && (!link_id.equals(""))) {
            int top = page.findId(link_id);
            if (top >= 0) {
                scrollTop = top;
            }
        }
        view.log("EPubFileLoaderAsync.scrollTop=" + scrollTop);
        view.viewPanel.setScrollTop(scrollTop);
    }

    private void errorFinish() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.finishReading();
            }
        }, 100);
    }

    private void showError() {
        DialogHelper.showError(
                MainMenu.lang(R.string.ERROR),
                MainMenu.lang(R.string.EPUB_OPEN_ERROR),
                new DialogCallback() {
                    @Override
                    public void dialogResult(Object which) {
                        errorFinish();
                    }
                    @Override
                    public void dialogCancel() {
                        errorFinish();
                    }
                });
        return;
    }

    @Override
    protected Integer doInBackground(Integer... value) {
        try {
            // open EPUB
            epubFile = EPubFile.openEPubFile(epub_path);
            if (epubFile == null) {
                errMsg = "Failed to open epub file.";
                return -1;
            }
            // check chapter
            String fullpath = html_path;
            if (fullpath == null) {
                // check reading info
                fullpath = epubFile.ini.get("reading::html_path", null);
                if (fullpath != null) {
                    cur_index = epubFile.ini.getInt("reading::cur_index", 0);
                }
                else {
                    if (epubFile.chapters.size() > 0) {
                        fullpath = epubFile.chapters.get(0);
                    } else {
                        errMsg = "no chapter";
                        return -1;
                    }
                }
            }
            // load html
            this.page = EPubPageLoader.load(epubFile, fullpath, null, view.viewPanel, EPubConfig.useCache);
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
