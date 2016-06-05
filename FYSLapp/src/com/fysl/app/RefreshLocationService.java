package com.fysl.app;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.fysl.app.main.utils.Geohash;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RefreshLocationService extends Service {

	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor = "bd09ll";
	private final OkHttpClient client = new OkHttpClient();
	private boolean SAVE = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// 位置相关
		mLocationClient = new LocationClient(this); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
		InitLocation();
	}

	@Override
	public void onDestroy() {
		mLocationClient.stop();
		super.onDestroy();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SAVE = true;
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(this); // 声明LocationClient类
			mLocationClient.registerLocationListener(myListener); // 注册监听函数
			InitLocation();
		}
		mLocationClient.start();

		return super.onStartCommand(intent, flags, startId);

	}

	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			if (location != null && SAVE) {

				String lat = String.valueOf(location.getLatitude());
				String lng = String.valueOf(location.getLongitude());
				String locationStr = Geohash.encode(location.getLatitude(),
						location.getLongitude());
				System.out.println("lat--------->>>" + lat);
				System.out.println("lng--------->>>" + lng);
				updateInServer(lat, lng, locationStr);
				mLocationClient.stop();
				SAVE = false;
			}

		}

	}

	private void updateInServer(final String lat, final String lng, String locationStr) {

		RequestBody formBody = new FormEncodingBuilder().add("lat", lat)
				.add("hxid", DemoHelper.getInstance().getCurrentUsernName())
				.add("lng", lng).add("location", locationStr).build();

		Request request = new Request.Builder().url(Constant.URL_UPDATE_LOCATION)
				.post(formBody).build();
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Request arg0, IOException arg1) {

			}

			@Override
			public void onResponse(Response arg0) throws IOException {
				if (arg0.isSuccessful()) {
					String result = arg0.body().string();

					System.out.println("result--->" + result);
					
					JSONObject json=JSONObject.parseObject(result);
					if(json!=null){
						int code =json.getIntValue("code");
						if(code==1000){
							
							DemoApplication.getInstance().setLat(lat);
							DemoApplication.getInstance().setLng(lng);
						}
					}
					

				}
			}

		});

	}

	// 定位参数

	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);//
		option.setCoorType(tempcoor);//
		int span = 300000;
		option.setScanSpan(span);//
		option.setIsNeedAddress(false);

		mLocationClient.setLocOption(option);
	}

}
