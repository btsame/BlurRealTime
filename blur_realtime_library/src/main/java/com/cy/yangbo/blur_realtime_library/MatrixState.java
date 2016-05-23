package com.cy.yangbo.blur_realtime_library;

import android.opengl.Matrix;

/**
 * Created by Administrator on 2016/5/17.
 */
public class MatrixState {

    private static float[] mProjMatrix = new float[16]; //投影所用(正交或透视)
    private static float[] mVMatrix = new float[16];    //摄像机朝向矩阵
    private static float[] mMVPMatrix;  //坐标转换矩阵

    private static float[] currMatrix; //当前变换矩阵
    private static float[][] changeStack = new float[10][16]; //用于保存变化矩阵的栈
    private static int stackTop = -1;

    public static void setInitStack(){
        currMatrix = new float[16];
//        Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
        Matrix.setIdentityM(currMatrix, 0);
    }

    public static void pushMatrix(){
        stackTop++;
        for(int i = 0; i < 16; i++){
            changeStack[stackTop][i] = currMatrix[i];
        }
    }

    public static void popMatrix(){
        for(int i = 0; i < 16; i++){
            currMatrix[i] = changeStack[stackTop][i];
        }
        stackTop--;
    }

    public static void translate(float x, float y, float z){
        Matrix.translateM(currMatrix, 0, x, y, z);
    }

    public static void rotate(float angle, float x, float y, float z){
        Matrix.rotateM(currMatrix, 0, angle, x, y, z);
    }

    public static void scale(float sx, float sy, float sz){
        Matrix.scaleM(currMatrix, 0, sx, sy, sz);
    }

    public static void setCamere(float cx, float cy, float cz,  //摄像机位置x、y、z坐标
                                 float tx, float ty, float tz,  //观察目标点坐标
                                 float upx, float upy, float upz){  //up向量在x、y、z方向的分量
        Matrix.setLookAtM(mVMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);
    }

    public static void setProjectOrtho(float left, float right, float bottom, float top,
                                       float near, float far){   //设置正交投影矩阵
        Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public static void setProjectFrustum(float left, float right, float bottom, float top, //设置透视投影矩阵
                                         float near, float far){
        Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
    }

    public static float[] getFinalMatrix(){
        mMVPMatrix = new float[16];
        //Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);//将摄像机矩阵乘以变换矩阵

        Matrix.multiplyMM(mMVPMatrix, 0, currMatrix, 0, mVMatrix, 0);//将摄像机矩阵乘以变换矩阵

        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);//将投影矩阵乘以上一步结果得到最终矩阵

        return mMVPMatrix;
    }
}
