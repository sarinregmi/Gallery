package com.sarinregmi.gallery;

public class ImageObject {
    private String mURL;
    private int mHeight;
    private int mWidth;

    public ImageObject(String url, int height, int width) {
        this.mURL = url;
        this.mHeight = height;
        this.mWidth = width;
    }

    public String getUrl() {
        return mURL;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }
}
