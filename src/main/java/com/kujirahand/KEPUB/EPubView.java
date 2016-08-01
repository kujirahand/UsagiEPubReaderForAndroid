package com.kujirahand.KEPUB;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.kujirahand.KEPUB.async.EPubFileLoaderAsync;
import com.kujirahand.KEPUB.menu.MainMenu;
import com.kujirahand.KEPUB.panels.EPubPanel;
import com.kujirahand.KEPUB.panels.EPubPanelHeader;
import com.kujirahand.KEPUB.panels.EPubPanelStatus;
import com.kujirahand.KEPUB.panels.EPubPanelRef;
import com.kujirahand.KEPUB.panels.EPubPanelView;
import com.kujirahand.KEPUB.utils.EPubTouchInfo;
import com.kujirahand.KEPUB.utils.SizeBox;
import com.kujirahand.KPage.KPage;
import com.kujirahand.usagireader.R;
import com.kujirahand.usagireader.ReaderActivity;
import com.kujirahand.usagireader.ShelfActivity;
import com.kujirahand.utils.DialogCallback;
import com.kujirahand.utils.DialogHelper;
import com.kujirahand.utils.KFile;
import com.kujirahand.utils.KPref;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by kujira on 2016/04/07.
 */

public class EPubView extends View {

    final public static String DEBUG_TAG = "UsagiEpub";
    final float DENSITY = getContext().getResources().getDisplayMetrics().density;
    public static boolean isReady = false;
    public boolean fullscreenMode = true;

    private int screenWidth, screenHeight;
    private boolean attached = false;
    public EPubFile epubFile;

    final float defalt_fontSizeDp = 16f;
    final float defalt_lineHeightRate = 1.5f;

    private float fontSizeDp = defalt_fontSizeDp;
    private float lineHeightRate = defalt_lineHeightRate;
    private float fontHeight;
    private float fontSizePx;
    // theme ... ref:changeColorTheme()
    public int backColor = Color.WHITE;
    public int textColor = Color.BLACK;
    public int linkColor = Color.rgb(80, 80, 255);
    public int backRefColor = Color.rgb(240,240,240);
    //
    public int borderColor = Color.rgb(200,200,200);
    //
    final public int iconColor = Color.rgb(75,75,75);

    public  Paint text_p = new Paint();
    public  Paint back_p = new Paint();
    public Typeface normal_tf;
    public Typeface bold_tf;

    private ArrayList<EPubPanel> panels = new ArrayList<EPubPanel>();

    // Panels
    public EPubPanelStatus statusPanel = null;
    public EPubPanelView viewPanel = null;
    public EPubPanelHeader headPanel = null;
    public EPubPanelRef refPanel = null;

    public boolean flagBackkeyPressed = false;
    private String overlayText = "Now Preparing...";
    private static SharedPreferences pref = null;

    public EPubFileLoaderAsync epubLoader;
    public static ReaderActivity activity;


    public EPubView(Context context) {
        super(context);
        setFocusable(true);
        MainMenu.view = this;
        // ---
        epubFile = null;
        isReady = false;
    }

    public void setFullScreen(boolean b) {
        if (b) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= 19) {
                hideSystemUI();
            }
        }
        else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= 19) {
                showSystemUI();
            }
        }
        fullscreenMode = b;
    }
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decor = activity.getWindow().getDecorView();
            decor.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void showSystemUI() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decor = activity.getWindow().getDecorView();
            decor.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    public SharedPreferences getPref() {
        if (pref == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        }
        return pref;
    }

    public static boolean canContinue(Context ctx) {
        if (pref == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        }
        String epub_path = pref.getString("epub_path", null);
        return (epub_path != null);
    }

    public static String getContinueFile(Context ctx) {
        if (pref == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        }
        String epub_path = pref.getString("epub_path", null);
        return epub_path;
    }

    public static void setContinueFile(Context ctx, String epub_file, String html_file, int cur_index, String link_id) {
        if (pref == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        }
        SharedPreferences.Editor edt = pref.edit();
        edt.putString("epub_path", epub_file);
        edt.putString("html_path", html_file);
        edt.putInt("cur_index", cur_index);
        edt.putString("link_id", link_id);
        edt.commit();
    }

    public void continueReading() {
        if (pref == null) pref = getPref();
        String epub_path = pref.getString("epub_path", null);
        String html_path = pref.getString("html_path", null);
        int cur_index = pref.getInt("cur_index", 0);
        String link_id = pref.getString("link_id", null);

        if (epub_path == null) return;
        // already loading ...
        if (epubLoader != null) return;
        epubLoader = new EPubFileLoaderAsync(this, epub_path, html_path, cur_index, link_id);
        epubLoader.execute();

        setOverlayText(viewPanel.lang(R.string.NOW_LOADING));
    }

    public void onResume() {
        log("EPubView.onResume");
        this.invalidate();
    }


    public void onSaveInfo(Bundle state) {
        if (flagBackkeyPressed) return;
        if (headPanel == null) return;
        //
        KPage page = viewPanel.getPage();
        if (page == null) return;
        //
        String epub_path = epubFile.getEpubPath();
        String html_path = page.path;
        int cur_index = page.lineNo2index(viewPanel.getScrollTop());
        setContinueFile(getContext(), epub_path, html_path, cur_index, null);
        log("@onPause:save reading info");
        log("| - epub_path = " + epub_path);
        log("| - html_path = " + html_path);
        log("| - cur_index = " + cur_index);
        // save page
        try {
            epubFile.savePage(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRestoreInfo(Bundle state) {
    }

    public void onPause() {
        log("onPause");
    }

    public static void log(String msg) {
        Log.d(DEBUG_TAG, msg);
    }

    public void showError(String msg) {
        Log.e(DEBUG_TAG, msg);
        Toast.makeText(getContext().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    public int dp2px(float dip) {
        return (int)Math.round(dip * DENSITY);
    }
    public float dp2pxF(float dip) {
        return dip * DENSITY;
    }
    public int px2dp(float px) {
        return (int)Math.round(px / DENSITY);
    }

    public float getFontSizeDp() {
        return fontSizeDp;
    }
    public float getLineHeightRate() {
        return lineHeightRate;
    }
    public float getFontSizePx() {
        return fontSizePx;
    }

    public void changeFontSizeDp(int dip) {
        setTypeface(dip);
        viewPanel.changeFontSize();
        refPanel.changeFontSize();
        getPref()
                .edit()
                .putInt("fontSizeDp", dip)
                .commit();
        viewPanel.reloadPage();
    }
    public void changeLineHeightRate(float rate) {
        lineHeightRate = rate;
        setTypeface(fontSizeDp);
        viewPanel.changeFontSize();
        getPref()
                .edit()
                .putFloat("lineHeightRate", rate)
                .commit();
        viewPanel.reloadPage();
    }
    public void changeColorTheme(String theme) {
        getPref().edit()
                .putString("colorTheme", theme.toLowerCase())
                .commit();
        viewPanel.changeColorTheme();
        viewPanel.changeFontSize();
        viewPanel.reloadPage();
        headPanel.drawPanel();
        refPanel.reloadPage();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        log("@onSizeChanged = " + w + "," + h);
        screenWidth = w;
        screenHeight = h;
        //
        SharedPreferences pref = getPref();
        this.fontSizeDp = pref.getInt("fontSizeDp", (int)this.fontSizeDp);
        this.lineHeightRate = pref.getFloat("lineHeightRate", this.lineHeightRate);
        setTypeface(this.fontSizeDp);
        setLayout();
        //
        isReady = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                log("@ready");
                continueReading();
            }
        }, 1);
    }
    public void initThemeSetting() {
        getPref().edit()
                .putInt("fontSizeDp", (int)this.defalt_fontSizeDp)
                .putFloat("lineHeightRate", this.defalt_lineHeightRate)
                .putString("colorTheme", "white")
                .commit();
        //
        DialogHelper.dialogYesNo(
                "Initialize theme",
                "Can I close this window?",
                new DialogCallback() {
                    @Override
                    public void dialogResult(Object which) {
                        finishReading();
                    }
                    @Override
                    public void dialogCancel() {
                    }
                });
    }

    private void setTypeface(float fontSizeDp) {
        this.fontSizeDp = fontSizeDp;

        // Set Typeface
        normal_tf = Typeface.DEFAULT;
        bold_tf = Typeface.DEFAULT_BOLD;

        // Set to Paint
        fontSizePx = dp2px(fontSizeDp);
        text_p.setTypeface(normal_tf);
        text_p.setTextSize(fontSizePx);
        text_p.setAntiAlias(true);
        text_p.setColor(textColor);
        back_p.setColor(backColor);

        // Calc FontSize
        String love = "愛gM";
        Rect bounds = new Rect();
        text_p.getTextBounds(love, 0, 3, bounds);
        fontHeight = bounds.height() * lineHeightRate;
        log("fontHeight(bounds)=" + bounds.height());
        log("fontHeight=" + fontHeight);
    }

    // TODO: setLayout
    private void setLayout() {
        // delete panels
        for (EPubPanel p : panels) { p.recycle(); }
        panels.clear();

        // STATUS PANEL
        int info_h     = dp2px(20);
        SizeBox szInfo = new SizeBox();
        szInfo.box     = new Rect(0, screenHeight - info_h, screenWidth, screenHeight);
        szInfo.margin  = new Rect(dp2px(2), dp2px(2), dp2px(2), dp2px(2));
        statusPanel    = new EPubPanelStatus(this, szInfo, "statusPanel");
        panels.add(statusPanel);

        // HEADER PANEL
        int head_h = dp2px(40);
        SizeBox szHead = new SizeBox();
        szHead.box = new Rect(0, 0, screenWidth, head_h);
        szHead.margin = new Rect(dp2px(15), dp2px(2), dp2px(15), dp2px(2));
        headPanel = new EPubPanelHeader(this, szHead, "headPanel");
        panels.add(headPanel);

        int ref_w = 0;

        // MAIN VIEW
        int view_bottom = (int)(screenHeight - info_h);
        int view_margin = dp2px(20);
        SizeBox szView = new SizeBox();
        szView.box    = new Rect(0, head_h, screenWidth - ref_w, view_bottom);
        szView.margin = new Rect(view_margin, (int)(view_margin * 1.5), view_margin, (int)(view_margin / 2));
        viewPanel = new EPubPanelView(this, szView, "viewPanel");
        panels.add(viewPanel);

        // REFPANEL
        SizeBox szRef = new SizeBox();
        szRef.box = new Rect((int)(screenWidth - ref_w), (int)head_h, screenWidth, (int)view_bottom);
        szRef.margin = new Rect(dp2px(10), dp2px(28), dp2px(10), dp2px(8));
        log("szRef=" + szRef.margin.toString());
        refPanel = new EPubPanelRef(this, szRef, "refPanel");
        panels.add(refPanel);



        // initPanel
        for (EPubPanel p: panels) {
            p.initPanel();
        }
    }

    public void showRefPanel(boolean b, Runnable callback) {
        if (!b) {
            SizeBox szRef = refPanel.getSize();
            szRef.box.left = screenWidth;
            szRef.box.right = screenWidth;
            refPanel.setSize(szRef);
            //
            SizeBox szView = viewPanel.getSize();
            szView.box.left = 0;
            szView.box.right = screenWidth;
            viewPanel.setSize(szView);
        } else {
            // 縦置きか横置きかで幅を判断
            int ref_w;
            if (screenWidth > screenHeight) {
                ref_w = (int)(screenWidth / 4);
            } else {
                ref_w = (int)(screenWidth / 3);
            }
            log("#refPanelSize=" + ref_w);
            SizeBox szRef = refPanel.getSize();
            szRef.box.left = screenWidth - ref_w;
            szRef.box.right = screenWidth;
            log("szRef=" + szRef.margin.toString());
            refPanel.setSize(szRef);
            refPanel.drawPanel();
            //
            SizeBox szView = viewPanel.getSize();
            szView.box.left = 0;
            szView.box.right = szRef.box.left;
            viewPanel.setSize(szView);
        }
        // reload main contents
        viewPanel.reloadPage();
        // add-on
        new Handler().postDelayed(callback, 1);
        setOverlayText(viewPanel.lang(R.string.NOW_LOADING));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // background
        canvas.drawRect(canvas.getClipBounds(), back_p);

        // draw every panel
        for (EPubPanel panel : panels) {
            panel.drawTo(canvas);
        }

        if (overlayText != null) {
            drawOverlayText(canvas);
        }
    }

    public float getFontHeight() {
        return fontHeight;
    }

    @Override
    protected void onAttachedToWindow() {
        this.attached = true;
        super.onAttachedToWindow();
    }
    @Override
    protected void onDetachedFromWindow() {
        this.attached = false;
        super.onDetachedFromWindow();
    }

    private EPubTouchInfo touchInfo = new EPubTouchInfo();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        touchInfo.setPos(x, y);
        boolean b = true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //log("MotionEvent.ACTION_DOWN");
                touchInfo.down(x, y);
                for (EPubPanel p : panels) {
                    if (p.isHit(x, y)) {
                        touchInfo.box = p.getSize().box;
                        b = p.onTouchDown(touchInfo);
                        if (!b) break;
                    }
                }
                checkLongTap();
                break;
            case MotionEvent.ACTION_MOVE:
                touchInfo.move(x, y);
                for (EPubPanel p : panels) {
                    if (p.captureTouch || p.isHit(x, y)) {
                        touchInfo.box = p.getSize().box;
                        b = p.onTouchMove(touchInfo);
                        if (!b) break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //log("MotionEvent.ACTION_UP");
                touchInfo.up(x, y);
                for (EPubPanel p : panels) {
                    if (p.captureTouch || p.isHit(x, y)) {
                        touchInfo.box = p.getSize().box;
                        b = p.onTouchUp(touchInfo);
                        if (!b) break;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                log("ACTION_CANCEL");
                viewPanel.drawPanel();
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    private void checkLongTap() {
        final float move_pos_max = fontHeight / 2;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (touchInfo.isDown()) {
                    if (touchInfo.move_pos > move_pos_max) return;
                    if (viewPanel.getSize().isHit(touchInfo.x, touchInfo.y)) {
                        viewPanel.onLongTap(touchInfo);
                    }
                    else if (refPanel.getSize().isHit(touchInfo.x, touchInfo.y)) {
                        refPanel.onLongTap(touchInfo);
                    }
                }
            }
        }, 300);
    }

    public void showInfo(String msg) {
        Toast.makeText(getContext().getApplicationContext(), msg, Toast.LENGTH_LONG);
    }

    public void setOverlayText(String msg) {
        this.overlayText = msg;
        this.invalidate();
    }

    public void drawOverlayText(Canvas canvas) {
        if (overlayText == null || overlayText.equals("")) return;
        if (canvas == null) return;

        float y = 2 * (screenHeight / 4);

        // Cover image
        if (epubFile == null) {
            String epub_path = getContinueFile(getContext());
            if (epub_path != null) {
                String cover_image = PathConfig.getCoverImage(epub_path);
                Bitmap cover_bmp = BitmapFactory.decodeFile(cover_image);
                if (cover_bmp == null) {
                    cover_bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.book);
                }
                canvas.drawBitmap(cover_bmp, (screenWidth - cover_bmp.getWidth()) / 2, y, null);
                y += cover_bmp.getHeight();
                y += fontHeight;
            }
        } else {
            y = 3 * (screenHeight / 4);
        }

        Rect bounds = new Rect();
        Paint pt = new Paint();
        pt.setAntiAlias(true);
        pt.setColor(Color.GRAY);
        pt.setTypeface(Typeface.DEFAULT);
        pt.setTextSize(dp2pxF(20));
        pt.getTextBounds(overlayText, 0, overlayText.length(), bounds);
        int back_w = (int)(bounds.width() + dp2px(20) * 2);
        int back_x = (screenWidth - back_w) / 2;
        int text_h = (int)(bounds.height() * 1.5);

        Paint p = new Paint();
        p.setColor(Color.argb(220, 255, 255, 255));
        p.setStyle(Paint.Style.FILL);
        Rect area = new Rect(back_x,
                (int)(y - text_h),
                (int)(back_x + back_w),
                (int)(y + text_h / 2));
        canvas.drawRect(area, p);
        p.setColor(Color.GRAY);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawRect(area, p);

        float w = pt.measureText(overlayText);
        canvas.drawText(overlayText, (screenWidth - w) / 2, y, pt);

    }

    public void openWeb(String href) {
        Uri uri = Uri.parse(href);
        Intent i = new Intent(Intent.ACTION_VIEW,uri);
        getContext().startActivity(i);

    }

    public Activity getActivity() {
        return (Activity)this.getContext();
    }

    private static void moveToMyBooksNo(final Activity activity, String epub_path, int no) {
        String mybook_s = PathConfig.getMyBooksDir(no);
        final File epub_f = new File(epub_path);
        String epub_name = epub_f.getName();
        final String new_path = mybook_s + "/" + epub_name;
        if (new_path.equals(epub_path)) return; // same path

        final File new_path_f = new File(new_path);
        String cache_path = PathConfig.getCacheDir(epub_path);
        final File cache_path_f = new File(cache_path);
        String new_cache_path = PathConfig.getCacheDir(new_path);
        final File new_cache_path_f = new File(new_cache_path);
        final Runnable exec = new Runnable() {
            @Override
            public void run() {
                log("copyFrom=" + epub_f.getAbsolutePath());
                log("copyTo=" + new_path_f.getAbsolutePath());
                epub_f.renameTo(new_path_f);
                if (cache_path_f.exists()) {
                    cache_path_f.renameTo(new_cache_path_f);
                    log("copied cache=" + cache_path_f.getAbsolutePath());
                }
                Toast.makeText(activity, DialogHelper.lang(R.string.SAVED), Toast.LENGTH_SHORT).show();
                // reload epubfile
                if (activity == ReaderActivity.instance) {
                    ((ReaderActivity)activity).finishReading();
                }
                else if (activity == ShelfActivity.instance) {
                    ((ShelfActivity)activity).refreshBooks();
                }
            }
        };
        if (new_path_f.exists()) {
            DialogHelper.dialogYesNo("Question", MainMenu.lang(R.string.Q_FILE_ALREADY_OVERWRITE),
                    new DialogCallback() {
                        @Override
                        public void dialogResult(Object which) {
                            if ((int)which == YES) {
                                exec.run();
                            }
                        }
                        @Override
                        public void dialogCancel() {
                        }
                    });
        } else {
            exec.run();
        }
    }

    public static void moveToMyBooks(final Activity activity, final String epub_path) {
        final String[] shelfNames = new String[5];
        for (int i = 0; i < 5; i++) {
            String name = KPref.getStr("MyBooks.name." + i, "MyBooks " + (i + 1));
            shelfNames[i] = name;
        }
        DialogHelper.selectListNo("", "", shelfNames, new DialogCallback() {
            @Override
            public void dialogResult(Object which) {
                int whichNo = (int)which;
                moveToMyBooksNo(activity, epub_path, whichNo + 1);
            }
            @Override
            public void dialogCancel() {
            }
        });
    }

    public void removeCacheAllBooks() {
        final File all_cache_f = PathConfig.getCacheDirAllBooks();
        if (all_cache_f == null) return;
        if (all_cache_f.getAbsolutePath().length() < 5) return;
        final File remove_f = all_cache_f;
        if (!remove_f.exists()) return;
        DialogHelper.dialogYesNo("Remove ALL Books Marker Data", "Really?", new DialogCallback() {
            @Override
            public void dialogResult(Object which) {
                KFile.deleteRecursive(all_cache_f);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setContinueFile(getContext(), null, null, 0, null);
                        finishReading();
                    }
                },500);
            }
            @Override
            public void dialogCancel() {
            }
        });
    }
    public void removeAllMarker() {
        final String epub_path = this.epubFile.getEpubPath();
        final String cache_path = this.epubFile.getCacheDir();
        final File cache_f = new File(cache_path);
        if (!cache_f.exists()) return;
        DialogHelper.dialogYesNo("Remove All Marker in this book", "Really?",
                new DialogCallback() {
            @Override
            public void dialogResult(Object which) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        KFile.deleteRecursive(cache_f);
                        //
                        setContinueFile(getContext(), null, null, 0, null);
                        finishReading();
                    }
                },500);
            }
            @Override
            public void dialogCancel() {
            }
        });
    }

    public void finishReading() {
        // link to activity's method
        ((ReaderActivity)getActivity()).finishReading();
    }
}

