package com.kujirahand.KPage;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kujira on 2016/05/13.
 */
public class KTagLoaderTest {

    @Test
    public void testLoad() throws Exception {
        String src = "<html><head><title>hoge</title></head><body><div>hoge</div></body></html>";
        KTagLoader lo = new KTagLoader();
        KCharList list = lo.parseStr(src);
        assertEquals(src, list.toString());
    }
    @Test
    public void testLoad2() throws Exception {
        String src = "<html><head><title>hoge</title></head><body><div id='hoge'>hoge</div></body></html>";
        KTagLoader lo = new KTagLoader();
        KCharList list = lo.parseStr(src);
        assertEquals(src, list.toString());
    }
    @Test
    public void testLoad3() throws Exception {
        String src1 = "<html><head><title>hoge</title></head><body><div id='hoge'>hoge<br/>la</div></body></html>";
        String src2 = "<html><head><title>hoge</title></head><body><div id='hoge'>hoge<br></br>la</div></body></html>";
        KTagLoader lo = new KTagLoader();
        KCharList list = lo.parseStr(src1);
        assertEquals(src2, list.toString());
    }
    @Test
    public void testTitle() throws Exception {
        String src = "<html><head><title>fuga</title></head><body><div id='hoge'>hoge</div></body></html>";
        KTagLoader lo = new KTagLoader();
        KCharList list = lo.parseStr(src);
        String title = list.findTagText(KTagNo.TITLE, 0);
        assertEquals("fuga", title);
    }
    @Test
    public void testCloseTagError() throws Exception {
        String src = "<html><head></head><body><p><a id='hoge' href='hoge'>aaaa</p></body></html>";
        KTagLoader lo = new KTagLoader();
        KCharList list = lo.parseStr(src);
        int i = list.findTag(KTagNo.A, 0);
        KTagBegin a = (KTagBegin)list.get(i);
        assertEquals("hoge", a.id);
        String res = list.toString();
        //
        String exc = "<html><head></head><body><p><a id='hoge' href='hoge'>aaaa</a></p></body></html>";
        assertEquals(exc, res);
    }
    @Test
    public void testCloseTagError2() throws Exception {
        String src = "<html><body><p><a id='hoge'></p><p></a></p></body></html>";
        KTagLoader lo = new KTagLoader();
        KCharList list = lo.parseStr(src);
        int i = list.findTag(KTagNo.A, 0);
        KTagBegin a = (KTagBegin)list.get(i);
        assertEquals("hoge", a.id);
        //
        String exc = "<html><body><p><a id='hoge'></a></p><p></p></body></html>";
        String res = list.toString();
        assertEquals(exc, res);
    }
}