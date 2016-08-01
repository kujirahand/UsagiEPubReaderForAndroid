package com.kujirahand.usagireader;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;

import com.kujirahand.KEPUB.EPubView;
import com.kujirahand.KEPUB.PathConfig;
import com.kujirahand.KEPUB.utils.EPubLinkArea;
import com.kujirahand.utils.DialogHelper;

public class ReaderActivity extends Activity {

    public String filePath = "";
    private EPubView epubview;
    final String LOG_TAG = "Usagi.ReaderActivity";
    public static Activity instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set instance
        ReaderActivity.instance = this;
        EPubView.activity = this;
        DialogHelper.parent = this;
        //
        epubview = new EPubView(this);
        epubview.setFullScreen(true);
        setContentView(epubview);

        Log.d(LOG_TAG, "@onCreate");

        Intent intent = getIntent();
        String action = intent.getAction();
        if (!Intent.ACTION_VIEW.equals(action)) {
            Log.d(LOG_TAG, "Not set action VIEW.");
            return;
        }

        Uri uri = intent.getData();
        Log.d(LOG_TAG, "scheme=" + uri.getScheme());
        if (uri.getScheme().equals("content")) {
            filePath = getRealPathFromURI(intent, this);
        } else {
            filePath = uri.getPath();
        }
        Log.d(LOG_TAG, "UsagiPath=" + uri.getPath());


        // exit?
        if (filePath == null) {
            epubview.log("FilePath not set.");
            this.finish();
            return;
        }
        if (filePath.equals("")) {
            epubview.log("FilePath not set.");
            this.finish();
            return;
        }

        // continue?
        String path = EPubView.getContinueFile(getApplicationContext());
        if (path != null) {
            if (path.equals(filePath)) {
                epubview.log("(continue) ReaderActivity are waiting for onResume. file=" + filePath);
                return;
            }
        }

        // new file
        EPubView.setContinueFile(getApplicationContext(), filePath, null, 0, null);
        epubview.log("(new) ReaderActivity are waiting for onResume. file=" + filePath);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "@onPause");
        epubview.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // set Context
        PathConfig.context = this.getApplicationContext();
        DialogHelper.parent = this;

        Log.d(LOG_TAG, "@onResume");
        epubview.onResume();
        epubview.flagBackkeyPressed = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG,"@onSaveInstanceState()");
        epubview.onSaveInfo(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(LOG_TAG,"@onRestoreInstanceState");
        epubview.onRestoreInfo(savedInstanceState);
    }

    @Override
    public void onBackPressed(){
        epubview.log("onBackPressed...");
        // no history finish
        EPubLinkArea back = epubview.viewPanel.backHistory();
        if (back == null) {
            epubview.flagBackkeyPressed = true;
            EPubView.setContinueFile(getApplicationContext(), null, null, 0, null);
            super.onBackPressed();
            return;
        }
    }


    public static String getRealPathFromURI(Intent data, Activity activity) {
        Uri uri = data.getData();
        Log.d("PATH", "scheme=" + uri.getScheme());
        Log.d("PATH", "str="+uri.toString());
        Log.d("PATH", "auth=" + uri.getAuthority());
        /*
        if (Build.VERSION.SDK_INT >= 19) {
            String id = DocumentsContract.getDocumentId(data.getData());
            Uri docUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            return docUri.getPath();
        } else {
            return data.getData().getPath();
        }
        */
        String filename = null;
        Cursor cursor = null;
        try {
            cursor = EPubView.activity.getContentResolver().query(data.getData(), new String[] {
                    OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, null, null, null );
            if (cursor != null && cursor.moveToFirst()) {
                filename = cursor.getString(0);
                // filesize = cursor.getLong(1);
            }
            return filename;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
    public void finishReading() {
        epubview.log("FINISH!! ReaderActivity");
        epubview.flagBackkeyPressed = true;
        EPubView.setContinueFile(getApplicationContext(), null, null, 0, null);
        this.finish();
    }
}
