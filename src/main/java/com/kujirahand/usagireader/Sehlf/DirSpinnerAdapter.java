package com.kujirahand.usagireader.Sehlf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kujirahand.usagireader.R;

import java.util.ArrayList;

/**
 * Created by kujira on 2016/04/22.
 */
public class DirSpinnerAdapter extends BaseAdapter{

        private Context mContext;
        private ArrayList<String> mDataList;

        public DirSpinnerAdapter(Context context) {
            super();
            mContext = context;
            mDataList = null;
        }

        public void setData(ArrayList<String> data) {
            mDataList = data;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.
                        inflate(R.layout.dir_spinner_selected_item, null);
            }
            String text = mDataList.get(position);
            TextView tv = (TextView)convertView.
                    findViewById(R.id.sample_selected_text_id);
            tv.setText(text);

            return convertView;
        }

        @Override
        public View getDropDownView(int position,
                                    View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.
                        inflate(R.layout.dir_spinner_item, null);
            }
            String text = mDataList.get(position);
            TextView tv = (TextView)convertView.
                    findViewById(R.id.sample_text_id);
            tv.setText(text);

            return convertView;
        }
}
