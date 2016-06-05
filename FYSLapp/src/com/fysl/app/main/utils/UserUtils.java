package com.fysl.app.main.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserUtils {

    /**
     * 保存Preference的name
     */
    public static final String PREFERENCE_NAME = "local_userinfo";
    private static SharedPreferences mSharedPreferences;
    private static UserUtils mPreferenceUtils;
    private static SharedPreferences.Editor editor;

    private UserUtils(Context cxt) {
        mSharedPreferences = cxt.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
    }

    /**
     * 单例模式，获取instance实例
     * 
     * @param cxt
     * @return
     */
    public static UserUtils getInstance(Context cxt) {
        if (mPreferenceUtils == null) {
            mPreferenceUtils = new UserUtils(cxt);
        }
        editor = mSharedPreferences.edit();
        return mPreferenceUtils;
    }

    //
    public void setUserInfo(String str_name, String str_value) {

        editor.putString(str_name, str_value);
        editor.commit();
    }

    public String getUserInfo(String str_name) {

        return mSharedPreferences.getString(str_name, "");

    }

}
