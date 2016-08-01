package com.kujirahand.KEPUB.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.kujirahand.KEPUB.EPubFile;
import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.async.EPubWriterAsync;
import com.kujirahand.KEPUB.utils.EPubLinkArea;
import com.kujirahand.usagireader.R;
import com.kujirahand.utils.DialogCallback;
import com.kujirahand.utils.DialogHelper;

/**
 * Created by kujira on 2016/04/19.
 */
public class MainMenu {

    public static EPubView view;
    private static int which;

    public static String lang(int id) {
        return view.getContext().getString(id);
    }

    public static void showMain() {
        //todo: 戻るボタン
        String[] menuItems = new String[]{
                /*?*/ // lang(R.string.BACK),
                /*0*/ lang(R.string.TOC),
                /*1*/ lang(R.string.NEXT_CHAPTER),
                /*2*/ lang(R.string.GOTO_CHAPTER_TOP),
                /*3*/ lang(R.string.MOVE_TO_MYBOOKS),
                /*4*/ lang(R.string.SAVE_TO_EPUB),
                /*5*/ lang(R.string.MORE),
        };
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                switch (which) {
                    // case ?: view.viewPanel.backHistory(); break;
                    case 0: showLink("@TableOfContents"); break;
                    case 1: view.viewPanel.showChapterNext(true); break;
                    case 2: view.viewPanel.setScrollTopEx(0, true); break;
                    case 3: view.moveToMyBooks(view.getActivity(), view.epubFile.getEpubPath()); break;
                    case 4: saveAsEPUB(); break;
                    case 5: showMore(view); break;
                }
            }
        };
        // show dialog
        new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), R.style.mainMenuDialogStyle))
                .setItems(menuItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainMenu.which = which;
                        new Handler().postDelayed(run, 1);
                    }
                }).show();
    }

    public static void saveAsEPUB() {
        EPubWriterAsync writer = new EPubWriterAsync(view, view.epubFile);
        writer.execute();
    }

    public static void showThemeMenu() {
        String[] menuItems = new String[]{
                lang(R.string.FONT_SIZE),       //0
                lang(R.string.LINE_HEIGHT),     //1
                lang(R.string.CHANGE_COLOR),    //2

        };
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                switch (which) {
                    case 0: showFontSize(); break;
                    case 1: showLineHeight(); break;
                    case 2: changeColor(); break;
                }
            }
        };
        // showMain dialog
        new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), R.style.mainMenuDialogStyle))
                .setItems(menuItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainMenu.which = which;
                        new Handler().postDelayed(run, 1);
                    }
                }).show();
    }

    public static void showMore(final EPubView view) {
        MainMenu.view = view;
        String[] menuItems = new String[]{
                "Initialize Theme Setting", // 0
                "Reset This Book - remove all markers", // 1
                "Reset All Books - remove all markers", // 2
                // remove this book
        };
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                switch (which) {
                    case 0: view.initThemeSetting(); break;
                    case 1: view.removeAllMarker(); break;
                    case 2: view.removeCacheAllBooks(); break;
                }
            }
        };
        // show dialog
        new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), R.style.mainMenuDialogStyle))
                .setItems(menuItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainMenu.which = which;
                        new Handler().postDelayed(run, 1);
                    }
                }).show();
    }

    public static void showLink(String href) {
        view.viewPanel.showLinkPage(new EPubLinkArea(href), true);
    }

    public static void changeColor() {
        DialogHelper.parent = (Activity)view.getContext();
        DialogHelper.selectList(
                "", "",
                new String[]{"White", "Silver", "Black", "Green", "Brown"},
                new DialogCallback() {
                    @Override
                    public void dialogResult(Object which) {
                        String c = (String)which;
                        view.changeColorTheme(c);
                    }
                    @Override
                    public void dialogCancel() {
                    }
                });
    }

    public static void showFontSize() {
        DialogHelper.parent = (Activity)view.getContext();
        DialogHelper.seekbarDialog(
                lang(R.string.FONT_SIZE), "",
                6, 40, (int)view.getFontSizeDp(),
                new DialogCallback() {
                    @Override
                    public void dialogResult(Object which) {
                        view.changeFontSizeDp((int)which);
                    }
                    @Override
                    public void dialogCancel() {
                    }
                });
    }
    public static void showLineHeight() {
        DialogHelper.parent = (Activity)view.getContext();

        float rate = view.getLineHeightRate();
        int rateInt = (int)Math.floor(rate * 10);
        DialogHelper.seekbarDialog(
                lang(R.string.LINE_HEIGHT), "",
                12, 30, rateInt,
                new DialogCallback() {
                    @Override
                    public void dialogResult(Object which) {
                        float rate = ((int)which) / 10.0f;
                        view.changeLineHeightRate(rate);
                    }
                    @Override
                    public void dialogCancel() {
                    }
                });
    }
}
