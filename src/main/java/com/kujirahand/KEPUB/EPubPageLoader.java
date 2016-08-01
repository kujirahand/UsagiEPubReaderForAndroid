package com.kujirahand.KEPUB;

import com.kujirahand.KEPUB.utils.ProcTime;
import com.kujirahand.KPage.KCharList;
import com.kujirahand.KPage.KCharMeasureIF;
import com.kujirahand.KPage.KPage;
import com.kujirahand.KPage.KPageParser;
import com.kujirahand.KPage.KTagLoader;
import com.kujirahand.KXml.KXmlNode;

import java.io.File;
import java.io.IOException;

/**
 * Created by kujira on 2016/05/28.
 */
public class EPubPageLoader {

    //TODO: 遅いので高速化
    // EPubPageLoaderAsync(EPubFile.log)=HTML loadAndParse=3624ms
    // path=/storage/emulated/0/Android/data/com.kujirahand.usagireader/cache/_storage_emulated_0_Books_UsagiReader_w_J_201603.epub/OEBPS/2016202-extracted.xhtml
    //        title=抽出されたテキスト: 16/3 若い皆さん バプテスマに向けてどんなことができますか
    public static KPage load(EPubFile epubFile, String full_path, KPage page, KCharMeasureIF charMeasure, boolean useCache) throws IOException {
        String target_html_path;
        String target_html_cache_path;

        // file
        File f = new File(full_path);
        if (!f.exists()) {
            EPubFile.log("File not found=" + full_path);
            throw new IOException("File not found. path=" + full_path);
        }

        // load new page
        KCharList charList = null;

        if (useCache && page != null && page.path.equals(full_path)) {
            String title = page.title;
            if (page.frameWidth != charMeasure.getFrameWidth()) {
                try {
                    EPubFile.log("@RemoveMarker/Page Remake");
                    ProcTime.create("HTML Page Remake");
                    KCharList list = page.source;
                    KPageParser.charMeasure = charMeasure;
                    page = KPageParser.splitLine(list, charMeasure.getFrameWidth());
                    page.frameWidth = charMeasure.getFrameWidth();
                    page.title = title;
                    page.path = full_path;
                    EPubFile.log(ProcTime.finishStr());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IOException(e.getMessage());
                }
            } else {
                EPubFile.log("@isSameFile=true");
            }
        } else {
            EPubFile.setAbsolutePath(full_path);
            target_html_path = full_path;
            target_html_cache_path = target_html_path + EPubConfig.CACHE_FOOTER;
            File f2 = new File(target_html_cache_path);
            if (f2.exists()) full_path = target_html_cache_path;

            // load
            ProcTime tm = ProcTime.create("HTML loadAndParse");
            try {
                charList = KTagLoader.load(full_path);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException(e.getMessage());
            }
            EPubFile.log("XML-loaded:"+tm.endGetStr());
            // parse
            try {
                KPageParser.charMeasure = charMeasure;
                // page = KPageParser.parseFromXMl(body_xml);
                page = KPageParser.parse(charList);
                if (page == null) throw new IOException("Page Parse error. path=" + full_path);
                page.frameWidth = charMeasure.getFrameWidth();
                page.path = target_html_path;
                // find chapter
                epubFile.chapter = epubFile.chapters.indexOf(target_html_path);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException(e.getMessage());
            }
            EPubFile.log(ProcTime.finishStr());
        }
        if (page != null) {
            EPubFile.log("path="  + full_path);
            EPubFile.log("title=" + page.title);
        }
        return page;
    }
}
