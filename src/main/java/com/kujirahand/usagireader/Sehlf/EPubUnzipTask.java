package com.kujirahand.usagireader.Sehlf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.kujirahand.KXml.KXmlNode;
import com.kujirahand.KXml.KXmlParser;
import com.kujirahand.utils.ZipUtil;

/**
 * Created by kujira on 2016/05/07.
 */
public class EPubUnzipTask extends AsyncTask<Integer,Integer,Integer> {
    private ImageView imageView;
    private Context context;
    private String tag;
    private BookItem item;

    public EPubUnzipTask(BookItem item, ImageView imageView, Context context) {
        this.imageView = imageView;
        this.context = context;
        this.item = item;
        this.tag = imageView.getTag().toString();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        try {
            String epub_path = item.getPath();
            String container_s = ZipUtil.extractString(
                    epub_path,
                    "META-INF/container.xml");
            if (container_s == null) return null;
            KXmlNode container_x = KXmlParser.parseString(container_s);
            if (container_x == null) return null;
            String rootFile = container_x.getTagAttrValue("rootfile", "full-path");
            if (rootFile == null) return null;

            String root_s = ZipUtil.extractString(epub_path, rootFile);
            if (root_s == null) return null;
            KXmlNode root_x = KXmlParser.parseString(root_s);
            if (root_x == null) return null;

            KXmlNode meta = root_x.getByTagAndAttr("meta", "name", "cover");
            if (meta == null) return null;
            String image_content = meta.getAttrValue("content");
            if (image_content == null) return null;
            Log.d("EPubUnzipTask", image_content);
            KXmlNode item_x = root_x.getByTagAndAttr("item", "id", image_content);
            if (item_x == null) return null;
            String image_path = item_x.getAttrValue("href");
            if (image_path == null) return null;
            Log.d("EPubUnzipTask", epub_path + "/" + image_path);

            byte[] bytes = ZipUtil.extractBytes(epub_path, image_path);
            if (bytes == null) return null;
            Log.d("EPubUnzipTask", "bytes=" + bytes.length);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            return null;
        } catch (Exception e) {
            Log.d("EPubUnzipTask", "error:" + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result != null) {
            if (imageView == null) {
                Log.d("EPubUnzipTask", "imageView is null");
                return;
            }
            if (tag == null) {
                Log.d("EPubUnzipTask", "tag is null");
                return;
            }
            if (this.tag.equals(this.imageView.getTag())) {
                // imageView.setImageBitmap(result);
            }
        }
    }
}
