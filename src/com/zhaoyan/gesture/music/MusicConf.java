package com.zhaoyan.gesture.music;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.zhaoyan.gesture.music.utils.SharedPreferencesCompat;

public class MusicConf {

	public static final String VISUALIZATION_TYPE="visualization_type";
	
	public static final String ZY_MUSIC_ACTION = "com.zhaoyan.music.aciton";
	
	public static String getStringPref(Context context, String name, String def) {
        SharedPreferences prefs =
            context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return prefs.getString(name, def);
    }
    
    public static void setStringPref(Context context, String name, String value) {
        SharedPreferences prefs =
            context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        Editor ed = prefs.edit();
        ed.putString(name, value);
        SharedPreferencesCompat.apply(ed);
    }
}
