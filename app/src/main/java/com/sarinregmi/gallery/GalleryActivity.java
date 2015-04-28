package com.sarinregmi.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

// Implement image equal method and check for same image existence before replacing
// Implement ripple effect for button press

public class GalleryActivity extends Activity implements View.OnClickListener {

    private static final String LOG_TAG              = "GalleryActivity";
    private static final String IMAGE_SEARCH_API_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&q=%s&start=%d&imgsz=large";
    private static final int TOTAL_NUMBER_OF_ITEMS   = 64; // API limit
    private static final int ITEMS_PER_REQUEST       = 8; // API max limit
    private static final int ANIMATION_DELAY         = 1000;
    public static final String DATASET_KEY           = "data";
    public static final String POSITION_KEY          = "position";

    private ProgressBar mProgressBar;
    private SearchView mImageSearchView;
    private TextView mPromptView;
    private RecyclerView mPhotoGridView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private GridAdapter mGridAdapter;

    private String mQuery;
    private Handler mHandler;

    private int mPendingRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mHandler = new Handler();

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mPromptView = (TextView) findViewById(R.id.status_prompt);

        int numberOfColumns = getResources().getDisplayMetrics().widthPixels / 300;

        mGridAdapter = new GridAdapter(this);
        mGridAdapter.setOnClickListener(this);
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(numberOfColumns, StaggeredGridLayoutManager.VERTICAL);
        mGridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mPhotoGridView = (RecyclerView) findViewById(R.id.image_recycler_view);
        mPhotoGridView.setLayoutManager(mGridLayoutManager);
        //mPhotoGridView.setLayoutManager(mStaggeredGridLayoutManager);
        mPhotoGridView.setAdapter(mGridAdapter);
        mPhotoGridView.setHasFixedSize(true);

        mPhotoGridView.getItemAnimator().setAddDuration(ANIMATION_DELAY);
        mPhotoGridView.getItemAnimator().setMoveDuration(0);
        mPhotoGridView.getItemAnimator().setChangeDuration(ANIMATION_DELAY);
        mPhotoGridView.getItemAnimator().setRemoveDuration(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        mImageSearchView = (SearchView) searchItem.getActionView();

        mImageSearchView.setIconifiedByDefault(false);
        mImageSearchView.setQueryHint(getString(R.string.search_image));
        mImageSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    mQuery = query;
                    loadImages();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mImageSearchView.getWindowToken(), 0);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    mQuery = newText;
                    loadImages();
                    return true;
                }
                return false;
            }
        });
        return true;
    }

    private void loadImages() {
        VolleyManager.getRequestQueue(this).cancelAll(this);
        mHandler.removeCallbacksAndMessages(null);
        mGridAdapter.reInitialize();
        mPendingRequests = 0;
        mPromptView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
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
                        mPendingRequests--;
                        Log.v("TACO", "Request count is " + mPendingRequests);
                        try {
                            JSONObject responseData = json.getJSONObject("responseData");
                            JSONArray results = responseData.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject jsonObject = results.getJSONObject(i);
                                final String imgUrl = jsonObject.getString("url");
                                final int height = jsonObject.getInt("height");
                                final int width = jsonObject.getInt("width");
                                int timeDelay = ANIMATION_DELAY * (i + 1);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mGridAdapter.upsertImageObject(new ImageObject(imgUrl, height, width));
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }, timeDelay);
                            }
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.getMessage());
                            e.printStackTrace();
                            mProgressBar.setVisibility(View.GONE);
                        }
                        if(mPendingRequests == 0) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.v("TACO", "Removing stray items");
                                    mGridAdapter.removeStrayItems();
                                }
                            }, ANIMATION_DELAY * 8);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Toast.makeText(GalleryActivity.this, "Unable to fetch data!", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
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

}
