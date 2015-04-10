package com.sarinregmi.gallery;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
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


public class GalleryActivity extends Activity {

    private static final String LOG_TAG = "GalleryActivity";
    private static final String IMAGE_SEARCH_API_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&imgsz=medium&start=";
    private static final int TOTAL_NUMBER_OF_ITEMS = 64; // API limit
    private static final int ITEMS_PER_REQUEST = 8; // API max limit
    private static final int NUMBER_OF_COLUMNS = 4;

    private SearchView mImageSearchView;
    private RecyclerView mPhotoGridView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private GridAdapter mGridAdapter;
    private int mNumberOfItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mImageSearchView = (SearchView) findViewById(R.id.searchView);
        mImageSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadImages(query);
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
    }

    private void loadImages(String query) {
        mNumberOfItems = 0;
        String url = IMAGE_SEARCH_API_URL;
        for (int i = 0; i < TOTAL_NUMBER_OF_ITEMS; i = i + ITEMS_PER_REQUEST) {
            try {
                url = url + i + "&q=" + URLEncoder.encode(query, "UTF-8");
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
                                String imgUrl = jsonObject.getString("url");
                                int height = jsonObject.getInt("height");
                                int width = jsonObject.getInt("width");
                                mGridAdapter.upsertImageObject(new ImageObject(imgUrl, height, width), mNumberOfItems++);
                            }
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Toast.makeText(GalleryActivity.this, "Unable to fetch data!", Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, e.getMessage());
                    }
                });
        VolleyManager.getRequestQueue(this).add(request);
    }

}
