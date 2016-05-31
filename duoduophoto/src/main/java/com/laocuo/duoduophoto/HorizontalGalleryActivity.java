/*
 *  * Copyright (C) 2015-2016 The Android Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */

package com.laocuo.duoduophoto;

import java.util.ArrayList;

import com.laocuo.duoduophoto.outsrcview.CustomViewPager;
import com.laocuo.duoduophoto.outsrcview.CustomViewPager.OnPageChangeListener;
import com.laocuo.duoduophoto.util.ImageCache;
import com.laocuo.duoduophoto.util.ImageResizer;
import com.laocuo.duoduophoto.util.Utils;
import com.laocuo.duoduophoto.util.ImageCache.ImageCacheParams;
import com.laocuo.duoduophoto.util.ImageWorker.ImageWorkerAdapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;

public class HorizontalGalleryActivity extends Activity implements OnPageChangeListener {
    public static final String TAG = "zhaocheng";
    private static final int REQUEST_CODE_GET_DIRECTORY = 1;
    private CustomViewPager mHroizontalListView;
    private HorizontalAdapter mHorizontalAdapter;
    private ArrayList<String> mPathList;
    private String duoduo_photo_path;
    private int mImageWidth, mImageHeight;
    private int screen_width,screen_height;
    private int mActionBarHeight = -1;
    private ImageResizer mImageWorker;
    private static final String IMAGE_CACHE_DIR = "thumbs";
    private int mImageCount;
    private final int IMAGE_PAGE_COUNT = 4;
    private Matrix mMatrix_s, mMatrix_e;
    private float SCALE_Y = 4f;
    private float SCALE_X = 16f;
    private boolean mIsOutTheScreen = false;
    private ArrayList<ImageView> mImageViewList = new ArrayList<ImageView>();
    private ProgressBar mProgressBar;
    public static final String DUODUO_PHOTO_PATH     = "pref_key_photo_path";
    private SharedPreferences mPrefs;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_photo);
        empty = (TextView)findViewById(R.id.hroizontal_list_empty);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mMatrix_s = new Matrix();
        mMatrix_e = new Matrix();
        allocateImageView();
        mProgressBar = (ProgressBar)findViewById(R.id.hroizontal_list_progressbar);
        mHroizontalListView = (CustomViewPager)findViewById(R.id.hroizontal_list);
        mHroizontalListView.setOnPageChangeListener(this);
        duoduo_photo_path = mPrefs.getString(DUODUO_PHOTO_PATH, Utils.getDefaultPhotoPath());
        if (duoduo_photo_path != null) {
            Log.d(TAG, "duoduo_photo_path:"+duoduo_photo_path);
            mPathList = Utils.getAllPhotoPaths(duoduo_photo_path);
            mImageCount = mPathList.size();
        } else {
            Log.d(TAG, "duoduo_photo_path:null");
        }
        getScreenSize();
        allocateImageCache(screen_width, screen_height-mActionBarHeight);
        mHroizontalListView.post(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                setImageCacheSize();
                mProgressBar.setVisibility(View.GONE);
                if (mImageCount > 0) {
                    mHorizontalAdapter = new HorizontalAdapter();
                    mHroizontalListView.setAdapter(mHorizontalAdapter);
                } else {
                    mHroizontalListView.setAdapter(null);
                    empty.setVisibility(View.VISIBLE);
                }
            }});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo, menu);
//        menu.findItem(R.id.action_settings).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent("com.laocuo.duoduophoto.GETDIECTORY");
            startActivityForResult(intent, REQUEST_CODE_GET_DIRECTORY);
            return true;
        } else if (id == R.id.action_clear) {
            releaseImageCache();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_CODE_GET_DIRECTORY) {
            if (resultCode == 1 && data != null) { //REQUEST_OK:0
                Bundle b = data.getExtras();
                resetPhotoDirectory(b.getString("savePath"));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void resetPhotoDirectory(String reset_dir) {
        // TODO Auto-generated method stub
        Log.d(TAG, "reset_dir:"+reset_dir);
        if (reset_dir.equalsIgnoreCase(duoduo_photo_path)) {
            return;
        }
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putString(DUODUO_PHOTO_PATH, reset_dir);
        prefsEditor.apply();
        duoduo_photo_path = reset_dir;
        mHroizontalListView.setAdapter(null);
        mHorizontalAdapter = null;
        if (mPathList != null) {
            mPathList.clear();
            mImageCount = 0;
        }
        if (duoduo_photo_path != null) {
            mPathList = Utils.getAllPhotoPaths(duoduo_photo_path);
            mImageCount = mPathList.size();
        }
        releaseImageCache();
        if (mImageCount > 0) {
            empty.setVisibility(View.GONE);
            mHorizontalAdapter = new HorizontalAdapter();
            mHroizontalListView.setAdapter(mHorizontalAdapter);
        } else {
            empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
//        mImageWorker.getLoadingBitmap().recycle();
        super.onDestroy();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    private void setImageCacheSize() {
        // TODO Auto-generated method stub
        mImageWidth = mHroizontalListView.getWidth();
        mImageHeight = mHroizontalListView.getHeight();
        Log.d(TAG, "mImageWidth=" + mImageWidth + " mImageHeight=" + mImageHeight);
        mImageWorker.setImageSize(mImageWidth, mImageHeight);
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo);
        Bitmap resizeB = Bitmap.createScaledBitmap(b, mImageWidth, mImageHeight, false);
        b.recycle();
        mImageWorker.setLoadingImage(resizeB);
    }

    private void getScreenSize() {
        // TODO Auto-generated method stub
        DisplayMetrics dm = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screen_width = dm.widthPixels;
        screen_height = dm.heightPixels;
        // Calculate ActionBar height
        if (mActionBarHeight < 0) {
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(
                    android.R.attr.actionBarSize, tv, true)) {
                mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, getResources().getDisplayMetrics());
            } else {
                // No ActionBar style (pre-Honeycomb or ActionBar not in theme)
                mActionBarHeight = 0;
            }
        }
    }

    private class HorizontalAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mImageCount;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // TODO Auto-generated method stub
            ImageView v = mImageViewList.get(position%IMAGE_PAGE_COUNT);
            mImageWorker.releaseImage(position, v);
            container.removeView(v);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // TODO Auto-generated method stub
            ImageView v = mImageViewList.get(position%IMAGE_PAGE_COUNT);
            mImageWorker.loadImage(position, v);
            container.addView(v);
            return v;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }
    }

    private void measureView(View child) {
        android.view.ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
        }
//        android.view.ViewGroup.LayoutParams parentParams = mHroizontalListView.getLayoutParams();

/*        int parentWidth = MeasureSpec.makeMeasureSpec(mImageWidth, MeasureSpec.EXACTLY);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(parentWidth, 0 + 0, params.width);
        int parentHeight = MeasureSpec.makeMeasureSpec(mImageHeight, MeasureSpec.EXACTLY);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(parentHeight, 0 + 0, params.height);*/
        int childWidthSpec = MeasureSpec.makeMeasureSpec(mImageWidth, MeasureSpec.EXACTLY);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(mImageHeight, MeasureSpec.EXACTLY);
        child.measure(childWidthSpec, childHeightSpec);
    }

    private void allocateImageCache(int image_width, int image_height) {
        // TODO Auto-generated method stub
        ImageCacheParams cacheParams = new ImageCacheParams(IMAGE_CACHE_DIR);

        // Allocate a third of the per-app memory limit to the bitmap memory cache.
        // This value
        // should be chosen carefully based on a number of factors. Refer to the
        // corresponding
        // Android Training class for more discussion:
        // http://developer.android.com/training/displaying-bitmaps/
        // In this case, we aren't using memory for much else other than this
        // activity and the
        // ImageDetailActivity so a third lets us keep all our sample image
        // thumbnails in memory
        // at once.
        Log.d(TAG, "Utils.getMemoryClass(this)="+Utils.getMemoryClass(this));
        cacheParams.memCacheSize = 1024 * 1024 * Utils
            .getMemoryClass(this) / 3;
        cacheParams.diskCacheSize = cacheParams.memCacheSize;
        // The ImageWorker takes care of loading images into our ImageView children
        // asynchronously
        mImageWorker = new ImageResizer(this, image_width, image_height);
        mImageWorker.setAdapter(mImageThumbWorkerUrlsAdapter);
        mImageWorker.setImageFadeIn(false);
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(
                this, cacheParams));
    }

    public void releaseImageCache() {
        final ImageCache cache = mImageWorker.getImageCache();
        if (cache != null) {
          mImageWorker.getImageCache().clearCaches();
          // DiskLruCache.clearCache(getActivity(), ImageFetcher.HTTP_CACHE_DIR);
          Toast.makeText(this, R.string.clear_cache_complete,
              Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Simple static adapter to use for image thumbnails.
     */
    private ImageWorkerAdapter mImageThumbWorkerUrlsAdapter = new ImageWorkerAdapter() {
      @Override
      public Object getItem(int num) {
        return mPathList.get(num);
      }

      @Override
      public int getSize() {
        return mPathList.size();
      }
    };

    private void doImageMatrix_E(int position, int positionOffsetPixels) {
        // TODO Auto-generated method stub
        ImageView iv = mImageViewList.get(position%IMAGE_PAGE_COUNT);
        float movePos = positionOffsetPixels;
        float mStepGapX = movePos * (SCALE_X + 1) / SCALE_X;
        float mStepGapY = movePos / SCALE_Y;
        float mHeight = 0;
        if (mIsOutTheScreen == true) {
            mHeight = (mStepGapY/2) * (1 - Math.abs(movePos-mImageWidth/2) / (mImageWidth/2));
        }
        float[] src = {
                0, 0,
                mImageWidth, 0,
                mImageWidth, mImageHeight,
                0, mImageHeight
        };

        float[] dst = {
                0, 0 - mHeight,
                mImageWidth - mStepGapX, 0 + mStepGapY,
                mImageWidth - mStepGapX, mImageHeight - mStepGapY,
                0, mImageHeight + mHeight
        };
        mMatrix_e.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
        iv.setImageMatrix(mMatrix_e);
    }

    private void doImageMatrix_S(int position, int positionOffsetPixels) {
        // TODO Auto-generated method stub
        ImageView iv = mImageViewList.get(position%IMAGE_PAGE_COUNT);
        float movePos = positionOffsetPixels;
        float mStepGapX = movePos * (SCALE_X + 1) / SCALE_X;
        float mStepGapY = movePos / SCALE_Y;
        float mHeight = 0;
        if (mIsOutTheScreen == true) {
            mHeight = (mStepGapY/2) * (1 - Math.abs(movePos-mImageWidth/2) / (mImageWidth/2));
        }
        float[] src = {
                0, 0,
                mImageWidth, 0,
                mImageWidth, mImageHeight,
                0, mImageHeight
        };

        float[] dst = {
                0 + mStepGapX, 0 + mStepGapY,
                mImageWidth, 0 - mHeight,
                mImageWidth, mImageHeight + mHeight,
                0 + mStepGapX, mImageHeight - mStepGapY
        };
        mMatrix_s.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
        iv.setImageMatrix(mMatrix_s);
    }

    private void allocateImageView() {
        // TODO Auto-generated method stub
        mImageViewList.clear();
        for(int i=0;i<IMAGE_PAGE_COUNT;i++) {
            ImageView iv = new ImageView(this);
            iv.setScaleType(ScaleType.MATRIX);
            measureView(iv);
            LayoutParams dd = new LayoutParams(iv.getMeasuredWidth(), iv.getMeasuredHeight());
            iv.setLayoutParams(dd);
            mImageViewList.add(iv);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // TODO Auto-generated method stub
//        Log.d(TAG, "position="+position+" positionOffset="+positionOffset+ "positionOffsetPixels="+positionOffsetPixels);
        if (mImageCount > 1) {
            if (positionOffsetPixels > 0 && positionOffsetPixels < mImageWidth) {
                doImageMatrix_S(position, positionOffsetPixels);
                doImageMatrix_E(position+1, mImageWidth-positionOffsetPixels);
            } else if (positionOffsetPixels == 0) {
                if (mImageCount > position) {
                    mImageViewList.get(position%IMAGE_PAGE_COUNT).setImageMatrix(null);
                }
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // TODO Auto-generated method stub
        
    }
}
