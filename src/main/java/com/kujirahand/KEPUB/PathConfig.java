package com.kujirahand.KEPUB;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by kujira on 2016/04/23.
 */
public class PathConfig {
    public static Context context;
    private static String cache_dir = null; // Activityの起動時にセットされる

    public static File getCacheDirAllBooks() {
        File cache_f = context.getExternalCacheDir();
        return cache_f;
    }

    public static String getCacheDir(String epub_path) {
        if (cache_dir == null) {
            File cache_f = getCacheDirAllBooks();
            cache_dir = cache_f.getAbsolutePath();
        }
        String cpath = cache_dir + "/" + epub_path.replaceAll("/", "_");
        return cpath;
    }
    public static String getCoverImage(String epub_path) {
        String cache_dir = getCacheDir(epub_path);
        return cache_dir + "/cover.png";
    }
    public static String getIniFilePath(String epub_path) {
        String cache_dir = getCacheDir(epub_path);
        return cache_dir + "/info_usagireader.ini";
    }

    public static String getStorageDir() {
        File ext_dir = Environment.getExternalStorageDirectory();
        return ext_dir.getAbsolutePath();
    }

    public static String getBooksDir() {
        File ext_dir = Environment.getExternalStorageDirectory();
        File books_dir = new File(ext_dir, "Books");
        if (!books_dir.exists()) {
            books_dir.mkdirs();
            Log.d("dir", "created=" + books_dir.getAbsolutePath());
        }
        Log.d("dir", "getBooksDir=" + books_dir.getAbsolutePath());
        return books_dir.getAbsolutePath();
    }

    public static String getMyBooksDir(int no) {
        File ext_dir = Environment.getExternalStorageDirectory();
        File books_dir = new File(ext_dir, "Books");
        File myBooks_dir = new File(books_dir, "UsagiReader");
        if (no > 0) {
            myBooks_dir = new File(myBooks_dir, "/shelf_" + no);
        }
        if (!myBooks_dir.exists()) {
            myBooks_dir.mkdirs();
            Log.d("dir", "created=" + myBooks_dir.getAbsolutePath());
        }
        Log.d("dir", "getMyBooksDir=" + myBooks_dir.getAbsolutePath());
        return myBooks_dir.getAbsolutePath();
    }
    // moon
    public static String getMoonBooksDir() {
        File ext_dir = Environment.getExternalStorageDirectory();
        File books_dir = new File(ext_dir, "Books");
        File myBooks_dir = new File(books_dir, "MoonReader");
        return myBooks_dir.getAbsolutePath();
    }
    // 暫定
    public static String getDropboxDir() {
        File ext_dir = Environment.getExternalStorageDirectory();
        File dropbox_dir = new File(ext_dir, "Dropbox");
        Log.d("dir", "DropboxDir=" + dropbox_dir.getAbsolutePath());
        return dropbox_dir.getAbsolutePath();
    }

}
