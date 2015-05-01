package com.sarinregmi.gallery;

import android.content.Context;
import android.os.Handler;
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
    private static final String LOG_TAG = "GridAdapater";
    private final Context mContext;
    private ArrayList<ImageObject> mImageObjects;
    private ArrayList<ImageObject> mItemsToAdd;
    private Set<ImageObject> mErrorItems;
    private OnClickListener mOnClickListener;
    private boolean mIsCleaned;
    private Handler mHandler;
    private int mImageWidth;

    public GridAdapter(Context context, Handler handler) {
        mImageObjects = new ArrayList<ImageObject>();
        mItemsToAdd = new ArrayList<>();
        mErrorItems = new HashSet<ImageObject>();

        mContext = context;
        mHandler = handler;
        mImageWidth = GridViewMetrics.getImageWidth(mContext);
        mIsCleaned = false;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void addImageObject(ImageObject imageObject) {
        if (!mImageObjects.contains(imageObject)) {
            mItemsToAdd.add(imageObject);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;

        public ViewHolder(View v, int imageWidth) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.icon);
            mImageView.getLayoutParams().height = imageWidth;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        v.setOnClickListener(mOnClickListener);
        ViewHolder vh = new ViewHolder(v, mImageWidth);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ImageObject item = mImageObjects.get(position);

        Picasso.with(mContext).load(mImageObjects.get(position).getUrl())
                .resize(mImageWidth, mImageWidth)
                .placeholder(R.drawable.default_image_background)
                .centerCrop().into(holder.mImageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                if (mErrorItems.add(item)) {
                    Log.v(LOG_TAG, "Error item at " + mImageObjects.indexOf(item));
                }
                if (mIsCleaned) {
                    removeErrorItems();
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

    public void removeErrorItems() {
        Log.v(LOG_TAG, "Removing error items " + mErrorItems.size());
        for (ImageObject errorItem : mErrorItems) {
            int position = mImageObjects.indexOf(errorItem);
            if (position != -1) {
                mImageObjects.remove(errorItem);
                notifyItemRemoved(position);
            }
        }
        mIsCleaned = true;
        mErrorItems.clear();
    }

    public void reInitialize() {
        mErrorItems.clear();
        mItemsToAdd.clear();
        mIsCleaned = false;
    }

    public void sync() {
        Log.v(LOG_TAG, "Items to add " + mItemsToAdd.size());
        int count = getItemCount();
        int animationDelay = GalleryActivity.ANIMATION_DELAY;

//        // Add new set of items for first time
//        if(count == 0) {
//            mImageObjects.addAll(mItemsToAdd);
//            notifyItemRangeInserted(count, mItemsToAdd.size());
//            mItemsToAdd.clear();
//        } else {
            for (int i = 0; i < getItemCount(); i++) {
                ImageObject item = mImageObjects.get(i);
                // Replace non matching ones
                if (!mItemsToAdd.contains(item)) {
                    if (mItemsToAdd.size() > 0) {
                        ImageObject itemToAdd = mItemsToAdd.remove(mItemsToAdd.size() - 1);
                        mImageObjects.set(i, itemToAdd);
                        final int position = i;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemChanged(position);
                            }
                        }, i * animationDelay);
                    } else {
                        // Remove stray items, old items count greater than new items
                        mImageObjects.remove(item);
                        final int position = i;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemRemoved(position);
                            }
                        }, i * animationDelay);
                        i--;
                    }
                } else {
                    mItemsToAdd.remove(item);
                }
            }
 //       }
        // add new remaining items, new items count greater than old items
        mHandler.postDelayed(new  Runnable() {
             @Override
             public void run() {
                 int index = getItemCount();
                 if (!mItemsToAdd.isEmpty()) {
                     mImageObjects.addAll(mItemsToAdd);
                     notifyItemRangeInserted(index, mItemsToAdd.size());
                     mItemsToAdd.clear();
                 }
                 removeErrorItems();
             }
        }, count * animationDelay);
    }
}