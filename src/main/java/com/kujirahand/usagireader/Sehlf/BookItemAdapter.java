package com.kujirahand.usagireader.Sehlf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kujirahand.KEPUB.PathConfig;
import com.kujirahand.KXml.KXmlNode;
import com.kujirahand.KXml.KXmlParser;
import com.kujirahand.usagireader.R;
import com.kujirahand.usagireader.ShelfActivity;
import com.kujirahand.utils.ZipUtil;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kujira on 2016/04/02.
 */
public class BookItemAdapter extends ArrayAdapter<BookItem> {

    private int resourceId;
    private LayoutInflater inflater;
    private Paint paint = new Paint();

    private ShelfActivity activity = null;

    public BookItemAdapter(Context context, int resourceId, List<BookItem> items) {
        super(context, resourceId, items);
        this.activity = (ShelfActivity)context;
        this.resourceId = resourceId;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
    }

    public void setItems(List<BookItem> newItems) {
        this.clear();
        this.addAll(newItems);
        this.notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Log.d("Shelf", "getView:position=" + position);

        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = this.inflater.inflate(R.layout.book_listview_item, null);
        }

        final BookItem item = this.getItem(position);

        // テキストをセット
        TextView text_bookTitle = (TextView)view.findViewById(R.id.item_bookTitle);
        text_bookTitle.setText(item.getBookTitle());
        TextView text_fileName = (TextView)view.findViewById(R.id.item_fileName);
        text_fileName.setText(item.getFileName());
        TextView text_dir = (TextView)view.findViewById(R.id.item_dir);
        text_dir.setText(item.getDir());
        ImageButton misc_btn = (ImageButton)view.findViewById(R.id.misc_btn);

        // アイコンをセット
        ImageView image_view = (ImageView)view.findViewById(R.id.item_image);
        boolean b = loadIcon(image_view, item);
        if (!b) {
            image_view.setImageResource(R.mipmap.book);
            /*
            image_view.setTag(item.getPath());
            EPubUnzipTask task = new EPubUnzipTask(image_view, getContext());
            task.execute(item);
            */
        }
        image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openBook(item);
            }
        });
        misc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openBookContextMenu(item);
            }
        });
        text_fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openBook(item);
            }
        });

        return view;
    }

    private boolean loadIcon(ImageView image_view, BookItem item) {
        // Check cache Image
        String cover = PathConfig.getCoverImage(item.getPath());
        File fc = new File(cover);
        if (fc.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(cover);
            image_view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image_view.setImageBitmap(bmp);
            return true;
        }

        return false;
    }

    public void sortFileName() {
        this.sort(new Comparator<BookItem>() {
            @Override
            public int compare(BookItem lhs, BookItem rhs) {
                String l_title = lhs.getBookTitle();
                String r_title = rhs.getBookTitle();
                int b = r_title.compareTo(l_title);
                if (b == 0) {
                    String lf = lhs.getFileName();
                    String rf = rhs.getFileName();
                    return rf.compareTo(lf);
                }
                return b;
            }
        });
    }
}
