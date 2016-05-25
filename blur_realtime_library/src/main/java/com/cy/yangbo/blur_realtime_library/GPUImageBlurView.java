package com.cy.yangbo.blur_realtime_library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBoxBlurFilter;

/**
 * Created by Administrator on 2016/5/25.
 */
public class GPUImageBlurView extends ImageView {
    private static final String TAG = BlurView.class.getSimpleName();

    private View mBackgroundView;

    private float x, y, width, height;
    private float belowViewX, belowViewY, belowViewWidth, belowViewHeight;

    public GPUImageBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        setBackgroundColor(Color.BLACK);
    }

    public void setBackgroundView(@NonNull View view) {
        mBackgroundView = view;

        if (view instanceof ListView) {
            changeOverlay();
            ((ListView) view).setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    changeOverlay();
                }
            });
        } else if (view instanceof ScrollView) {
            changeOverlay();
            ((ScrollView) view).setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        changeOverlay();
                    }
                    return false;
                }
            });

        } else if (view instanceof RecyclerView) {
            ((RecyclerView) view).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if(newState == RecyclerView.SCROLL_STATE_IDLE){
                        //changeOverlay();
                        asyncChangeOverlay();
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
//                    changeOverlay();
                    asyncChangeOverlay();
                }
            });
        } else {
            changeOverlay();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int[] location = new int[2];
        getLocationOnScreen(location);
        x = location[0];
        y = location[1];
        width = getWidth();
        height = getHeight();
    }

    private void asyncChangeOverlay(){
        Log.d(TAG, "OnPreDrawListener:" + "x:" + x + " y:" + y);
        int[] location = new int[2];
        mBackgroundView.getLocationOnScreen(location);
        belowViewX = location[0];
        belowViewY = location[1];
        belowViewWidth = mBackgroundView.getWidth();
        belowViewHeight = mBackgroundView.getHeight();

        if(width <= 0 || height <= 0) return;
        mBackgroundView.buildDrawingCache();
        Bitmap bmp = mBackgroundView.getDrawingCache();
        Bitmap targetBmp = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBmp);

        int srcRectX = (int) (x - belowViewX >= 0 ? x - belowViewX : 0);
        int srcRectY = (int) (y - belowViewY >= 0 ? y - belowViewY : 0);
        int srcRectWidth = (int) (width > belowViewWidth ? belowViewWidth : width);
        int srcRectHeight = (int) (height > belowViewHeight ? belowViewHeight : height);

//        int srcRectWidth = (int) (belowViewWidth);
//        int srcRectHeight = (int) (belowViewHeight);
        Log.d(TAG, "OnPreDrawListener:" + "srcRectX:" + srcRectX + " srcRectY:" + srcRectY);
        Log.d(TAG, "OnPreDrawListener:" + "srcRectWidth:" + srcRectWidth + " srcRectHeight:" + srcRectHeight);
        canvas.drawBitmap(bmp, new Rect(srcRectX, srcRectY, srcRectX + srcRectWidth, srcRectY + srcRectHeight),
                new Rect(0, 0, (int) width, (int) height), new Paint());
                    bmp.recycle();
       final GPUImage gpuImage = new GPUImage(getContext());
        new AsyncTask<Bitmap, Void, Bitmap>(){

            @Override
            protected Bitmap doInBackground(Bitmap... params) {
       /* GPUImageGaussianBlurFilter gpuImageFilter = new GPUImageGaussianBlurFilter();
        gpuImageFilter.setBlurSize(10.0f);
        gpuImage.setFilter(gpuImageFilter);*/
                Bitmap originBmp = params[0];
                GPUImageBoxBlurFilter gpuImageFilter1 = new GPUImageBoxBlurFilter();
                gpuImageFilter1.setBlurSize(5.0f);
                gpuImage.setFilter(gpuImageFilter1);
                originBmp = gpuImage.getBitmapWithFilterApplied(originBmp);

        /*GPUImageLevelsFilter gpuImageFilter2 = new GPUImageLevelsFilter();
        gpuImageFilter2.setMin(0.0f, 3.0f, 5.0f);
        gpuImage.setFilter(gpuImageFilter2);*/



                return originBmp;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                setImageBitmap(bitmap);
            }
        }.execute(targetBmp);
    }

    private void changeOverlay() {
        Log.d(TAG, "OnPreDrawListener:" + "x:" + x + " y:" + y);
        int[] location = new int[2];
        mBackgroundView.getLocationOnScreen(location);
        belowViewX = location[0];
        belowViewY = location[1];
        belowViewWidth = mBackgroundView.getWidth();
        belowViewHeight = mBackgroundView.getHeight();

        if(width <= 0 || height <= 0) return;
        mBackgroundView.buildDrawingCache();
        Bitmap bmp = mBackgroundView.getDrawingCache();
        Bitmap targetBmp = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBmp);

        int srcRectX = (int) (x - belowViewX >= 0 ? x - belowViewX : 0);
        int srcRectY = (int) (y - belowViewY >= 0 ? y - belowViewY : 0);
        int srcRectWidth = (int) (width > belowViewWidth ? belowViewWidth : width);
        int srcRectHeight = (int) (height > belowViewHeight ? belowViewHeight : height);

//        int srcRectWidth = (int) (belowViewWidth);
//        int srcRectHeight = (int) (belowViewHeight);
        Log.d(TAG, "OnPreDrawListener:" + "srcRectX:" + srcRectX + " srcRectY:" + srcRectY);
        Log.d(TAG, "OnPreDrawListener:" + "srcRectWidth:" + srcRectWidth + " srcRectHeight:" + srcRectHeight);
        canvas.drawBitmap(bmp, new Rect(srcRectX, srcRectY, srcRectX + srcRectWidth, srcRectY + srcRectHeight),
                new Rect(0, 0, (int) width, (int) height), new Paint());
//                    bmp.recycle();
        GPUImage gpuImage = new GPUImage(getContext());
       /* GPUImageGaussianBlurFilter gpuImageFilter = new GPUImageGaussianBlurFilter();
        gpuImageFilter.setBlurSize(10.0f);
        gpuImage.setFilter(gpuImageFilter);*/

        GPUImageBoxBlurFilter gpuImageFilter1 = new GPUImageBoxBlurFilter();
        gpuImageFilter1.setBlurSize(5.0f);
        gpuImage.setFilter(gpuImageFilter1);
        targetBmp = gpuImage.getBitmapWithFilterApplied(targetBmp);

        /*GPUImageLevelsFilter gpuImageFilter2 = new GPUImageLevelsFilter();
        gpuImageFilter2.setMin(0.0f, 3.0f, 5.0f);
        gpuImage.setFilter(gpuImageFilter2);*/

        targetBmp = gpuImage.getBitmapWithFilterApplied(targetBmp);

        setImageBitmap(targetBmp);
    }
}
