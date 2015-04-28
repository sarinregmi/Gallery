package com.sarinregmi.gallery;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class ImageObject implements Parcelable{
    private String mURL;
    private int mHeight;
    private int mWidth;

    public String getUrl() {
        return mURL;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private ImageObject(Parcel in) {
        mURL = in.readString();
        mHeight = in.readInt();
        mWidth = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mURL);
        out.writeInt(mHeight);
        out.writeInt(mWidth);
    }

    public static final Parcelable.Creator<ImageObject> CREATOR = new Parcelable.Creator<ImageObject>() {
        public ImageObject createFromParcel(Parcel in) {
            return new ImageObject(in);
        }

        public ImageObject[] newArray(int size) {
            return new ImageObject[size];
        }
    };


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageObject that = (ImageObject) o;
        return Objects.equals(mHeight, that.mHeight) &&
                Objects.equals(mWidth, that.mWidth) &&
                Objects.equals(mURL, that.mURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mURL, mHeight, mWidth);
    }

    public ImageObject(String url, int height, int width) {
        this.mURL = url;
        this.mHeight = height;
        this.mWidth = width;

    }
}
