package com.sarinregmi.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.NetworkImageView;
import com.sarinregmi.gallery.volley.VolleyManager;

import java.util.ArrayList;

import static com.sarinregmi.gallery.R.id.icon;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
    private ArrayList<String> mImageUrls = new ArrayList<String>();
    private final Context mContext;

    public GridAdapter(Context context) {
        mContext = context;
    }

    public void setImageUrls(ArrayList<String> urls) {
        mImageUrls = urls;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public NetworkImageView mImageView;

        public ViewHolder(View v) {
            super(v);
            mImageView = (NetworkImageView) v.findViewById(icon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mImageView.setImageUrl(mImageUrls.get(position), VolleyManager.getImageLoader(mContext));
    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }
}