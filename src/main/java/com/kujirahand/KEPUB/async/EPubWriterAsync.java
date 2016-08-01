package com.kujirahand.KEPUB.async;

import android.view.Menu;

import com.kujirahand.KEPUB.EPubFile;
import com.kujirahand.KEPUB.EPubPageLoader;
import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.menu.MainMenu;
import com.kujirahand.KEPUB.panels.EPubPanelViewBase;
import com.kujirahand.usagireader.R;

/**
 * Created by kujira on 2016/07/16.
 */
public class EPubWriterAsync extends AsyncTaskInt3 {

    EPubView view;
    EPubFile epubFile;

    public EPubWriterAsync(EPubView view, EPubFile epubFile) {
        super();
        this.view = view;
        this.epubFile = epubFile;
        view.log("@EPubWriterAsync.begin");
        view.setOverlayText(MainMenu.lang(R.string.SAVE_TO_EPUB));
    }

    // Completed Task
    @Override
    protected void onPostExecute(Integer result) {
        view.log("@EPubWriterAsync.onPostExecute=" + result);
        view.setOverlayText(null);
    }

    @Override
    protected Integer doInBackground(Integer... value) {
        try {
            this.epubFile.saveAsEPUBAsync();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
