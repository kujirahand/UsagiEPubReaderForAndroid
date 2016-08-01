package com.kujirahand.utils;

import android.util.Log;

import com.kujirahand.usagireader.Sehlf.BookItem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by kujira on 2016/04/03.
 */
public class EnumFiles {
    final public static int MAX_FILE = 150;

    public static void enumAllFiles(File path, ArrayList<String>items)  {
        if (items.size() > MAX_FILE) return;

        File f[] = path.listFiles();
        if (f == null) return;
        for (int i = 0; i < f.length; i++) {
            if (f[i].isDirectory()) {
                enumAllFiles(f[i], items);
                continue;
            }
            String ps = f[i].getAbsolutePath();
            items.add(ps);
            // Log.d("UsagiReader", "enum=" + ps);
        }
        return;
    }

    public static ArrayList<BookItem> enumEpub(File path)  {
        // enum epub
        FilenameFilter ff = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.lastIndexOf(".epub") > 0) return true;
                return false;
            }
        };
        ArrayList<BookItem> result = new ArrayList<BookItem>();

        File f[] = path.listFiles(ff);
        if (f == null) return result;
        for (int i = 0; i < f.length; i++) {
            result.add(new BookItem(f[i]));
        }
        return result;
    }
    public static void enumEpubAll(File path, ArrayList<BookItem>items)  {
        Log.d("dir", "enumEpubAll=" + path.getAbsolutePath());
        if (items.size() > MAX_FILE) return;

        File f[] = path.listFiles();
        if (f == null) return;
        for (int i = 0; i < f.length; i++) {
            if (f[i].isDirectory()) {
                enumEpubAll(f[i], items);
                continue;
            }
            if (f[i].getName().lastIndexOf(".epub") > 0) {
                items.add(new BookItem(f[i]));
            }
        }
        return;
    }
}
