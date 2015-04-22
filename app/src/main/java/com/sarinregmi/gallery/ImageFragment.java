package com.sarinregmi.gallery;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.NetworkImageView;
import com.sarinregmi.gallery.volley.VolleyManager;

public class ImageFragment extends Fragment {

    private static final String IMAGE_URL_KEY = "url";
    private String mImageUrl;

    public static ImageFragment newInstance(String imageUrl) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URL_KEY, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public ImageFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageUrl = getArguments().getString(mImageUrl);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.image_fragment, container, false);
        NetworkImageView imageView = (NetworkImageView) root.findViewById(R.id.image_view);
        imageView.setImageUrl(mImageUrl, VolleyManager.getImageLoader(getActivity()));
        return root;
    }

}
