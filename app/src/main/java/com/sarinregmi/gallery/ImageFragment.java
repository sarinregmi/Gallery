package com.sarinregmi.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ImageFragment extends Fragment implements View.OnClickListener {

    private static final String IMAGE_URL_KEY = "url";
    private static final String LOG_TAG       = "ImageFragment";
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
            mImageUrl = getArguments().getString(IMAGE_URL_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.image_fragment, container, false);
        ImageView imageView = (ImageView) root.findViewById(R.id.image_view);
        root.findViewById(R.id.close).setOnClickListener(this);
        root.findViewById(R.id.share).setOnClickListener(this);

        Picasso.with(getActivity()).load(mImageUrl).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                Log.w(LOG_TAG, "Error loading file " + mImageUrl);
            }
        });
        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                getActivity().finish();
                getActivity().overridePendingTransition(0, R.anim.slide_to_bottom);
                break;

            case R.id.share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, mImageUrl);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_prompt)));
                break;

            default:
                break;
        }
    }
}
