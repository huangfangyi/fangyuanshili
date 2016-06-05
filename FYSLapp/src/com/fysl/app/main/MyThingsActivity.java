package com.fysl.app.main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fysl.app.Constant;
import com.fysl.app.DemoApplication;
import com.fysl.app.DemoHelper;
import com.fysl.app.R;
import com.fysl.app.main.adapter.ThingsAdapter;
import com.fysl.app.main.utils.ACache;
import com.fysl.app.ui.BaseActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class MyThingsActivity extends BaseActivity{
	
	private List<JSONObject> jsonArray = new ArrayList<JSONObject>();
	protected boolean isConflict;
	protected boolean hidden;
	private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private PullToRefreshListView pull_refresh_list;
	private final OkHttpClient client = new OkHttpClient();
	private int page = 1;
	private JSONArray jsonArray_Cache;
	private ThingsAdapter adapter;
	private ListView actualListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_things);
		
		

		initView();
		initFile();
	}
	@SuppressLint("SdCardPath")
	public void initFile() {

		File dir = new File("/sdcard/bizchat");

		if (!dir.exists()) {
			dir.mkdirs();
		}

	}

	private void initView() {
		EaseTitleBar titleBar = (EaseTitleBar) findViewById(
				R.id.titleBar);
		titleBar.setRightImageResource(R.drawable.icon_camera);
		titleBar.setRightLayoutClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showPhotoDialog();
			}

		});
		pull_refresh_list = (PullToRefreshListView) findViewById(
				R.id.pull_refresh_list);
		pull_refresh_list.setMode(Mode.BOTH);

		pull_refresh_list
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(MyThingsActivity.this,
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);

						// Update the LastUpdatedLabel
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);

						// Do work to refresh the list here.

						if (pull_refresh_list.getCurrentMode() == Mode.PULL_FROM_START) {
							page = 1;

						} else if (pull_refresh_list.getCurrentMode() == Mode.PULL_FROM_END) {
							page++;

						}

						getData(page);
					}
				});

		actualListView = pull_refresh_list.getRefreshableView();
		JSONArray jsonArray_temp = ACache.get(MyThingsActivity.this).getAsJSONArray(
				"things");
		if (jsonArray_temp != null) {
			jsonArray_Cache = jsonArray_temp;
			JSONArray2List(jsonArray_temp, jsonArray);
			try {
				page = Integer.parseInt(ACache.get(MyThingsActivity.this).getAsString(
						"things"));
			} catch (RuntimeException ex) {
				page = 1;
			}

		}

		adapter = new ThingsAdapter(MyThingsActivity.this, jsonArray);
		actualListView.setAdapter(adapter);
		actualListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				 JSONObject json = adapter.getItem(position - 1);
				 startActivity(new Intent(MyThingsActivity.this,
						 ThingDetailActivity.class).putExtra("thingsInfo",
				 json.toJSONString()));
			}

		});
		// actualListView.setOnTouchListener(new OnTouchListener() {
		//
		// @SuppressLint("ClickableViewAccessibility")
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// //adapter.hideCommentEditText();
		// return false;
		// }
		//
		// });
		getData(1);
		pull_refresh_list.setRefreshing(false);

	}

	private void getData(final int page) {
		RequestBody formBody = new FormEncodingBuilder()
		.add("num", String.valueOf(page))
		 
		.add("hxid", DemoHelper.getInstance().getCurrentUsernName()).build();
		Request request = new Request.Builder().url(Constant.URL_GET_SOCIAL)
				.post(formBody)

				.build();

		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Request arg0, IOException arg1) {

				MyThingsActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						pull_refresh_list.onRefreshComplete();
						Toast.makeText(MyThingsActivity.this, "连不上服务器",
								Toast.LENGTH_SHORT).show();
					}

				});

			}

			@Override
			public void onResponse(Response arg0) throws IOException {
				MyThingsActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						pull_refresh_list.onRefreshComplete();

					}

				});
				// System.out.println("response--->" + arg0.body().string());

				if (arg0.isSuccessful()) {
					String result = arg0.body().string();

					System.out.println("result--->" + result);
					final JSONObject json = JSONObject.parseObject(result);

					if (json.getInteger("code") == 1000) {
					 

						JSONArray jsonArray_temp = json
								.getJSONArray("things");
                        if(jsonArray_temp!=null&&jsonArray_temp.size()!=0){
                        	
                        	if (page == 1) {
    							jsonArray.clear();
    							JSONArray2List(jsonArray_temp, jsonArray);
    							jsonArray_Cache = jsonArray_temp;
    						} else {

    							JSONArray2List(jsonArray_temp, jsonArray);
    							jsonArray_Cache.addAll(jsonArray_temp);
    						}

    						MyThingsActivity.this.runOnUiThread(new Runnable() {

    							@Override
    							public void run() {
    								// TODO Auto-generated method stub

    								adapter.notifyDataSetChanged();

    							}

    						});

    						ACache.get(MyThingsActivity.this)
    								.put("things", jsonArray_Cache);
    						ACache.get(MyThingsActivity.this).put("page", page + "");

                        	
                        }
						
					} else {
						 
						MyThingsActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub

								Toast.makeText(MyThingsActivity.this, "访问失败",
										Toast.LENGTH_SHORT).show();
							}

						});
					}

				} else {

					MyThingsActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

							Toast.makeText(MyThingsActivity.this, "访问失败",
									Toast.LENGTH_SHORT).show();
						}

					});

					throw new IOException("Unexpected code " + arg0);
				}

			}

		});

	}

	private String imageName;

	private void showPhotoDialog() {
		final AlertDialog dlg = new AlertDialog.Builder(MyThingsActivity.this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.fy_dialog_alert);
		TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
		tv_paizhao.setText("拍照");
		tv_paizhao.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("SdCardPath")
			public void onClick(View v) {

				imageName = getNowTime() + ".jpg";
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// 指定调用相机拍照后照片的储存路径
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File("/sdcard/bizchat/", imageName)));
				startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
				dlg.cancel();
			}
		});
		TextView tv_xiangce = (TextView) window.findViewById(R.id.tv_content2);
		tv_xiangce.setText("相册");
		tv_xiangce.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				getNowTime();
				imageName = getNowTime() + ".jpg";
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, PHOTO_REQUEST_GALLERY);

				dlg.cancel();
			}
		});

	}

	@SuppressLint("SimpleDateFormat")
	private String getNowTime() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmssSS");
		return dateFormat.format(date);
	}

	@SuppressLint("SdCardPath")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {

			String path = null;

			switch (requestCode) {

			case PHOTO_REQUEST_TAKEPHOTO:
				path = "/sdcard/bizchat/" + imageName;
				break;

			case PHOTO_REQUEST_GALLERY:

				if (data != null) {
					Uri imageFilePath = data.getData();

					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor cursor = MyThingsActivity.this.getContentResolver().query(
							imageFilePath, proj, null, null, null);
					int column_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					// 获取图片真实地址
					path = cursor.getString(column_index);
					System.out.println(path);

				}

				break;

			}

			Intent intent = new Intent();
			intent.putExtra("imagePath", path);

			intent.setClass(MyThingsActivity.this, SocialPublishActivity.class);
			startActivity(intent);
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void JSONArray2List(JSONArray jsonArray, List<JSONObject> lists) {
		;
		for (int i = 0; i < jsonArray.size(); i++) {

			lists.add(jsonArray.getJSONObject(i));
		}

	}

}
