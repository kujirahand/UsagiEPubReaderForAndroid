package com.kujirahand.KEPUB;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.kujirahand.KEPUB.utils.ProcTime;
import com.kujirahand.KPage.EPubMarkerList;
import com.kujirahand.KPage.KCharList;
import com.kujirahand.KPage.KCharMeasureIF;
import com.kujirahand.KPage.KPage;
import com.kujirahand.KPage.KPageParser;
import com.kujirahand.KPage.KTagLoader;
import com.kujirahand.KXml.KXmlNode;
import com.kujirahand.KXml.KXmlNodeList;
import com.kujirahand.KXml.KXmlParser;
import com.kujirahand.utils.EnumFiles;
import com.kujirahand.utils.IniFile;
import com.kujirahand.utils.KFile;
import com.kujirahand.utils.StringList;
import com.kujirahand.utils.ZipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO: mobi形式のファイルをサポートすること


/**
 * Created by kujira on 2016/04/07.
 */
public class EPubFile {
    private String cache_dir;
    private File cache_dir_f;
    private String epub_path;
    public StringList chapters;
    private String target_html_path;
    private String target_html_dir;
    private String cover_image_path;
    public String linkId = null;
    public String bookTitle = null;
    public int chapter = -1;
    public EPubMarkerList markerList = new EPubMarkerList();
    public IniFile ini = new IniFile();
    private String ini_path;

    private EPubFile() {
    }

    protected static StringBuilder logStr = new StringBuilder();

    public static String getLog() {
        String s = logStr.toString();
        logStr = new StringBuilder();
        return s;
    }

    public static String getAbsolutePath(String href) {
        if (null == instanceInternal) return href;
        return instanceInternal.parseRelativeURL(href);
    }

    public static void setAbsolutePath(String path) {
        if (null == instanceInternal) return; // error
        instanceInternal.target_html_path = path;
        File f = new File(path);
        instanceInternal.target_html_dir = f.getParent();
    }

    private static EPubFile instanceInternal = null;
    public static EPubFile openEPubFile(String epub_path) {
        // create EpubFile
        EPubFile epub = new EPubFile();
        instanceInternal = epub;
        // get Cache dir
        epub.cache_dir = PathConfig.getCacheDir(epub_path);
        epub.cache_dir_f = new File(epub.cache_dir);
        epub.cover_image_path = PathConfig.getCoverImage(epub_path);
        epub.ini_path = PathConfig.getIniFilePath(epub_path);
        log("epub_path=" + epub_path);

        boolean b = epub.open(epub_path);
        if (!b) return null;
        return epub;
    }

    public static void log(String msg) {
        logStr.append(msg+"\n");
    }

    private boolean open(String epub_path) {
        this.epub_path = epub_path;
        unzipEpub();
        if (!readIndex()) return false;
        return true;
    }

    public String getHtmlPath() { return target_html_path; }

    public String getEpubPath() { return epub_path; }

    public String parseRelativeURL(String href) {
        if (href == null) href = "";
        String full_path;
        String file_path = href;
        String link_id = "";
        int i = href.indexOf('#');
        if (i >= 0) {
            file_path = href.substring(0, i);
            link_id = href.substring(i+1);
        }
        if (file_path.equals("")) {
            full_path = target_html_path;
        }
        else {
            // ofile resource
            if (file_path.indexOf("file:/") == 0) {
                // キャッシュパス以下であればOK
                file_path = file_path.replace("file:/", "");
                if (file_path.indexOf(cache_dir) != 0) {
                    // NG
                    log("wrong file path = " + file_path);
                    return target_html_path; // NG
                }
            }

            // check already full path
            if (file_path.indexOf(cache_dir) == 0) {
                // nothing to do
                full_path = file_path;
            } else {
                // add dir
                if (file_path.charAt(0) == '/') { // full path
                    full_path = cache_dir + file_path;
                } else {
                    file_path = trimPrefixPath(file_path);
                    full_path = target_html_dir + "/" + file_path;
                }
            }
        }
        this.linkId = link_id;
        return full_path;
    }

    // ASync
    public void saveAsEPUBAsync() {
        // cache_dirを圧縮して、epub_pathに保存
        ArrayList<String> list = new ArrayList<String>();
        EnumFiles.enumAllFiles(this.cache_dir_f, list);
        ZipUtil.compress(list, this.cache_dir, this.epub_path);
    }


    public void savePage(KPage page) throws IOException {
        if (page == null) return;
        if (!page.isModified) return;
        page.isModified = false;
        String body = page.toStringForSave();
        String cache_file = page.path + EPubConfig.CACHE_FOOTER;
        KFile.save(cache_file, body);
        //TODO: EPUBに書き戻す
        log("savePage=" + cache_file);
    }


    public int getChapter() {
        return chapter;
    }


    public String getPathFromHTML(String path) {
        path = trimPrefixPath(path);
        return this.target_html_dir + "/"  + path;
    }

    public String getCacheDir() {
        return this.cache_dir;
    }


    private void unzipEpub() {
        // check Cache
        if (!cache_dir_f.exists()) {
            cache_dir_f.mkdirs();
        }
        List<String> files = ZipUtil.extract(epub_path, cache_dir);
        // enum files
        for (String fname : files) {
            log("- file=" + fname);
        }
    }

    public void saveIniFile() {
        try {
            ini.saveToFile(ini_path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean readIndex() {
        // check cache
        if (KFile.exists(ini_path)) {
            try {
                ini.loadFromFile(ini_path);
                String ini_epub_path = ini.get("epub_path", "");
                if (!ini_epub_path.equals(epub_path)) {
                    ini.clear();
                    ini.put("epub_path", epub_path);
                }
            } catch (IOException e) {
                log(e.getMessage());
                e.printStackTrace();
            }
        }
        String root_path = ini.get("root_path");
        if ( root_path == null || (!KFile.exists(root_path)) ) {
            // ROOT_FILE = META-INF/container.xml
            KXmlNode cont = KXmlParser.loadAndParse(cache_dir + "/META-INF/container.xml");
            if (cont == null) {
                log("Broken EPUB file, no container.xml");
                return false;
            }
            root_path = cont.getTagAttrValue("rootfile", "full-path");
            if (root_path == null) return false;
            root_path = trimPrefixPath(root_path);
            root_path = cache_dir + "/" + root_path;
            ini.put("root_path", root_path);
        }
        return readRootXMLFile(root_path);
    }

    private boolean readRootXMLFile(String root_path) {
        // content.opf
        this.bookTitle = ini.get("bookTitle", null);
        if (this.bookTitle == null || EPubConfig.useCache == false) {
            return readRootXMLFileFromXML(root_path);
        }
        // from Ini file
        // chapters
        this.chapters = new StringList();
        int chapter_size = Integer.valueOf(ini.get("chapter.size", "0"));
        for (int i = 1; i <= chapter_size; i++) {
            String f = ini.get("chapter" + i, null);
            if (f != null) chapters.add(f);
        }
        log("readRootXMLFile=fromIni");
        log(ini.toString());
        return true;
    }

    private boolean readRootXMLFileFromXML(String rootfile_path) {
        File base_f = new File(rootfile_path);
        String base_path = base_f.getParentFile().getAbsolutePath();
        KXmlNode root_x = KXmlParser.loadAndParse(rootfile_path);

        // | - title
        KXmlNode title = root_x.getByTag("dc:title");
        if (title != null) {
            this.bookTitle = title.getText();
        } else {
            this.bookTitle = "No title";
        }
        ini.put("bookTitle", bookTitle);

        // | - cover image
        if (!checkCoverImage(epub_path, base_path, root_x)) {
            return false;
        }

        // | - spine (目次)
        KXmlNode spine = root_x.getByTag("spine");
        KXmlNodeList nodelist = spine.findChildren("itemref");
        chapters = new StringList();
        for (KXmlNode n : nodelist) {
            String ref = n.getAttrValue("idref");
            String linear = n.getAttrValue("linear");
            if (linear != null) {
                if (linear.toLowerCase().equals("no")) continue;
            }
            KXmlNode book = root_x.getById(ref);
            if (book == null) {
                // view.log("broken link = " + ref);
                continue;
            }
            String book_href = book.getAttrValue("href");
            if (book_href == null) continue;
            book_href = trimPrefixPath(book_href);
            chapters.add(base_path + "/" + book_href);
            // Log.d("Usagi", "chapter=" + book_href);
        }
        if (chapters.size() == 0) return false;

        ini.put("chapter.size", ""+chapters.size());
        for (int i = 1; i <= chapters.size(); i++) {
            String f = chapters.get(i - 1);
            ini.put("chapter" + i, f);
        }
        saveIniFile();
        return true;
    }

    public String getMarkerListPath() {
        return cache_dir + "/_marker_list_usagireader.tsv";
    }

    private String trimPrefixPath(String path) {
        if (path.indexOf("/") == 0) path = path.substring(1);
        if (path.indexOf("./") == 0) path = path.substring(2);
        return path;
    }

    private String toc_path = null;
    public String getTOCPath() {
        return toc_path;
    }

    public void saveTableOfContents(String save_path) {
        log("saveTableOfContents=" + save_path);
        toc_path = save_path;
        File f = new File(save_path);
        String base_path= f.getParent();
        // chapters フルパスを相対パスに変更してリンクして保存
        String html = "<html><head><title>Table of contents</title></head><body><ul>";
        int index = 1;
        for (String href : chapters) {
            String title = KFile.getTitleTagFromFile(href);
            String apath = href.replace(base_path, "");
            if (title == null || title.equals("")) title = "Chapter" + index;
            html += "<li><a href='"+apath+"'>" + title + "</a></li>";
            index++;
        }
        html += "</ul></body></html>";
        try {
            KFile.save(save_path, html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resizeBitmap(Canvas canvas, String img_src, Rect size) throws IOException {
        try {
            Bitmap bmp;
            // Get only size
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(img_src, opt);
            int real_w = opt.outWidth;
            int real_h = opt.outHeight;
            // for Memory set scale
            float w, r, h;
            h = size.width();//px
            r = h / real_h;
            w = r * real_w;
            int scale = 1 + (int)Math.floor(real_h / h);
            opt.inJustDecodeBounds = false;
            opt.inSampleSize = scale;
            float x = size.left + (size.width() - w) / 2;
            bmp = BitmapFactory.decodeFile(img_src, opt);
            // copy to canvas
            Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
            Rect dst = new Rect((int)x, (int)size.top, (int) Math.ceil(x + w), (int) Math.ceil(size.top + h));
            //
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            canvas.drawBitmap(bmp, src, dst, paint);
            bmp.recycle();
        } catch (Exception e) {
            throw new IOException("Image load error");
        }
    }


    private boolean checkCoverImage(String epub_path, String cpath, KXmlNode root) {
        // simple
        KXmlNode meta = root.getByTagAndAttr("meta", "name", "cover");
        if (meta == null) return false;
        String image_content = meta.getAttrValue("content");
        if (image_content == null) return false;
        KXmlNode item = root.getByTagAndAttr("item", "id", image_content);
        if (item == null) return false;
        String path = item.getAttrValue("href");
        path = trimPrefixPath(path);
        String src = cpath + "/" + path;

        // resize
        int px = 128;
        Bitmap bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bmp);
        try {
            resizeBitmap(cv, src, new Rect(0, 0, px, px));
            String savePath = cover_image_path;
            FileOutputStream fos = null;
            fos = new FileOutputStream(new File(savePath));
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            log("Could make cover image.");
            return false;
        }
        return true;
    }


}
