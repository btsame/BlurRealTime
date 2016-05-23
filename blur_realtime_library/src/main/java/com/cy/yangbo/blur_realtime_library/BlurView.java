package com.cy.yangbo.blur_realtime_library;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2016/5/20.
 */
public class BlurView extends GLSurfaceView{

    private Renderer mRenderer;

    private TextureSquare mSquare;

    public BlurView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);
        mRenderer = new MyRender();
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
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
