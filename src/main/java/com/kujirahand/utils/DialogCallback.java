package com.kujirahand.utils;

/**
 * Created by kujira on 2016/04/19.
 */
public interface DialogCallback {
    final int YES = 0;
    void dialogResult(Object which);
    void dialogCancel();
}

