package com.kujirahand.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kujira on 2016/04/23.
 */
public class IniFileTest {

    @Test
    public void testPut() throws Exception {
        IniFile ini = new IniFile();
        ini.put("aaa", "bbb");
        assertEquals("bbb", ini.get("aaa"));
    }

    @Test
    public void testPut2() throws Exception {
        IniFile ini = new IniFile();
        ini.put("aaa", "bbb\nccc");
        assertEquals("bbb\nccc", ini.get("aaa"));
    }

    @Test
    public void testParseStr() throws Exception {
        IniFile ini = new IniFile();
        ini.parseStr("aaa=123\nbbb=333\nccc=222");
        assertEquals("123", ini.get("aaa"));
        assertEquals("333", ini.get("bbb"));
        assertEquals("222", ini.get("ccc"));
    }

    @Test
    public void testToString() throws Exception {
        IniFile ini = new IniFile();
        ini.put("aaa","11");
        ini.put("bbb","22");
        String s = ini.toString();
        assertEquals("aaa=11\nbbb=22\n", s);
    }

    @Test
    public void testLoadFromFile() throws Exception {
        IniFile ini = new IniFile();
        ini.put("aaa", "111");
        ini.put("bbb", "222");
        ini.put("ccc", "333");
        // save
        String tmpDir = System.getProperty("java.io.tmpdir");
        String tmpFile = tmpDir + "/test.ini";
        ini.saveToFile(tmpFile);
        // load
        ini.loadFromFile(tmpFile);
        assertEquals("111", ini.get("aaa"));
        assertEquals("222", ini.get("bbb"));
        assertEquals("333", ini.get("ccc"));
    }

    @Test
    public void testLoadFromFile2() throws Exception {
        IniFile ini = new IniFile();
        ini.put("aaa", "111\n222");
        ini.put("bbb", "222\n333");
        ini.put("ccc", "333\n444");
        // save
        String tmpDir = System.getProperty("java.io.tmpdir");
        String tmpFile = tmpDir + "/test.ini";
        ini.saveToFile(tmpFile);
        // load
        ini.loadFromFile(tmpFile);
        assertEquals("111\n222", ini.get("aaa"));
        assertEquals("222\n333", ini.get("bbb"));
        assertEquals("333\n444", ini.get("ccc"));
    }

    @Test
    public void testParseIni2() throws Exception {
        IniFile ini = new IniFile();
        ini.parseStr("[aaa]\nhoge=10\nfuga=20\n[bbb]\npoko=10\nfuga=20");
        assertEquals("10", ini.getSecKey("aaa","hoge"));
        assertEquals(null, ini.getSecKey("bbb","hoge"));
        assertEquals("20", ini.getSecKey("aaa","fuga"));
        assertEquals("10", ini.getSecKey("bbb","poko"));
    }

    @Test
    public void testSaveToFile() throws Exception {

    }
}