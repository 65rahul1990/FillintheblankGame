package com.example.fillintheblankgame;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class WikipediaBlankGameApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Muli-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
