package com.fysl.app.main;

import java.io.IOException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.fysl.app.Constant;
import com.fysl.app.DemoApplication;
import com.fysl.app.DemoHelper;
import com.fysl.app.R;
import com.fysl.app.main.utils.Geohash;
import com.hyphenate.easeui.widget.EaseSwitchButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentPeople extends Fragment {

	protected boolean isConflict;
	protected boolean hidden;
	public LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor = "bd09ll";
	boolean isFirstLoc = true;

	ImageLoader imageLoad;
	DisplayImageOptions options;
	Marker centerMarker = null;

	BitmapDescriptor bdA = BitmapDescriptorFactory
			.fromResource(R.drawable.mylocation);

	View view_marker;

	private final OkHttpClient client = new OkHttpClient();
	private EaseSwitchButton switch_all;

	private String isAll = "0";
	private String locationStr;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_people, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("isConflict", false))
			return;

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getActivity()).build();
		imageLoad = ImageLoader.getInstance();
		imageLoad.init(config);
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ease_default_avatar_mini) // resource

				.showImageForEmptyUri(R.drawable.ease_default_avatar_mini) // resource

				.showImageOnFail(R.drawable.ease_default_avatar_mini) // resource
																		// or

				.resetViewBeforeLoading(false) // default
				.delayBeforeLoading(1000).cacheInMemory(true) // default
				.cacheOnDisc(true) // default
				.considerExifParams(false) // default
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
				.bitmapConfig(Bitmap.Config.ARGB_8888) // default
				.displayer(new CircleBitmapDisplayer(20)) // default
				.handler(new Handler()) // default
				.build();

		initView();
		setMarker();
	}

	private void initView() {
		mMapView = (MapView) getView().findViewById(R.id.bmapView);

		mBaiduMap = mMapView.getMap();

		mBaiduMap.setMyLocationEnabled(true);
		mLocClient = new LocationClient(getActivity());
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);//
		option.setCoorType(tempcoor);//
		option.setScanSpan(1000);//
		view_marker = LayoutInflater.from(getActivity()).inflate(
				R.layout.infowindow, null);
		mLocClient.setLocOption(option);
		mLocClient.start();
		switch_all = (EaseSwitchButton) getView().findViewById(R.id.switch_all);
		String state = DemoApplication.getInstance().getAllP();
		if (!TextUtils.isEmpty(state) && state.equals("1")) {

			switch_all.openSwitch();
			isAll = "1";

		} else {

			switch_all.closeSwitch();
			isAll = "0";

		}

		switch_all.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (switch_all.isSwitchOpen()) {

					switch_all.closeSwitch();
					isAll = "0";

				} else {

					switch_all.openSwitch();
					isAll = "1";

				}
				DemoApplication.getInstance().setAllP(isAll);
				getPeopleInServer();
			}

		});

	}

	private void setMarker() {

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				// 这个字段用于标识数据
				String title = marker.getTitle();
				startActivity(new Intent(getActivity(),
						UserDetailActivity.class).putExtra("userInfo", title));
				// Toast.makeText(getActivity(), "点击跳转...", Toast.LENGTH_SHORT)
				// .show();
				return true;
			}
		});
	}

	public void initOverlay(JSONArray data) {
		// if(data.size()!=0&&marker_temp!=null){
		//
		// marker_temp.remove();
		// }
		for (int i = 0; i < data.size(); i++) {

			final JSONObject json = data.getJSONObject(i);
			String lat = json.getString("lat");
			String lng = json.getString("lng");
			String avatar = json.getString("avatar");
			String nick = json.getString("nick");

			String sex = json.getString("sex");

			// final LatLng ll_temp = new LatLng(Double.parseDouble(lat),
			// Double.parseDouble(lng));
			ImageView iv_sex = (ImageView) view_marker
					.findViewById(R.id.iv_sex);

			final ImageView iv_avatar = (ImageView) view_marker
					.findViewById(R.id.iv_avatar);
			TextView tv_name = (TextView) view_marker
					.findViewById(R.id.tv_name);
			TextView tv_distance = (TextView) view_marker
					.findViewById(R.id.tv_distance);

			if (sex.equals("女")) {
				iv_sex.setImageResource(R.drawable.fy_ic_sex_female);

			}

			tv_name.setText(nick);

			final LatLng p1 = new LatLng(Double.parseDouble(lat),
					Double.parseDouble(lng));
			LatLng p2 = new LatLng(Double.parseDouble(DemoApplication
					.getInstance().getLat()),
					Double.parseDouble(DemoApplication.getInstance().getLng()));
			String distance = String.valueOf(DistanceUtil.getDistance(p1, p2));
			if (distance.contains(".")) {

				distance = distance.substring(0, distance.indexOf("."));
			}
			tv_distance.setText(distance + " m");

			// OverlayOptions ooA = new MarkerOptions().position(p1)
			// .icon(BitmapDescriptorFactory.fromView(view_marker))
			// .zIndex(9).draggable(false);
			// Marker marker_temp = (Marker) (mBaiduMap.addOverlay(ooA));

			// Glide.with(getActivity())
			// .load(avatar)
			// .into(new GlideDrawableImageViewTarget(iv_avatar) {
			//
			// @Override
			// public void onResourceReady(GlideDrawable arg0,
			// GlideAnimation<? super GlideDrawable> arg1) {
			//
			//
			//
			// // iv_avatar.setImageBitmap(Constant
			// // .toRoundCorner(iv_avatar.G, 2));
			//
			//
			//
			// OverlayOptions ooA = new MarkerOptions().position(p1)
			// .icon(BitmapDescriptorFactory.fromView(view_marker))
			// .zIndex(9).draggable(false);
			// Marker marker_temp = (Marker) (mBaiduMap.addOverlay(ooA));
			// marker_temp.setTitle(json.toJSONString());
			// super.onResourceReady(arg0, arg1);
			// }
			//
			// });

//			if (TextUtils.isEmpty(avatar) || !avatar.contains("http://")) {
//				OverlayOptions ooA = new MarkerOptions().position(p1)
//						.icon(BitmapDescriptorFactory.fromView(view_marker))
//						.zIndex(9).draggable(false);
//				Marker marker_temp = (Marker) (mBaiduMap.addOverlay(ooA));
//				marker_temp.setTitle(json.toJSONString());
//			} else {
				imageLoad.displayImage(avatar, iv_avatar, options,
						new ImageLoadingListener() {

							@Override
							public void onLoadingStarted(String imageUri,
									View view) {
								//
								System.out.println("444------------------>>>");
							}

							@Override
							public void onLoadingFailed(String imageUri,
									View view, FailReason failReason) {

								OverlayOptions ooA = new MarkerOptions()
										.position(p1)
										.icon(BitmapDescriptorFactory
												.fromView(view_marker))
										.zIndex(9).draggable(false);
								Marker marker_temp = (Marker) (mBaiduMap
										.addOverlay(ooA));
								marker_temp.setTitle(json.toJSONString());

								System.out.println("111------------------>>>");
							}

							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								if (loadedImage != null) {
									iv_avatar.setImageBitmap(Constant
											.toRoundCorner(loadedImage, 2));

								}

								OverlayOptions ooA = new MarkerOptions()
										.position(p1)
										.icon(BitmapDescriptorFactory
												.fromView(view_marker))
										.zIndex(9).draggable(false);
								Marker marker_temp = (Marker) (mBaiduMap
										.addOverlay(ooA));
								marker_temp.setTitle(json.toJSONString());
								System.out.println("222------------------>>>");
							}

							@Override
							public void onLoadingCancelled(String imageUri,
									View view) {
								OverlayOptions ooA = new MarkerOptions()
										.position(p1)
										.icon(BitmapDescriptorFactory
												.fromView(view_marker))
										.zIndex(9).draggable(false);
								Marker marker_temp = (Marker) (mBaiduMap
										.addOverlay(ooA));
								marker_temp.setTitle(json.toJSONString());
								System.out.println("333------------------>>>");
							}

						});
			//}
		}

	}

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			if (location == null || mMapView == null) {
				return;
			}
			if (isFirstLoc) {
				isFirstLoc = false;
				double lng_temp = location.getLongitude();
				double lat_temp = location.getLatitude();
				// LatLng ll = new LatLng(location.getLatitude(),
				// location.getLongitude());
				LatLng ll = new LatLng(lat_temp, lng_temp);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				if (centerMarker == null) {
					OverlayOptions ooA = new MarkerOptions().position(ll)
							.icon(bdA).zIndex(9).draggable(false);
					centerMarker = (Marker) mBaiduMap.addOverlay(ooA);
				} else {
					centerMarker.setPosition(ll);
				}
				// url_temp = "?lng" + "=" + String.valueOf(lng_temp) + "&lat="
				// + String.valueOf(lat_temp) + "&range=1000"
				// + "&type=client";
				locationStr = Geohash.encode(lat_temp, lng_temp);

				getPeopleInServer();

				mLocClient.stop();
			}

		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	private void getPeopleInServer() {

		if (TextUtils.isEmpty(locationStr)) {

			Toast.makeText(getActivity(), "获取位置信息失败...", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		System.out.println("locationStr---->>>." + locationStr);
		final ProgressDialog progressDialog = new ProgressDialog(getActivity());

		progressDialog.setMessage("正在搜寻...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();

		RequestBody formBody = new FormEncodingBuilder()

		.add("location", locationStr)
				.add("hxid", DemoHelper.getInstance().getCurrentUsernName())
				.add("isAll", isAll).build();

		Request request = new Request.Builder().url(Constant.URL_GET_PEOPLE)
				.post(formBody).build();
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Request arg0, IOException arg1) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						progressDialog.dismiss();
						Toast.makeText(getActivity(), "连接服务器失败...",
								Toast.LENGTH_SHORT).show();
					}

				});
			}

			@Override
			public void onResponse(Response arg0) throws IOException {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						progressDialog.dismiss();

					}

				});
				// System.out.println("response--->" + arg0.body().string());

				if (arg0.isSuccessful()) {
					String result = arg0.body().string();

					System.out.println("result--->" + result);
					final JSONObject json = JSONObject.parseObject(Constant
							.jsonTokener(result));

					if (json.getInteger("code") == 1000) {

						final JSONArray userJson = json.getJSONArray("people");
						// 返回自己的资料信息
						// 进行SDK登录后进入主页

						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {

								// TODO Auto-generated method stub
								initOverlay(userJson);
							}

						});
					}

				}
			}

		});

	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden && !isConflict) {

		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {

		}
		isFirstLoc = true;
		mMapView.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (isConflict) {
			outState.putBoolean("isConflict", true);
		}
	}

	public class CircleBitmapDisplayer implements BitmapDisplayer {

		protected final int margin;

		public CircleBitmapDisplayer() {
			this(0);
		}

		public CircleBitmapDisplayer(int margin) {
			this.margin = margin;
		}

		@Override
		public void display(Bitmap bitmap, ImageAware imageAware,
				LoadedFrom loadedFrom) {
			if (!(imageAware instanceof ImageViewAware)) {
				throw new IllegalArgumentException(
						"ImageAware should wrap ImageView. ImageViewAware is expected.");
			}

			imageAware.setImageBitmap(Constant.toRoundCorner(bitmap, 1));
		}

	}

}
