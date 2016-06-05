package com.fysl.app.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fysl.app.Constant;
import com.fysl.app.DemoApplication;
import com.fysl.app.R;
import com.fysl.app.ui.BaseActivity;

public class ThingDetailActivity extends BaseActivity {
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thing_detail);
		context = this;
		String thingsInfo = this.getIntent().getStringExtra("thingsInfo");
		if (thingsInfo == null) {

			finish();
			return;
		}
		
		ListView listView=(ListView) this.findViewById(R.id.listView);
		final JSONObject userJson = JSONObject.parseObject(thingsInfo);
		View view = LayoutInflater.from(ThingDetailActivity.this).inflate(
				R.layout.item_frament_things_gridview, null, false);

		TextView tv_distance = (TextView) view.findViewById(R.id.tv_distance);
		TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
		TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
		TextView tv_address = (TextView) view.findViewById(R.id.tv_address);
		ImageView iv_avatar = (ImageView) view.findViewById(R.id.iv_avatar);
		GridView gridView = (GridView) view.findViewById(R.id.gridView);

		String imageStr = userJson.getString("imageStr");
		String content = userJson.getString("content");
		String location = userJson.getString("location");

		// String location = userJson.getString("location");
		String nick = userJson.getJSONObject("userInfo").getString("nick");
		String avatar = userJson.getJSONObject("userInfo").getString("avatar");
		String lat = userJson.getString("lat");
		String lng = userJson.getString("lng");

		LatLng p1 = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
		LatLng p2 = new LatLng(Double.parseDouble(DemoApplication.getInstance()
				.getLat()), Double.parseDouble(DemoApplication.getInstance()
				.getLng()));
		String distance = String.valueOf(DistanceUtil.getDistance(p1, p2));
		if (distance.contains(".")) {
			distance = distance.substring(0, distance.indexOf("."));
		}
		tv_distance.setText(distance + "m");
		Glide.with(context).load(avatar)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.fy_default_useravatar).into(iv_avatar);
		tv_content.setText(content);
		tv_name.setText(nick);
		if (!imageStr.equals("0") && imageStr != null) {
			String[] images = imageStr.split("split");
			int imNumb = images.length;
		    gridView.setAdapter(new GridAdapter(context,images));

		}
		if (!TextUtils.isEmpty(location)) {

			tv_address.setText(location);
		}
		listView.addHeaderView(view);
		listView.setAdapter(new ListViewAdapter(context,new JSONArray()));
		iv_avatar.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ThingDetailActivity.this,UserDetailActivity.class).putExtra("userInfo", userJson.getJSONObject("userInfo").toJSONString()));
			}
			
			
		});
		// 隐藏输入法
		InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		// 显示或者隐藏输入法
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	class ImageListener implements View.OnClickListener {
		String[] images;
		int page;

		public ImageListener(String[] images, int page) {

			this.images = images;
			this.page = page;
		}

		@Override
		public void onClick(View v) {

			Intent intent = new Intent();
			intent.setClass(context, BigImageActivity.class);
			intent.putExtra("images", images);
			intent.putExtra("page", page);
			context.startActivity(intent);

		}

	}

	class GridAdapter extends BaseAdapter {
		private Context _context;
		private String[] imageLists;
		private LayoutInflater inflater;

		public GridAdapter(Context _context, String[] imageLists) {
			this._context = _context;
			this.imageLists = imageLists;
			inflater = LayoutInflater.from(_context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return imageLists.length;
		}

		@Override
		public String getItem(int position) {
			// TODO Auto-generated method stub
			return imageLists[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {

				convertView = inflater.inflate(R.layout.item_gridview, null,
						false);
			}

			ViewHolder holder = (ViewHolder) convertView.getTag();

			if (holder == null) {
				holder = new ViewHolder();
				holder.iv_image = (ImageView) convertView
						.findViewById(R.id.iv_image);
				convertView.setTag(holder);
			}
			Glide.with(_context).load(Constant.URL_SOCIAL_PHOTO+getItem(position)).centerCrop()
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.default_image)
					.into(holder.iv_image);
			holder.iv_image.setOnClickListener(new ImageListener(imageLists,
					position));
			return convertView;
		}

		class ViewHolder {

			private ImageView iv_image;
		}
	}

	class ListViewAdapter extends BaseAdapter {
		private Context _context;
		private JSONArray jsonArray;

		public ListViewAdapter(Context _context, JSONArray jsonArray) {
			this._context = _context;
			this.jsonArray = jsonArray;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return jsonArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			// TODO Auto-generated method stub
			return jsonArray.getJSONObject(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return convertView;
		}
		
		
		
		
		
		class ViewHolder{
			
			
		}

	}
}
