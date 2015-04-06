package com.sarinregmi.gallery.volley;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;

public class VolleyManager
{
	private static final int MEMORY_CACHE_SIZE_BYTES = (int) (Runtime.getRuntime().maxMemory() / 4);
	private static final int DISK_CACHE_SIZE_BYTES   = 512 * 1024 * 1024;
	private static final String DISK_CACHE_DIRECTORY = "volley";

	private static RequestQueue sRequestQueue;
	private static ImageLoader sImageLoader;

	public static synchronized RequestQueue getRequestQueue(Context context)
	{
		if (sRequestQueue == null)
		{
			File cacheDir = new File(context.getCacheDir(), DISK_CACHE_DIRECTORY);
			Cache cache = new DiskBasedCache(cacheDir, DISK_CACHE_SIZE_BYTES);
			Network network = new BasicNetwork(new HurlStack());
			sRequestQueue = new RequestQueue(cache, network);
			sRequestQueue.start();
		}
		return sRequestQueue;
	}

	public static synchronized ImageLoader getImageLoader(Context context)
	{
		if (sImageLoader == null)
		{
			sImageLoader = new ImageLoader(getRequestQueue(context), new BitmapLruCache(MEMORY_CACHE_SIZE_BYTES));
		}
		return sImageLoader;
	}
}
