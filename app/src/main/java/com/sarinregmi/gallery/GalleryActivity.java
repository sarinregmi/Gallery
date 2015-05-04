package com.sarinregmi.gallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sarinregmi.gallery.volley.VolleyManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GalleryActivity extends Activity implements View.OnClickListener, GridAdapter.OnDataLoadFinishListener{

    private static final String LOG_TAG              = "GalleryActivity";
    private static final String IMAGE_SEARCH_API_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&q=%s&start=%d&imgsz=large";
    private static final int TOTAL_NUMBER_OF_ITEMS   = 64; // API limit
    private static final int ITEMS_PER_REQUEST       = 8; // API max limit

    public static final int ANIMATION_DELAY          = 300;
    public static final String DATASET_KEY           = "data";
    public static final String POSITION_KEY          = "position";

    private ProgressBar mProgressBar;
    private TextView mStatusView;
    private SearchView mImageSearchView;
    private RecyclerView mPhotoGridView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private GridAdapter mGridAdapter;
    private Handler mHandler;

    private String mQuery;
    private int mPendingRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mHandler = new Handler();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mStatusView = (TextView) findViewById(R.id.status_prompt);
        setActionBar(toolbar);
        setUpSearchView();
        setUpRecyclerView();
    }

    private void setUpSearchView() {
        mImageSearchView = (SearchView) findViewById(R.id.searchView);
        mImageSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            String query = "";

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty() && !query.equals(this.query)) {
                    mQuery = query;
                    loadImages();
                }
                hideKeyBoard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                this.query = newText;
                if (!newText.isEmpty()) {
                    mQuery = newText;
                    loadImages();
                    return true;
                }
                return false;
            }
        });
    }

    private void setUpRecyclerView() {
        int numberOfColumns = GridViewMetrics.getNumberOfColumns(this);
        mGridAdapter = new GridAdapter(this, mHandler);
        mGridAdapter.setOnClickListener(this);
        mGridAdapter.setOnDataLoadFinishListener(this);
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(numberOfColumns, StaggeredGridLayoutManager.VERTICAL);
        mGridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mPhotoGridView = (RecyclerView) findViewById(R.id.image_recycler_view);
        mPhotoGridView.setLayoutManager(mGridLayoutManager);
        mPhotoGridView.setAdapter(mGridAdapter);
        mPhotoGridView.setHasFixedSize(true);

        mPhotoGridView.getItemAnimator().setAddDuration(ANIMATION_DELAY);
        mPhotoGridView.getItemAnimator().setMoveDuration(ANIMATION_DELAY);
        mPhotoGridView.getItemAnimator().setChangeDuration(ANIMATION_DELAY);
        mPhotoGridView.getItemAnimator().setRemoveDuration(0);
    }

    private void loadImages() {
        VolleyManager.getRequestQueue(this).cancelAll(this);
        mHandler.removeCallbacksAndMessages(null);
        mGridAdapter.reInitialize();
        mPendingRequests = 0;
        mProgressBar.setVisibility(View.VISIBLE);
        mStatusView.setVisibility(View.VISIBLE);
        updateStatusMessage(String.format(getString(R.string.search_prompt), mQuery));
        for (int i = 0; i < TOTAL_NUMBER_OF_ITEMS; i = i + ITEMS_PER_REQUEST) {
            try {
                String url = String.format(IMAGE_SEARCH_API_URL, URLEncoder.encode(mQuery, "UTF-8"), i);
                sendRequest(url);
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void sendRequest(String url) {
        mPendingRequests ++;
        JsonObjectRequest imageRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject json) {
                    try {
                        JSONObject responseData = json.getJSONObject("responseData");
                        JSONArray results = responseData.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject jsonObject = results.getJSONObject(i);
                            final String imgUrl = jsonObject.getString("url");
                            final int height = jsonObject.getInt("height");
                            final int width = jsonObject.getInt("width");
                            mGridAdapter.addImageObject(new ImageObject(imgUrl, height, width));
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        updateStatusMessage(getString(R.string.server_error));
                        e.printStackTrace();
                    }
                    mPendingRequests--;
                    if(mPendingRequests == 0) {
                        mGridAdapter.sync();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    Toast.makeText(GalleryActivity.this,getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    updateStatusMessage(getString(R.string.network_error));
                }
            });
        imageRequest.setTag(this);
        VolleyManager.getRequestQueue(this).add(imageRequest);
    }

    @Override
    public void onClick(View v) {
        int itemPosition = mPhotoGridView.getChildPosition(v);
        Intent intent = new Intent(this, PhotoViewerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putParcelableArrayListExtra(DATASET_KEY, mGridAdapter.getImageDataSet());
        intent.putExtra(POSITION_KEY, itemPosition);
        startActivity(intent);
    }

    private void hideKeyBoard() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(mImageSearchView.getWindowToken(), 0);
                }
            }
        }, ANIMATION_DELAY);
    }

    private void updateStatusMessage(final String message) {
        mStatusView.animate()
            .alpha(0.0f)
            .setDuration(ANIMATION_DELAY)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mStatusView.setText(message);
                    mStatusView.animate().setListener(null);
                    mStatusView.animate().alpha(1.0f);
                }
            });
    }

    @Override
    public void onDataLoadFinished() {
        mProgressBar.setVisibility(View.INVISIBLE);
        updateStatusMessage(String.format(getString(R.string.results_prompt), mQuery));
        hideKeyBoard();
    }
}
