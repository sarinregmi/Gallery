package com.sarinregmi.gallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityOptions;
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

// Ripple effect not working on button press
// Sharedelement activity switch
// Toolbar impelementation for better UI

public class GalleryActivity extends Activity implements View.OnClickListener {

    private static final String LOG_TAG              = "GalleryActivity";
    private static final String IMAGE_SEARCH_API_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&q=%s&start=%d&imgsz=large";
    private static final int TOTAL_NUMBER_OF_ITEMS   = 64; // API limit
    private static final int ITEMS_PER_REQUEST       = 8; // API max limit

    public static final int ANIMATION_DELAY          = 300;
    public static final String DATASET_KEY           = "data";
    public static final String POSITION_KEY          = "position";

    private ProgressBar mProgressBar;
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
        int numberOfColumns = GridViewMetrics.getNumberOfColumns(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mGridAdapter = new GridAdapter(this, mHandler);
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
            String query = "";
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty() && !query.equals(this.query)) {
                    mQuery = query;
                    loadImages();
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mImageSearchView.getWindowToken(), 0);
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
        return true;
    }

    private void loadImages() {
        VolleyManager.getRequestQueue(this).cancelAll(this);
        mHandler.removeCallbacksAndMessages(null);
        mGridAdapter.reInitialize();
        mPendingRequests = 0;
        animateProgressBar(1.0f, View.VISIBLE, mProgressBar.getHeight());
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
                        e.printStackTrace();
                        animateProgressBar(0.0f, View.GONE, 0);
                        Toast.makeText(GalleryActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                    mPendingRequests--;
                    if(mPendingRequests == 0) {
                        mGridAdapter.sync();
                        animateProgressBar(0.0f, View.GONE, 0);
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    Toast.makeText(GalleryActivity.this,getString(R.string.network_error) , Toast.LENGTH_SHORT).show();
                    animateProgressBar(0.0f, View.GONE, 0);
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

        String transitionName = getString(R.string.transition_image);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, v, transitionName);
        startActivity(intent, options.toBundle());
    }

    private void animateProgressBar(float alpha, final int visibility, float translation) {
        mPhotoGridView.animate().translationY(translation).setDuration(500);
        mProgressBar.animate()
            .alpha(alpha)
            .setDuration(ANIMATION_DELAY)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mProgressBar.setVisibility(visibility);
                }
            });
    }
}
