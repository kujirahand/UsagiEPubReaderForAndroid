package com.kujirahand.KPage;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kujira on 2016/04/17.
 */
public class KCharListTest {

    KCharMeasureDummy kcm = new KCharMeasureDummy();

    @Before
    public void setUp() throws Exception {
        KPageParser.charMeasure = kcm;
        kcm.width = 5;
    }

    @Test
    public void testIsChar() throws Exception {
        KCharList list = new KCharList();
        list.appendStr("abcd");
        list.setPosition(0);

        assert list.isChar();
        list.next();
        assert list.isChar();
    }

    @Test
    public void testGetIndexCharByPos() throws Exception {
        KCharList list = new KCharList();
        list.appendStr("abcd");

        int i;

        i = list.getIndexCharByPos(0, kcm);
        assertEquals(0, i);

        i = list.getIndexCharByPos(1, kcm);
        assertEquals(1, i);

        i = list.getIndexCharByPos(2, kcm);
        assertEquals(2, i);

        i = list.getIndexCharByPos(3, kcm);
        assertEquals(3, i);

        i = list.getIndexCharByPos(4, kcm);
        assertEquals(3, i);

    }
}