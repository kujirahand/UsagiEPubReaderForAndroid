package com.kujirahand.utils;

import android.content.Context;
import android.util.Log;

import com.kujirahand.KEPUB.EPubFile;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by kujira on 2016/04/03.
 */
public class ZipUtil {

    final private static int MAX_FILE_SIZE = 1024 * 1024 * 100; // 100MB

    public static List<String> extract(String filename, String basedir) {
        ZipInputStream in = null;
        BufferedOutputStream out = null;

        ZipEntry zipEntry = null;
        int len = 0;
        List<String> list = new ArrayList<>();

        try {
            in = new ZipInputStream(new FileInputStream(filename));

            // ZIPファイルに含まれるエントリに対して順にアクセス
            while ((zipEntry = in.getNextEntry()) != null) {
                File newfile = new File(basedir + "/" + zipEntry.getName());
                EPubFile.log("zip.entry=" + zipEntry.toString());
                if (newfile.exists()) continue;
                // mkdir
                File dir = newfile.getParentFile();
                if (!dir.exists()) dir.mkdirs();
                // 出力用ファイルストリームの生成
                FileOutputStream file = new FileOutputStream(newfile);
                out = new BufferedOutputStream(file);
                // エントリの内容を出力
                int total = 0;
                byte[] buffer = new byte[1024];
                while ((len = in.read(buffer)) != -1) {
                    total += len;
                    if (total > MAX_FILE_SIZE) {
                        // too big!!
                        break;
                    }
                    out.write(buffer, 0, len);
                }
                in.closeEntry();
                out.close();
                out = null;
                list.add(zipEntry.getName());
            }
            return list;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String extractString(String zip_path, String filename) {
        byte[] b = extractBytes(zip_path, filename);
        if (b == null) return null;
        try {
            return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(b);
        }
    }

    public static byte[] extractBytes(String zip_path, String filename) {
        ZipInputStream in = null;
        ZipEntry zipEntry = null;
        String res = "";
        try {
            in = new ZipInputStream(new FileInputStream(zip_path));

            // ZIPファイルに含まれるエントリに対して順にアクセス
            while ((zipEntry = in.getNextEntry()) != null) {
                // 出力用ファイルストリームの生成
                String entry_name = zipEntry.getName();
                // Log.d("ZIP", entry_name);
                if (!entry_name.equals(filename)) continue;
                // Check too big
                if (zipEntry.getSize() > MAX_FILE_SIZE) return null;
                // エントリの内容を出力
                byte[] buffer = new byte[(int)zipEntry.getSize()];
                in.read(buffer);
                in.closeEntry();
                return buffer;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void compress(List inputFiles, String basePath, String outputFile) {
        // 入力ストリーム
        InputStream is = null;

        // ZIP形式の出力ストリーム
        ZipOutputStream zos = null;

        // 入出力用のバッファを作成
        byte[] buf = new byte[1024];

        // ZipOutputStreamオブジェクトの作成
        try {
            zos = new ZipOutputStream(new FileOutputStream(outputFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < inputFiles.size(); i++) {
                // 入力ストリームのオブジェクトを作成
                String filename = (String)inputFiles.get(i);
                String savename = filename;
                is = new FileInputStream(filename);

                // Setting Filename
                if (savename.indexOf(basePath) >= 0) {
                    savename = savename.substring(basePath.length());
                }

                // ZIPエントリを作成
                ZipEntry ze = new ZipEntry(savename);

                // 作成したZIPエントリを登録
                zos.putNextEntry(ze);

                // 入力ストリームからZIP形式の出力ストリームへ書き出す
                int len = 0;
                while ((len = is.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }

                // 入力ストリームを閉じる
                is.close();

                // エントリをクローズする
                zos.closeEntry();
            }

            // 出力ストリームを閉じる
            zos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

