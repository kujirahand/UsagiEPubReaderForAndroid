package com.kujirahand.KPage;

import com.kujirahand.KXml.KXmlNode;
import com.kujirahand.KXml.KXmlParser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kujira on 2016/04/13.
 */

public class KPageParserTest {

    private KCharMeasureDummy panel = new KCharMeasureDummy();
    @Before
    public void setUp() throws Exception {
        KPageParser.charMeasure = panel;
        panel.width = 10;
    }

    @Test
    public void testSimplifyNode() throws Exception {
    }

    @Test
    public void testConvertCharList() throws Exception {

    }

    @Test
    public void testParseSimple() throws Exception {
        KXmlNode node = KXmlParser.parseString("<html><h1>aaa</h1><p>bbb</p></html>");
        KPage page = KPageParser.parseFromXMl(node);

        assertEquals("aaa", page.getLineTextOnly(0));
        assertEquals("bbb", page.getLineTextOnly(2));
    }

    @Test
    public void testParse() throws Exception {
        KXmlNode node = KXmlParser.parseString("<html><h1>aaa</h1><p><a href='hoge'>bbb<strong>ccc</strong></a></p></html>");
        KPage page = KPageParser.parseFromXMl(node);
        assertEquals("aaa", page.getLineTextOnly(0));
        assertEquals("bbbccc", page.getLineTextOnly(2));
    }

    @Test
    public void testParse1() throws Exception {
        KXmlNode node = KXmlParser.parseString("<html><body><h1>aaa</h1><p><a href='hoge'>bbb</a></p></body></html>");
        KPage page = KPageParser.parseFromXMl(node);
        assertEquals("aaa", page.getLineTextOnly(0));
        assertEquals("bbb", page.getLineTextOnly(2));
    }

    @Test
    public void testParse2() throws Exception {
        KXmlNode node = KXmlParser.parseString("<html><h1>aaa</h1><p><a href='hoge'>bbb<strong>ccc</strong></a></p></html>");
        KPage page = KPageParser.parseFromXMl(node);
        assertEquals("aaa", page.getLineTextOnly(0));
        assertEquals("bbbccc", page.getLineTextOnly(2));
    }

    @Test
    public void testParse3wordBreak() throws Exception {
        KXmlNode node = KXmlParser.parseString("<html><p>This is aaa pen.</p></html>");
        panel.width = 5;
        KPage page = KPageParser.parseFromXMl(node);
        assertEquals("This ", page.getLineTextOnly(0));
        assertEquals("is ", page.getLineTextOnly(1));
        assertEquals("aaa ", page.getLineTextOnly(2));
        assertEquals("pen.", page.getLineTextOnly(3));
    }

    @Test
    public void testParse4wordBreak() throws Exception {
        KXmlNode node = KXmlParser.parseString("<html><p>This is from JW's pen.</p></html>");
        panel.width = 5;
        KPage page = KPageParser.parseFromXMl(node);
        assertEquals("This ", page.getLineTextOnly(0));
        assertEquals("is ", page.getLineTextOnly(1));
        assertEquals("from ", page.getLineTextOnly(2));
        assertEquals("JW's ", page.getLineTextOnly(3));
        assertEquals("pen.", page.getLineTextOnly(4));
    }

    @Test
    public void testParse_KINSOKU_HEAD() throws Exception {
        KXmlNode node = KXmlParser.parseString("<html><body><p>"+
                "あいうえお、かきくけこさしすせそ</p></body></html>");
        panel.width = 5;
        KPage page = KPageParser.parseFromXMl(node);
        // System.out.println(page.toString());
        assertEquals("あいうえ", page.getLineTextOnly(0)); // 0:[p]あいうえ
        assertEquals("お、かきく", page.getLineTextOnly(1)); // 5:かきくけこ
        assertEquals("けこさしす", page.getLineTextOnly(2));
        // System.out.print(page.toString());
        //
        assertEquals(0, page.lineNo2index(0));
        assertEquals(5, page.lineNo2index(1));
    }

    @Test
    public void testPageLineNo2Index() throws Exception {
        KXmlNode node = KXmlParser.parseString("<html><body><p>あいうえおかきくけこさしすせそ</p></body></html>");
        panel.width = 5;
        KPage page = KPageParser.parseFromXMl(node);
        // System.out.println(page.toString());
        assertEquals("あいうえお", page.getLineTextOnly(0)); // 0:[p]あいうえお
        assertEquals("かきくけこ", page.getLineTextOnly(1)); // 6:かきくけこ
        assertEquals("さしすせそ", page.getLineTextOnly(2));
        // System.out.print(page.toString());
        //
        assertEquals(0, page.lineNo2index(0));
        assertEquals(6, page.lineNo2index(1));
        assertEquals(11, page.lineNo2index(2));

    }
    @Test
    public void testPageIndex2LineNo() throws Exception {
        KXmlNode node = KXmlParser.parseString("<html><body><p>あいうえおかきくけこさしすせそ</p></body></html>");
        panel.width = 5;
        KPage page = KPageParser.parseFromXMl(node);
        assertEquals("あいうえお", page.getLineTextOnly(0));// 0:[p]あいうえお
        assertEquals("かきくけこ", page.getLineTextOnly(1));// 6:かきくけこ
        assertEquals("さしすせそ", page.getLineTextOnly(2));//11:さしすせそ
        // System.out.print(page.toString());
        //
        //
        assertEquals(0, page.index2lineNo(3));
        assertEquals(0, page.index2lineNo(5));
        assertEquals(1, page.index2lineNo(6));
        assertEquals(1, page.index2lineNo(10));
        assertEquals(2, page.index2lineNo(11));
        assertEquals(2, page.index2lineNo(13));
    }

    @Test
    public void testParseSave() throws Exception {
    }

    @Test
    public void testParseDIVTag() throws Exception {
        String base = "<html><div><p>aaa</p></div><div><p>bbb</p></div></html>";
        KXmlNode node = KXmlParser.parseString(base);
        KPage page = KPageParser.parseFromXMl(node);
        String s = page.toString().trim();
        assertEquals(base, s);
    }
    @Test
    public void testParseDIVTag2() throws Exception {
        String base = "<html><div><p>aaa</p><p>bbb</p></div><p>ccc</p></html>";
        KXmlNode node = KXmlParser.parseString(base);
        KPage page = KPageParser.parseFromXMl(node);
        String s = page.toString().trim();
        assertEquals(base, s);
    }
    @Test
    public void testParseDIVTag3() throws Exception {
        String base = "<html><figure><figcaption><p>aaa<span></span>bbb</p></figcaption></figure></html>";
        KXmlNode node = KXmlParser.parseString(base);
        KPage page = KPageParser.parseFromXMl(node);
        String s = page.toString().trim();
        assertEquals(base, s);
    }
    @Test
    public void testParseDIVTag3_2() throws Exception {
        String base = "<html><figcaption> <p>aaa<span class='wd'></span><span class='wd'></span>bbb</p></figcaption></html>";
        String expect = "<html><figcaption><p>aaa<span class='wd'></span><span class='wd'></span>bbb</p></figcaption></html>";
        KXmlNode node = KXmlParser.parseString(base);
        KPage page = KPageParser.parseFromXMl(node);
        String s = page.toString().trim();
        assertEquals(expect, s);
    }
    @Test
    public void testParseDIVTag3_3() throws Exception {
        String base = "<html><figcaption> <p>aaa<span class='wd'></span><span class='wd'></span>bbb</p></figcaption></html>";
        String expect = "<html><figcaption><p>aaa<span class='wd'></span><span class='wd'></span>bbb</p></figcaption></html>";
        KXmlNode node = KXmlParser.parseString(base);
        KPage page = KPageParser.parseFromXMl(node);
        String s = page.toString().trim();
        assertEquals(expect, s);
    }

    //---------------------------------
    // ここから KCharLineを使ったモダンなUsagiReaderのテストコード

    @Test
    public void testParseTestSpace() throws Exception {
        String base   = "<html><figcaption> \n<p>aaa<span class='wd'></span><span class='wd'></span>bbb</p></figcaption></html>";
        String expect = "<html><figcaption><p>aaa<span class='wd'></span><span class='wd'></span>bbb</p></figcaption></html>";

        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.toString().trim();
        assertEquals(expect, s);
    }

    @Test
    public void testParseSplit1() throws Exception {
        panel.width = 5;
        String base   = "<html><p>あああああいいいいいうううううえええええおおおおお</p></html>";
        String expect = "あああああ\nいいいいい\nううううう\nえええええ\nおおおおお";

        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        assertEquals(expect, s);
    }
    @Test
    public void testParseSplitEnglish() throws Exception {
        panel.width = 5;
        String base   = "<html><p>This is a pen.</p></html>";
        String expect = "This \nis a \npen.";
        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        assertEquals(expect, s);
    }
    @Test
    public void testParseSplit2() throws Exception {
        panel.width = 5;
        String base   = "<html><p><p><p>あいう</p></p></p><p><p><p>あいう</p></p></p></html>";
        String expect = "あいう\n\nあいう";
        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        assertEquals(expect, s);
    }
    @Test
    public void testParseSplit3() throws Exception {
        panel.width = 5;
        String base   = "<html><div><p><p>あい</p></p></div><div><p><p>あいう</p></p></div></html>";
        String expect = "あい\n\nあいう";
        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        assertEquals(expect, s);
    }

    @Test
    public void testParseSplit4() throws Exception {
        panel.width = 5;
        String base   = "<html><div><h3>あい</h3><p>ほげ</p></div><div><p><p>あいう</p></p></div></html>";
        String expect = "あい\n\nほげ\n\nあいう";
        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        assertEquals(expect, s);
    }

    @Test
    public void testParseSplit5() throws Exception {
        panel.width = 5;
        String base   = "<ul><li><p>愛</p></li><li><p>知恵</p></li><li><p>勇気</p></li></ul>";
        String expect = "愛\n\n知恵\n\n勇気";
        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        assertEquals(expect, s);
    }
    @Test
    public void testParseSplitKinsoku1() throws Exception {
        panel.width = 5;
        String base   = "ああ今日は、雨だ";
        String expect = "ああ今日\nは、雨だ";
        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        assertEquals(expect, s);
    }
    @Test
    public void testParseSplitKinsoku2() throws Exception {
        panel.width = 5;
        String base   = "「あ今日は」雨だ";
        String expect = "「あ今日\nは」雨だ";
        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        assertEquals(expect, s);
    }
    @Test
    public void testParseSplitKinsoku3() throws Exception {
        panel.width = 5;
        String base   = "あああああ。い";
        String expect = "ああああ\nあ。い";
        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        assertEquals(expect, s);
    }

    @Test
    public void testParseFromCharList() throws Exception {
        panel.width = 15;
        String base = "<div class=\"bodyTxt\"><div id=\"section1\" class=\"section\"><div class=\"pGroup\">"+
                "<ul><li><p id=\"p3\" data-pid=\"3\" class=\"p3\">  <span id=\"pcitationsource1\"></span>"+
                "<a href=\"202016121-extracted.xhtml#pcitation1\">79<span class=\"wd\"></span>番<span class=\"wd\"></span>の<span class=\"wd\"></span>歌</a>"+
                "<span class=\"wd\"></span>と<span class=\"wd\"></span>祈り</p></li>"+
                "<li><p id=\"p4\" data-pid=\"4\" class=\"p4\">  開会<span class=\"wd\"></span>の<span class=\"wd\"></span>言葉（3<span class=\"wd\"></span>分<span class=\"wd\"></span>以内）</p></li>"+
                "</ul></div></div>\n<div>神の</div>";
        String expect = "79番の歌と祈り\n\n 開会の言葉（3分以内）\n\n神の";
        KCharList list = KTagLoader.loadFromStr(base);
        KPage page = KPageParser.parse(list);
        String s = page.sourceSplitByLinesTextOnly().trim();
        //String s2 = page.sourceSplitByLines().trim();
        assertEquals(expect, s);
    }

    public void testGetCharWidthPx() throws Exception {

    }

    public void testCharwidth() throws Exception {

    }

    public void testStrwidth() throws Exception {

    }
}