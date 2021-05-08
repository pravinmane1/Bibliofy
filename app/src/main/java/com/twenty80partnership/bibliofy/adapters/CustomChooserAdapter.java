package com.twenty80partnership.bibliofy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.twenty80partnership.bibliofy.R;
import com.twenty80partnership.bibliofy.events.ListEvent;
import com.twenty80partnership.bibliofy.models.EvalInstalledAppInfo;

import java.util.ArrayList;


public class CustomChooserAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<EvalInstalledAppInfo> mData;
    private ListEvent mCallBack;

    public CustomChooserAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setDataAdapter(ArrayList<EvalInstalledAppInfo> mData, ListEvent mCallBack) {
        this.mData = mData;
        this.mCallBack = mCallBack;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        ViewHolder vh;
        if (view == null) {
            vh = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_chooser_row, null);

            vh.appImg = view.findViewById(R.id.appIcon);
            vh.appName = view.findViewById(R.id.appName);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        vh.appImg.setImageDrawable(mData.get(i).getAppIcon());
        vh.appName.setText(mData.get(i).getSimpleName());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallBack.onClick(i);
            }
        });
        return view;
    }

    static class ViewHolder {
        ImageView appImg;
        TextView appName;
    }
}
