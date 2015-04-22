package com.sarinregmi.gallery;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
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

public class GalleryActivity extends Activity {

    private static final String LOG_TAG = "GalleryActivity";
    private static final String IMAGE_SEARCH_API_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&imgsz=medium&start=";
    private static final int TOTAL_NUMBER_OF_ITEMS = 64; // API limit
    private static final int ITEMS_PER_REQUEST = 8; // API max limit
    private static final int NUMBER_OF_COLUMNS = 5;

    private ProgressBar mProgressBar;
    private SearchView mImageSearchView;
    private TextView mPromptView;
    private RecyclerView mPhotoGridView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private GridAdapter mGridAdapter;

    private int mNumberOfItems;
    private String mQuery;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mHandler = new Handler();

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mPromptView = (TextView) findViewById(R.id.result_prompt);
        mImageSearchView = (SearchView) findViewById(R.id.searchView);
        mImageSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery = query;
                loadImages();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mImageSearchView.getWindowToken(), 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mGridAdapter = new GridAdapter(this);
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(NUMBER_OF_COLUMNS, StaggeredGridLayoutManager.VERTICAL);
        mPhotoGridView = (RecyclerView) findViewById(R.id.image_recycler_view);
        mPhotoGridView.setLayoutManager(mStaggeredGridLayoutManager);
        mPhotoGridView.setAdapter(mGridAdapter);
        mPhotoGridView.getItemAnimator().setAddDuration(2000);
        mPhotoGridView.getItemAnimator().setMoveDuration(0);
        mPhotoGridView.getItemAnimator().setChangeDuration(2000);
        mPhotoGridView.getItemAnimator().setRemoveDuration(10000);
    }

    private void loadImages() {
        mNumberOfItems = 0;
        mPromptView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        updatePromptMessage(String.format(getString(R.string.search_prompt), mQuery));
        String url = IMAGE_SEARCH_API_URL;
        for (int i = 0; i < TOTAL_NUMBER_OF_ITEMS; i = i + ITEMS_PER_REQUEST) {
            try {
                url = url + i + "&q=" + URLEncoder.encode(mQuery, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            sendRequest(url);
        }

    }

    private void sendRequest(String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
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
                            int timeDelay = 2000 * (i + 1);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mGridAdapter.upsertImageObject(new ImageObject(imgUrl, height, width), mNumberOfItems++);
                                    Log.v("TACO", "Item upserted at " + mNumberOfItems);
                                    updatePromptMessage(String.format(getString(R.string.results_prompt), mQuery));
                                }
                            }, timeDelay);
                        }
                        mProgressBar.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    Toast.makeText(GalleryActivity.this, "Unable to fetch data!", Toast.LENGTH_SHORT).show();
                    updatePromptMessage(getString(R.string.error_prompt));
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        VolleyManager.getRequestQueue(this).add(request);
    }

    private void updatePromptMessage(final String message) {
        mPromptView.animate().setListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPromptView.setText(message);
                mPromptView.animate().alpha(1.0f);
                mPromptView.animate().setListener(null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mPromptView.animate().alpha(1.0f);
            }
        });
        mPromptView.animate().alpha(0.0f);
    }

}
