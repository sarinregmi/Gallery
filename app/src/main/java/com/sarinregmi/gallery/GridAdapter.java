package com.sarinregmi.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.NetworkImageView;
import com.sarinregmi.gallery.volley.VolleyManager;

import java.util.ArrayList;

import static com.sarinregmi.gallery.R.id.icon;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
    private ArrayList<ImageObject> mImageObjects;
    private final Context mContext;

    public GridAdapter(Context context) {
        mImageObjects = new ArrayList<ImageObject>();
        mContext = context;
    }

    public void setImageDataSet(ArrayList<ImageObject> data) {
        mImageObjects = data;
        notifyDataSetChanged();
    }

    public void addImageObject(ImageObject imageObject) {
        mImageObjects.add(imageObject);
        notifyItemInserted(getItemCount() - 1);
    }

    public void addImageDataSet(ArrayList<ImageObject> dataSet) {
        mImageObjects.addAll(dataSet);
        // TODO
    }

    public void upsertImageObject(ImageObject imageObject, int position) {
        if (getItemCount() <= position) {
            mImageObjects.add(imageObject);
            notifyItemInserted(position);
        } else {
            mImageObjects.set(position, imageObject);
            notifyItemChanged(position);
        }
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
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)holder.mImageView.getLayoutParams();
        final ImageObject item = mImageObjects.get(position);
        float ratio = item.getHeight()/item.getWidth();
        rlp.height = (int)(rlp.width * ratio);
        holder.mImageView.setLayoutParams(rlp);
        holder.mImageView.setImageUrl(mImageObjects.get(position).getUrl(), VolleyManager.getImageLoader(mContext));
    }

    @Override
    public int getItemCount() {
        return mImageObjects.size();
    }
}