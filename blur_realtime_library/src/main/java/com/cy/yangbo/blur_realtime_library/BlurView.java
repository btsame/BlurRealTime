package com.cy.yangbo.blur_realtime_library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2016/5/20.
 */
public class BlurView extends GLSurfaceView{

    private static final String TAG = BlurView.class.getSimpleName();

    private Renderer mRenderer;

    private TextureSquare mSquare;

    private View mBackgroundView;
    private ImageView mShowOriginView;
    private float x, y, width, height;

    public BlurView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);
        mRenderer = new MyRender();
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d(TAG, "OnPreDrawListener:" + "x:" + x + " y:" + y);
               /* try {
                    final Bitmap bmp1 = BitmapFactory.decodeStream(getContext().getAssets().open("mm.jpg"));
                    if(mShowOriginView != null){
                        mShowOriginView.setImageBitmap(bmp1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                if(mSquare != null){
                    mBackgroundView.buildDrawingCache();
                    Bitmap bmp = mBackgroundView.getDrawingCache();
                    final Bitmap targetBmp = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(targetBmp);
                    canvas.drawBitmap(bmp, new Rect((int)x, (int)y, (int)(x + width), (int)(y + height)),
                            new Rect(0, 0, (int)width, (int)height), new Paint());
//                    bmp.recycle();
                    if(mShowOriginView != null){
                        mShowOriginView.setImageBitmap(bmp);
                    }
                    try {
                       final Bitmap bmp1 = BitmapFactory.decodeStream(getContext().getAssets().open("mm.jpg"));

                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
//                            mSquare.changeTexBitmap(targetBmp);
                                mSquare.changeTexBitmap(bmp1);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                return true;
            }
        });
    }

    public void setBackgroundView(View view){
        mBackgroundView = view;
    }

    public void setShowOriginView(ImageView view){
        mShowOriginView = view;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            x = getX();
            y = getY();
            width = getWidth();
            height = getHeight();
        }else{
            x = left;
            y = top;
            width = right - left;
            height = bottom - top;
        }
    }

    public class MyRender implements Renderer{

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            MatrixState.setCamere(0.0f, 0.0f, -0.5f,
                    0.0f, 0.0f, -5.0f,
                    0.0f, 1.0f, 0.0f);
            mSquare = new TextureSquare(BlurView.this);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            float ratio = (float)width / height;

            MatrixState.setProjectFrustum(-ratio, ratio, -1.0f, 1.0f, 3.0f, 10.0f);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            mSquare.drawSelf();
        }
    }
}
