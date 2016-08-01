package com.kujirahand.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Created by kujira on 2016/04/06.
 */
public class KFile {
	
	public static void save(String path, String body) throws IOException {
    	try {
    		FileOutputStream fos = new FileOutputStream(path);
    		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    		osw.write(body);
    		osw.close();
    		fos.close();
    	} catch (Exception e) {
    		throw new IOException(e.getMessage());
    	}
	}

    public static String load(String path) throws IOException {
        // return readTextFromFileB(path);
        return readTextFromFileAll(path);
    }

    public static String readTextFromFile(String path, int maxline) throws IOException {
        File fi = new File(path);
        BufferedReader b_reader = new BufferedReader(new InputStreamReader(new FileInputStream(fi),"UTF-8"));
        StringBuilder sb = new StringBuilder();
        int line = 0;
        String tmp;
        while((tmp = b_reader.readLine())!=null){
            sb.append(tmp);
            if (maxline > 0) {
                line++;
                if (maxline <= line) break;
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    public static String readTextFromFileB(String path) throws IOException {
        File fi = new File(path);
        BufferedReader b_reader = new BufferedReader(new InputStreamReader(new FileInputStream(fi),"UTF-8"));
        StringBuilder sb = new StringBuilder();
        String tmp;
        while((tmp = b_reader.readLine())!=null){
            sb.append(tmp);
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String readTextFromFileAll(String path) throws IOException {
        File f = new File(path);
        byte[] data = new byte[(int)f.length()];
        BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(f));
        bis.read(data);
        bis.close();
        String fs = new String(data, "utf-8");
        return fs;
    }

	public static String getTitleTagFromFile(String fname) {
        try {
            String src = readTextFromFile(fname, 20);
            // lower case
            KStrTokenizer tok = new KStrTokenizer(src);
            tok.getTokenStr("<title>", true);
            String title = tok.getTokenStr("</title>", false);
            if (title.length() > 0) return title;
            // upper case
            tok.reset();
            tok.getTokenStr("<TITLE>", true);
            title = tok.getTokenStr("</TITLE>", false);
            return title;
        } catch (Exception e) {
            Log.d("UsagiPreRead", "error=" + fname);
            e.printStackTrace();
            return null;
        }
    }

    public static boolean exists(String path) {
        File f = new File(path);
        return f.exists();
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }
}
