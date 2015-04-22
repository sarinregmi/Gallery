package com.sarinregmi.gallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

public class PhotoViewerActivity extends FragmentActivity {

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PhotoPagerAdapter(getSupportFragmentManager(), null);
        mPager.setAdapter(mPagerAdapter);
        // get item position from the starting intent, as well the arraylist of objests to feed to adapter
        mPager.setCurrentItem(0);
    }

    private class PhotoPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<ImageObject> mImageObjects;

        public PhotoPagerAdapter(FragmentManager fm, ArrayList<ImageObject> imageObjects) {
            super(fm);
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
