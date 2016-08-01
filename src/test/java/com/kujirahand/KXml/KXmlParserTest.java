package com.kujirahand.KXml;

import com.kujirahand.KEPUB.utils.ProcTime;
import com.kujirahand.utils.KFile;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kujira on 2016/04/18.
 */
public class KXmlParserTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testLoadAndParse() throws Exception {

    }

    @Test
    public void testParseString() throws Exception {
        KXmlNode node = KXmlParser.parseString("<root><a>abc</a></root>");
        KXmlNode fnode = node.firstChild();
        fnode.setIndentLevel(0);
        String s = fnode.toString();
        assertEquals("<root><a>abc</a></root>", s);
    }
    @Test
    public void testReadOne_space() throws Exception {
        KXmlNode node = KXmlParser.parseString("<root><a> abc</a></root>");
        KXmlNode fnode = node.firstChild();
        fnode.setIndentLevel(0);
        String s = fnode.toString();
        assertEquals("<root><a> abc</a></root>", s);
    }
    @Test
    public void testReadOne2() throws Exception {
        KXmlNode node = KXmlParser.parseString("<root>aaa<b></b>bbb</root>");
        KXmlNode fnode = node.firstChild();
        fnode.setIndentLevel(0);
        String s = fnode.toString();
        assertEquals("<root>aaa<b />bbb</root>", s);
    }

    @Test
    public void testDecodeSpecialChars() throws Exception {
        String a = "&lt;div&gt;";
        String b = KXmlParser.decodeSpecialChars(a);
        assertEquals("<div>", b);

        String a2 = "&quot;div&quot;";
        String b2 = KXmlParser.decodeSpecialChars(a2);
        assertEquals("\"div\"", b2);

        String pad = "";
        String tmp = "0123456789<BCD>F"; // 16
        for (int i = 0; i < 64; i++) {
            pad += tmp;
        }
        // 1kb
        String src = "";
        for (int i = 0; i < 200; i++) src += tmp;

        //
        long tm;
        tm = System.nanoTime();
        for (int i = 0; i < 256; i++) {
            KXmlParser.decodeSpecialCharsEasy(src);
        }
        System.out.println("old=" + (System.nanoTime() - tm));
        //
        tm = System.nanoTime();
        for (int i = 0; i < 256; i++) {
            KXmlParser.decodeSpecialChars(src);
        }
        System.out.println("new=" + (System.nanoTime() - tm));
    }

    @Test
    public void testRead() throws Exception {
    }

    @Test
    public void testReadOne() throws Exception {

    }

    @Test
    public void testReadTag() throws Exception {

    }

    @Test
    public void testParse() throws Exception {

    }
}
