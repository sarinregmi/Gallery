package com.sarinregmi.gallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

import java.util.ArrayList;

public class PhotoViewerActivity extends FragmentActivity {

    private ViewPager mPager;
    private FragmentStatePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        getActionBar().hide();

        int position = getIntent().getIntExtra(GalleryActivity.POSITION_KEY, 0);
        ArrayList<ImageObject> data = getIntent().getParcelableArrayListExtra(GalleryActivity.DATASET_KEY);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PhotoPagerAdapter(getSupportFragmentManager(), data);
        mPager.setAdapter(mPagerAdapter);

        mPager.setPageTransformer(true, new PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                if (position < 0 || position >= 1.f) {
                    page.setTranslationX(0);
                    page.setAlpha(1.f);
                    page.setScaleX(1);
                    page.setScaleY(1);
                } else {
                    page.setTranslationX(-position * page.getWidth());
                    page.setAlpha(Math.max(0, 1.f - position));
                    final float scale = Math.max(0, 1.f - position * 0.3f);
                    page.setScaleX(scale);
                    page.setScaleY(scale);
                }
            }
        });
        mPager.setCurrentItem(position);
    }

    private class PhotoPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<ImageObject> mImageObjects;

        public PhotoPagerAdapter(FragmentManager fm, ArrayList<ImageObject> imageObjects) {
            super(fm);
            mImageObjects = imageObjects;
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(mImageObjects.get(position).getUrl());
        }

        @Override
        public int getCount() {
            return mImageObjects.size();
        }
    }
}
