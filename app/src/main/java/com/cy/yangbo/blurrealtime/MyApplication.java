package com.cy.yangbo.blurrealtime;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Administrator on 2016/5/25.
 */
public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
