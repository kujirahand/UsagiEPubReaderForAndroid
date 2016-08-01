package com.kujirahand.KPage;

import android.graphics.Color;

import junit.framework.TestCase;

/**
 * Created by kujira on 2016/04/18.
 */
public class KStylesTestAndroid extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void testGetColor() throws Exception {
        assert 0xFF0000 == Color.rgb(255,0,0);
    }
}