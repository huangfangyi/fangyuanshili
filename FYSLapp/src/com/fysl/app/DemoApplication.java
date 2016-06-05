/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fysl.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.SDKInitializer;
import com.fysl.app.main.utils.UserUtils;
import com.hyphenate.EMCallBack;

public class DemoApplication extends Application {

	public static Context applicationContext;
	private static DemoApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";

	/**
	 * 当前用户nickname,为了苹果推送不是userid而是昵称
	 */
	public static String currentUserNick = "";
	private JSONObject userJson;
	private String lat = "0";
	private String lng = "0";
	// 是否显示所有人
	private String isAllP="1";
	// 是否显示所有动态
	private String isAllT;

	@Override
	public void onCreate() {
		MultiDex.install(this);
		super.onCreate();
		applicationContext = this;
		instance = this;

		// init demo helper
		DemoHelper.getInstance().init(applicationContext);
		SDKInitializer.initialize(instance);
		getUserInfo();
	}

	public void setAllP(String isAllP) {
		this.isAllP = isAllP;
		UserUtils.getInstance(instance).setUserInfo("isAllP", isAllP);

	}

	public String getAllP() {
		if (isAllP == null) {

			isAllP = UserUtils.getInstance(instance).getUserInfo("isAllP");
		}

		return isAllP;
	}
	
	public void setAllT(String isAllT) {
		this.isAllT = isAllT;
		UserUtils.getInstance(instance).setUserInfo("isAllT", isAllT);

	}

	public String getAllT() {
		if (isAllT == null) {

			isAllT = UserUtils.getInstance(instance).getUserInfo("isAllT");
		}

		return isAllT;
	}
    
	public void setLat(String lat) {
		this.lat = lat;

		UserUtils.getInstance(instance).setUserInfo("lat", lat);

	}

	public String getLat() {
		if (lat.equals("0")) {
			lat = UserUtils.getInstance(instance).getUserInfo("lat");

		}
		if (TextUtils.isEmpty(lat)) {

			lat = "0";
		}
		return lat;

	}

	public void setLng(String lng) {
		this.lng = lng;

		UserUtils.getInstance(instance).setUserInfo("lng", lng);

	}

	public String getLng() {
		if (lng.equals("0")) {
			lng = UserUtils.getInstance(instance).getUserInfo("lng");

		}
		if (TextUtils.isEmpty(lng)) {

			lng = "0";
		}
		return lng;

	}

	public static DemoApplication getInstance() {
		return instance;
	}

	public void savaUserInfo(JSONObject userJson) {
		this.userJson = userJson;
		// 保存到PRE
		UserUtils.getInstance(instance).setUserInfo("userJson",
				userJson.toJSONString());

	}

	public JSONObject getUserInfo() {
		if (userJson == null) {
			String userJsonStr = UserUtils.getInstance(instance).getUserInfo(
					"userJson");
			if (!TextUtils.isEmpty(userJsonStr)) {

				userJson = JSONObject.parseObject(userJsonStr);
			}
		}

		return userJson;

	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
}
