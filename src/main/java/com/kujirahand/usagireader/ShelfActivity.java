package com.kujirahand.usagireader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.PathConfig;
import com.kujirahand.usagireader.Sehlf.BookItem;
import com.kujirahand.usagireader.Sehlf.DirSpinnerAdapter;
import com.kujirahand.usagireader.Sehlf.BookItemAdapter;
import com.kujirahand.utils.DialogCallback;
import com.kujirahand.utils.DialogHelper;
import com.kujirahand.utils.EnumFiles;
import com.kujirahand.utils.KFile;
import com.kujirahand.utils.KPref;

import java.io.File;
import java.io.IOError;
import java.util.ArrayList;
import java.util.Locale;

// TODO: ダウンロードからうまく起動しない
// TODO: ブラウザを開く画面
// TODO: ファイルが多すぎるとき、どうするのか？
// TODO: ファイルが見づらい

public class ShelfActivity extends Activity {

    GridView book_grid = null;
    Spinner dir_spin = null;
    ImageButton menu_button = null;
    ImageButton rename_btn = null;

    boolean loaded = false;
    private float DENSITY;
    public static ShelfActivity instance;
    public ArrayList<BookItem> bookItems;
    private DirSpinnerAdapter dir_adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelf);
        // set context
        KPref.init(this);
        PathConfig.context = getApplicationContext();
        DENSITY = getResources().getDisplayMetrics().density;
        instance = this;
        DialogHelper.parent = ShelfActivity.instance;
        //

        // first time?
        boolean firstTime = false;
        int hello = KPref.getInt("Hello.Usagi", -1);
        if (hello < 0) { // first time
            KPref.setInt("Hello.Usagi", 1);
            firstTimeProc();
            firstTime = true;
        }

        if (!firstTime) {
            // AdView mAdView = (AdView) findViewById(R.id.adView);
            // AdRequest adRequest = new AdRequest.Builder().build();
            // mAdView.loadAd(adRequest);
        }

        rename_btn = (ImageButton) findViewById(R.id.rename_btn);


        // set GUI items
        // Spinner dir
        ArrayList<String>dir_items = new ArrayList<String>();
        dir_adapter = new DirSpinnerAdapter(this);
        dir_adapter.setData(dir_items);
        dir_spin = (Spinner) findViewById(R.id.dirSpin);
        dir_spin.setAdapter(dir_adapter);
        setShelfName();

        // Book Items
        book_grid = (GridView)findViewById(R.id.flist);
        bookItems = new ArrayList<BookItem>();


        // Set Adapter
        BookItemAdapter adapter = new BookItemAdapter(this, book_grid.getId(), bookItems);
        adapter.sortFileName();
        book_grid.setAdapter(adapter);
        book_grid.invalidateViews();
        setEvent();

        menu_button = (ImageButton)findViewById(R.id.menu_button);
        setMenuEvent();

        //
        // last time spin index?
        int dir_index = KPref.getInt("dir.index", 0);
        dir_spin.setSelection(dir_index);
        changeDirItem(dir_index);
    }

    private void setShelfName() {
        ArrayList<String> dir_items = new ArrayList<String>();
        dir_items.add(DialogHelper.lang(R.string.DOWNLOAD));
        dir_items.add("MyBooks All");
        for (int i = 0; i < 5; i++) {
            String name = KPref.getStr("MyBooks.name." + i, "MyBooks " + (i+1));
            dir_items.add(name);
        }
        dir_adapter.setData(dir_items);
        dir_adapter.notifyDataSetChanged();
    }


    private void firstTimeProc() {
        Log.d("Shelf", "firstTimeProc");
        /*
        try {
            // Assets内のファイルストリームを開く
            AssetManager assetManager = getResources().getAssets();
            InputStream input = assetManager.open("ReadMe.epub");
            // 書き込み先のストリームを開く
            String path = PathConfig.getMyBooksDir() + "/ReadMe.epub";
            FileOutputStream output = new FileOutputStream(new File(path));

            // データをコピー
            byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            // ストリームを閉じる
            output.close();
            input.close();
            assetManager.close();
        } catch (Exception e) {
        }
        */
    }

    public void openBook(BookItem item) {
        Log.d("Usagi", item.getFileName());
        File f = item.getFile();
        if (f == null) return;
        showEpubFile(f.getAbsolutePath());
    }
    public void openBookContextMenu(final BookItem item) {
        if (item == null) return;
        final ShelfActivity self = this;
        Log.d("Usagi", item.getFileName());
        File f = item.getFile();
        final String path = f.getAbsolutePath();
        // 以下は必要
        DialogHelper.parent = ShelfActivity.instance;
        final String[] items = new String[]{
                DialogHelper.lang(R.string.OPEN),
                DialogHelper.lang(R.string.MOVE_TO_MYBOOKS),
                DialogHelper.lang(R.string.REMOVE),
        };
        DialogHelper.selectListNo("", "", items, new DialogCallback() {
            @Override
            public void dialogResult(Object which) {
                int whichNo = (int)which;
                switch (whichNo) {
                    case 0: // open
                        openBook(item);
                        break;
                    case 1: // move
                        EPubView.moveToMyBooks(self, path);
                        break;
                    case 2: // remove
                        String cpath = PathConfig.getCacheDir(path);
                        try {
                            KFile.deleteRecursive(new File(cpath));
                            KFile.deleteRecursive(new File(path));
                            Toast.makeText(ShelfActivity.instance, "OK!", Toast.LENGTH_LONG);
                            refreshBooks();
                        } catch (IOError e) {
                            Toast.makeText(ShelfActivity.instance, "Sorry, failed to remove.", Toast.LENGTH_LONG);
                        }
                        break;
                }
            }
            @Override
            public void dialogCancel() {
            }
        });
    }

    private void setEvent() {
        // click event
        dir_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeDirItem(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        book_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookItem s = bookItems.get(position);
                openBook(s);
            }
        });
        book_grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                BookItem s = bookItems.get(position);
                openBookContextMenu(s);
                return true;
            }
        });
        rename_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = dir_spin.getSelectedItemPosition();
                if (i < 2) return;
                final int si = i - 2;
                String defName = KPref.getStr("MyBooks.name." + si, "MyBooks " + (si + 1));
                DialogHelper.memoDialog(DialogHelper.lang(R.string.INPUT_MYBOOKS_NAME), defName, new DialogCallback() {
                    @Override
                    public void dialogResult(Object which) {
                        String name = (String)which;
                        if (name.equals("")) return;
                        KPref.setStr("MyBooks.name." + si, name);
                        setShelfName();
                    }
                    @Override
                    public void dialogCancel() {

                    }
                }
                );
            }
        });
    }

    private void setMenuEvent() {
        final ShelfActivity self = this;
        DialogHelper.parent = self;
        // menu_button
        final String menuItems[] = new String[] {
                DialogHelper.lang(R.string.DOWNLOAD_FROM_WEB),
                DialogHelper.lang(R.string.OPEN_WEB),
        };
        menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.parent = self;
                final Locale locale = Locale.getDefault();
                Log.d("lang", locale.toString());

                DialogHelper.selectList("", "", menuItems, new DialogCallback() {
                    @Override
                    public void dialogResult(Object which) {
                        String w = (String)which;
                        if (w.equals(menuItems[0])) {
                            Uri uri = Uri.parse("http://kujirahand.com/blog/index.php?EPUB%252FDownload&simple");
                            if (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE)) {
                                uri = Uri.parse("http://kujirahand.com/blog/index.php?EPUB%252FDownload.ja&simple");
                            }
                            Intent i = new Intent(getApplicationContext(), WebActivity.class);
                            i.setAction(Intent.ACTION_VIEW);
                            i.setData(uri);
                            startActivity(i);
                            // Downloadに切り替える
                            dir_spin.setSelection(1);
                        }
                        if (w.equals(menuItems[1])) {
                            Uri uri = Uri.parse("http://google.com/");
                            Intent i = new Intent(Intent.ACTION_VIEW,uri);
                            startActivity(i);
                            dir_spin.setSelection(1);
                        }
                    }
                    @Override
                    public void dialogCancel() {
                    }
                });
            }
        });
    }
    public void refreshBooks() {
        int i = dir_spin.getSelectedItemPosition();
        changeDirItem(i);
    }

    public void refreshGridAdapterOnly() {
        BookItemAdapter adapter = (BookItemAdapter)book_grid.getAdapter();
        adapter.sortFileName();
        adapter.notifyDataSetChanged();
    }

    public void showEpubFile(String path) {
        try {
            Intent intent = new Intent(getApplication(), ReaderActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(new File(path));
            intent.setDataAndType(uri, "application/epub+zip");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Usagi", "path=" + path);
        loaded = true;
    }

    public void enumAllBooks() {
        bookItems.clear();
        ArrayList<BookItem> items = new ArrayList<BookItem>();
        /*
        File mybooks_dir = new File(PathConfig.getMyBooksDir());
        ArrayList<BookItem> items = EnumFiles.enumEpub(mybooks_dir);

        File dropbox_dir = new File(PathConfig.getDropboxDir());
        EnumFiles.enumEpubAll(dropbox_dir, items);

        // Download directory
        File download_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        ArrayList<BookItem> sia = new ArrayList<BookItem>();
        EnumFiles.enumEpubAll(download_dir, items);
        */
        /*
        // 全部取得するのは危険ー時間かかりすぎる
        File adir = Environment.getExternalStorageDirectory();
        EnumFiles.enumEpubAll(adir, items);
        */
        for (BookItem book : items) {
            bookItems.add(book);
        }
        Log.d("Shelf", "items=" + bookItems.size());
    }
    public void enumDownloads() {
        // Download directory
        // DOWNLOAD
        File da = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        ArrayList<BookItem> sia = EnumFiles.enumEpub(da);
        // + DROPBOX (for mothoer)
        File dropbox_dir = new File(PathConfig.getDropboxDir());
        EnumFiles.enumEpubAll(dropbox_dir, sia);
        // + MoonReader
        // File moon_dir = new File(PathConfig.getMoonBooksDir());
        // EnumFiles.enumEpubAll(moon_dir, sia);

        bookItems.clear();
        for (BookItem book : sia) {
            bookItems.add(book);
        }
        Log.d("Shelf", "items=" + bookItems.size());
    }
    public void enumMyBooks(int no) {
        // Download directory
        File my_books = new File(PathConfig.getMyBooksDir(no));
        ArrayList<BookItem> sib = new ArrayList<BookItem>();
        EnumFiles.enumEpubAll(my_books, sib);
        // Reset
        bookItems.clear();
        for (BookItem book : sib) {
            bookItems.add(book);
        }
        Log.d("Shelf", "items=" + bookItems.size());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // continue?
        String path = EPubView.getContinueFile(getApplicationContext());
        if (path != null) {
            showEpubFile(path);
        }

        // check size
        Display display = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        if (p.x > p.y) {
            book_grid.setNumColumns(3);
        } else {
            book_grid.setNumColumns(1);
        }

        // showMain files
        int i = dir_spin.getSelectedItemPosition();
        changeDirItem(i);
    }


    private void changeDirItem(int position) {
        Log.d("Shelf", "changeDirItem=" + position);
        switch (position) {
            //case ?: enumAllBooks(); break;
            case 0: enumDownloads(); break;
            case 1: enumMyBooks(0); break;
            case 2: enumMyBooks(1); break;
            case 3: enumMyBooks(2); break;
            case 4: enumMyBooks(3); break;
            case 5: enumMyBooks(4); break;
            case 6: enumMyBooks(5); break;
        }
        rename_btn.setVisibility((position >= 2) ? View.VISIBLE : View.INVISIBLE);
        refreshGridAdapterOnly();
        KPref.setInt("dir.index", position);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public int dp2px(float dip) {
        return (int)Math.round(dip * DENSITY);
    }

}
