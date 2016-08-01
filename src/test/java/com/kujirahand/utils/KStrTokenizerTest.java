package com.kujirahand.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kujira on 2016/04/13.
 */
public class KStrTokenizerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testNext() throws Exception {
        KStrTokenizer tok = new KStrTokenizer("abc");
        char c1 = tok.getChar();
        assert c1 == 'a';
        char c2 = tok.getChar();
        assert c2 == 'b';
        char c3 = tok.getChar();
        assert c3 == 'c';
        char c4 = tok.getChar();
        assert c4 == '\0';
        char c5 = tok.getChar();
        assert c5 == '\0';
    }

    @Test
    public void testPrev() throws Exception {
        KStrTokenizer tok = new KStrTokenizer("abc");
        char c1 = tok.getChar();
        assert c1 == 'a';
        char c2 = tok.peek();
        assert c2 == 'b';
        tok.prev();
        char c3 = tok.getChar();
        assert c3 == 'a';
        tok.prev();
        char c4 = tok.getChar();
        assert c4 == 'a';
        char c5 = tok.getChar();
        assert c5 == 'b';
    }

    @Test
    public void testSkipSpace() throws Exception {
        KStrTokenizer tok = new KStrTokenizer("   aaa\t\t\tbbb");
        tok.skipSpace();
        assertEquals("aaa", tok.getCurStr(3));
        tok.skipSpace();
        assertEquals("bbb", tok.getCurStr(3));
        tok.skipSpace();
        assertEquals("", tok.getCurStr(3));
    }

    @Test
    public void testSkipSpaceReturn() throws Exception {
        KStrTokenizer tok = new KStrTokenizer(" \t\n  aaa\t\r\n\t     bbb");
        tok.skipSpaceReturn();
        assertEquals("aaa", tok.getCurStr(3));
        tok.skipSpaceReturn();
        assertEquals("bbb", tok.getCurStr(3));
        tok.skipSpaceReturn();
        assertEquals("", tok.getCurStr(3));
    }

    @Test
    public void testIsEOS() throws Exception {

    }

    @Test
    public void testGetToken() throws Exception {

    }

    @Test
    public void testGetTokenStr() throws Exception {
        KStrTokenizer tok = new KStrTokenizer("a<<<aaa>>>bbb");
        String a = tok.getTokenStr("<<<", true);
        assertEquals("a", a);
        String b = tok.getTokenStr(">>>", true);
        assertEquals("aaa", b);
        String c = tok.getTokenStr(">>>", true);
        assertEquals("bbb", c);
        //
        tok.reset();
        String d = tok.getTokenStr("<<<", false);
        assertEquals("a", d);
        String e = tok.getTokenStr("<<<", true);
        assertEquals("", e);
        String f = tok.getTokenStr(">>>", false);
        assertEquals("aaa", f);
        String g = tok.getTokenStr(">>>", false);
        assertEquals("", g);

    }

    @Test
    public void testCompareStr() throws Exception {
        KStrTokenizer tok = new KStrTokenizer("a   bc");
        char c = tok.getChar();
        assert c == 'a';
        tok.skipSpace();
        assert tok.compareStr("bc");
        String sub = tok.getCurStr(2);
        assert sub.equals("bc");
    }
}