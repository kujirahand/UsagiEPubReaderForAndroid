package com.kujirahand.usagireader.Sehlf;

import com.kujirahand.KEPUB.PathConfig;
import com.kujirahand.utils.IniFile;
import com.kujirahand.utils.KFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by kujira on 2016/04/22.
 */
public class BookItem {
    private String path;
    private String icon;
    private String booktitle;
    private String dir = null;
    private File file;
    private IniFile ini = null;

    public BookItem(String path) {
        this.path = path;
        this.file = new File(path);
    }
    public BookItem(File file) {
        this.file = file;
        this.path = file.getAbsolutePath();
    }

    public String getDir() {
        if (dir == null) {
            String sdir = PathConfig.getStorageDir();
            dir = file.getParentFile().getAbsolutePath();
            dir = dir.replace(sdir, "");
        }
        return dir;
    }
    public String getPath() { return path; }
    public String getIcon() { return icon; }
    public String getFileName() {
        String s = file.getName();
        s = s.replaceAll("\\.epub$", "");
        return s;
    }

    public String getBookTitle() {
        // 書籍名を読み込む
        if (ini == null) {
            ini = new IniFile();
            String iniFile = PathConfig.getIniFilePath(path);
            if (KFile.exists(iniFile)) {
                try {
                    ini.loadFromFile(iniFile);
                } catch (IOException e) {
                }
            }
            booktitle = ini.get("bookTitle", "---");
        }
        return booktitle;
    }

    public File getFile() {
        return file;
    }
}
