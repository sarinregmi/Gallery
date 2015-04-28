package com.sarinregmi.gallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
    private ArrayList<ImageObject> mImageObjects;
    private final Context mContext;
    private OnClickListener mOnClickListener;
    private int mCounter;
    private Set<ImageObject> mErrorItems;
    private boolean mIsCleaned;

    public GridAdapter(Context context) {
        mImageObjects = new ArrayList<ImageObject>();
        mErrorItems = new HashSet<ImageObject>();
        mContext = context;
        mCounter = 0;
        mIsCleaned = false;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void addImageObject(ImageObject imageObject) {
        mImageObjects.add(imageObject);
        notifyItemInserted(getItemCount() - 1);
    }

    public void upsertImageObject(ImageObject imageObject) {
        int position = mCounter++;
        if (getItemCount() <= position) {
            mImageObjects.add(imageObject);
            notifyItemInserted(getItemCount() - 1);
            Log.v("TACO", "Item inserted at " + (getItemCount() - 1));
        } else {
            mImageObjects.set(position, imageObject);
            notifyItemChanged(position);
            Log.v("TACO", "Item replaced at " + position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;

        public ViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.icon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        v.setOnClickListener(mOnClickListener);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ImageObject item = mImageObjects.get(position);

        Picasso.with(mContext).load(mImageObjects.get(position).getUrl())
                .resize(300, 300)
                .centerCrop().into(holder.mImageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                if (mErrorItems.add(item)) {
                    Log.v("TACO" , "Error item at " + mImageObjects.indexOf(item));
                }
                if(mIsCleaned) {
                    removeStrayItems();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageObjects.size();
    }

    public ArrayList<ImageObject> getImageDataSet() {
        return mImageObjects;
    }

    public void removeStrayItems() {
        for (ImageObject errorItem : mErrorItems) {
            int position = mImageObjects.indexOf(errorItem);
            if (position != -1) {
                mImageObjects.remove(errorItem);
                notifyItemRemoved(position);
                Log.v("TACO", "Removing Items at " + position);
            }
            mIsCleaned = true;
        }
    }

    public void reInitialize() {
        mCounter = 0;
        mErrorItems.clear();
        mIsCleaned = false;
    }
}