package com.kujirahand.KEPUB.utils;

import android.util.Log;

/**
 * Created by kujira on 2016/04/13.
 */
public class ProcTime {
    long startTime;
    String what;
    static ProcTime[] stack = new ProcTime[10];
    static int stack_index = 0;

    public void start(String what) {
        startTime = System.nanoTime();
        this.what = what;
    }
    public long peek(String what2) {
        String w2 = (what2 == null) ? "" : what2;
        long ptime = System.nanoTime() - startTime;
        long ms = ptime / 1000000;
        Log.d("ProcTime", "| peek " + w2  + "=" + ms + "ms");
        return ms;
    }
    public long peekTime() {
        long ptime = System.nanoTime() - startTime;
        long ms = ptime / 1000000;
        return ms;
    }
    public void end() {
        long ptime = System.nanoTime() - startTime;
        long ms = ptime / 1000000;
        Log.d("ProcTime", what + "=" + ms + "ms");
    }
    public String endGetStr() {
        long ptime = System.nanoTime() - startTime;
        long ms = ptime / 1000000;
        return (what + "=" + ms + "ms");
    }

    private static void stack_push(ProcTime n) {
        if (stack.length > stack_index) {
            stack[stack_index] = n;
            stack_index++;
        }
    }
    private static ProcTime stack_pop() {
        ProcTime n = null;
        if (stack_index > 0) {
            n = stack[stack_index - 1];
            stack_index--;
        }
        return n;
    }
    public static ProcTime create(String what) {
        ProcTime n = new ProcTime();
        n.start(what);
        stack_push(n);
        return n;
    }
    public static void finish() {
        ProcTime last = stack_pop();
        if (last == null) return;
        last.end();
    }
    public static String finishStr() {
        ProcTime last = stack_pop();
        if (last == null) return "unkwnon";
        return last.endGetStr();
    }
}
