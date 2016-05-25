package com.cy.yangbo.blur_realtime_library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Administrator on 2016/5/17.
 */
public class TextureSquare {

    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_DATA_SIZE = 3;
    private static final int TEXTURE_DATA_SIZE = 2;

    private GLSurfaceView mGLSurfaceView;
    private FloatBuffer mCoordBuffer;
    private FloatBuffer mTextureBuffer;

    int vertexShaderHandle, fragmentShaderHandle, programHandle;
    int mMVPMatrixHandle, mPositionHandle, mTextureHandle,
            ufResolutionHandle, ufBlurRadiusHandle, ufSampleNumHandle;

    int imageWidth, imageHeight;

    private Bitmap mTexBitmap;


    public TextureSquare(GLSurfaceView glSurfaceView){
        this.mGLSurfaceView = glSurfaceView;

        mTexBitmap = readStandardTexture("mm.jpg");
        initVertexData();
        initShader();
        initTexture();
    }

    public void changeTexBitmap(Bitmap bitmap){
        mTexBitmap = bitmap;
        initTexture();
        mGLSurfaceView.requestRender();
    }

    public void initVertexData(){
        mCoordBuffer = ByteBuffer.allocateDirect(cubePosition.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCoordBuffer.put(cubePosition).position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(cubeTexture.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureBuffer.put(cubeTexture).position(0);
    }

    public void initShader(){
        vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        if(vertexShaderHandle != 0){
            GLES20.glShaderSource(vertexShaderHandle,
                    readAssetShaderStr("vertexShader_horizontal.gl", mGLSurfaceView.getContext()));
            GLES20.glCompileShader(vertexShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if(compileStatus[0] == 0){
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if(vertexShaderHandle == 0){
            throw new RuntimeException("failed to create vertex shader!");
        }

        fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if(fragmentShaderHandle != 0){
            GLES20.glShaderSource(fragmentShaderHandle,
                    readAssetShaderStr("fragmentShader_horizontal.gl", mGLSurfaceView.getContext()));
            GLES20.glCompileShader(fragmentShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if(compileStatus[0] == 0){
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if(fragmentShaderHandle == 0){
            throw new RuntimeException("failed to create fragment shader!");
        }

        programHandle = GLES20.glCreateProgram();
        if(programHandle != 0){
            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            GLES20.glLinkProgram(programHandle);

            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if(linkStatus[0] == 0){
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if(programHandle == 0){
            throw new RuntimeException("failed to create program!");
        }


    }

    private String readAssetShaderStr(String shaderName, Context context){
        StringBuilder shaderSB = new StringBuilder();

        InputStream is = null;
        try {
            is = context.getAssets().open(shaderName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while((line = br.readLine()) != null){
                shaderSB.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return shaderSB.toString();
    }


    public void drawSelf(){
        GLES20.glUseProgram(programHandle);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition");
        mTextureHandle = GLES20.glGetAttribLocation(programHandle, "aTexCoor");
        ufResolutionHandle = GLES20.glGetUniformLocation(programHandle, "resolution");
        ufBlurRadiusHandle = GLES20.glGetUniformLocation(programHandle, "blurRadius");
        ufSampleNumHandle = GLES20.glGetUniformLocation(programHandle, "sampleNum");

        MatrixState.setInitStack();
        MatrixState.translate(0.0f, 0.0f, -5.0f);

        mCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false,
                0, mCoordBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureHandle, TEXTURE_DATA_SIZE, GLES20.GL_FLOAT, false,
                0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(mTextureHandle);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        GLES20.glUniform1f(ufBlurRadiusHandle, 60.0f);
        GLES20.glUniform1f(ufSampleNumHandle, 2.0f);
        GLES20.glUniform2f(ufResolutionHandle, imageWidth, imageHeight);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private int textureId;
    private void initTexture(){
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        if(textures[0] == -1){
            throw new RuntimeException("failed to generate texture!");
        }

        textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        imageWidth = mTexBitmap.getWidth();
        imageHeight = mTexBitmap.getHeight();
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mTexBitmap, 0);
        mTexBitmap.recycle();
    }

    private Bitmap readStandardTexture(String imageName){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            is = mGLSurfaceView.getContext().getAssets().open(imageName);
            bitmap = BitmapFactory.decodeStream(is, null, options);
            if(options.outWidth % 2 != 0 || options.outHeight % 2 != 0){
                options.outWidth = options.outWidth - options.outWidth % 2;
                options.outHeight = options.outHeight - options.outHeight % 2;
            }
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(is, null, options);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    final float cubePosition[] =
            {
                    -1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f
            };

    final float[] cubeTexture =
            {
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f
            };

}

