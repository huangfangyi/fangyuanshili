package com.fysl.app.main.adapter;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fysl.app.Constant;
import com.fysl.app.DemoApplication;
import com.fysl.app.R;
import com.fysl.app.main.BigImageActivity;
import com.fysl.app.main.MyUserInfoActivity;
import com.fysl.app.main.ThingDetailActivity;
import com.fysl.app.main.UserDetailActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ThingsAdapter extends BaseAdapter {
	private Context context;
	List<JSONObject> jsons;
	private LayoutInflater inflater;

	public ThingsAdapter(Context _context, List<JSONObject> jsons) {
		this.jsons = jsons;
		this.context = _context;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return jsons.size();
	}

	@Override
	public JSONObject getItem(int position) {
		// TODO Auto-generated method stub
		return jsons.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {

			convertView = inflater.inflate(R.layout.item_fragment_things,
					parent, false);

		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		if (holder == null) {
			holder = new ViewHolder();
			holder.iv_avatar = (ImageView) convertView
					.findViewById(R.id.iv_avatar);
			holder.iv_image1 = (ImageView) convertView
					.findViewById(R.id.iv_image1);
			holder.iv_image2 = (ImageView) convertView
					.findViewById(R.id.iv_image2);
			holder.iv_image3 = (ImageView) convertView
					.findViewById(R.id.iv_image3);

			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_distance = (TextView) convertView
					.findViewById(R.id.tv_distance);
			holder.tv_content = (TextView) convertView
					.findViewById(R.id.tv_content);
			holder.tv_address = (TextView) convertView
					.findViewById(R.id.tv_address);

			holder.ll_pl = (RelativeLayout) convertView
					.findViewById(R.id.ll_pl);

			convertView.setTag(holder);
		}
		final JSONObject userJson = getItem(position);
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
		holder.tv_distance.setText(distance + "m");
		Glide.with(context).load(avatar)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.fy_default_useravatar)
				.into(holder.iv_avatar);
		holder.tv_content.setText(content);
		holder.tv_name.setText(nick);
		if (!imageStr.equals("0") && imageStr != null) {
			String[] images = imageStr.split("split");
			int imNumb = images.length;
			holder.iv_image1.setVisibility(View.VISIBLE);
			Glide.with(context).load(Constant.URL_SOCIAL_PHOTO + images[0])
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.default_image).centerCrop()
					.into(holder.iv_image1);
			holder.iv_image1.setOnClickListener(new ImageListener(images, 0));

			Log.e("imNumb--->>", String.valueOf(imNumb));
			// 四张图的时间情况比较特殊
			if (imNumb == 2) {
				holder.iv_image2.setVisibility(View.VISIBLE);
				// holder.iv_image2.setImageURI(Uri
				// .parse(Constant.URL_SOCIAL_PHOTO + images[1]));
				Glide.with(context).load(Constant.URL_SOCIAL_PHOTO + images[1])
						.diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
						.placeholder(R.drawable.default_image)
						.into(holder.iv_image2);

				holder.iv_image2
						.setOnClickListener(new ImageListener(images, 1));
				holder.iv_image3.setVisibility(View.GONE);

			} else if (imNumb > 2) {
				holder.iv_image2.setVisibility(View.VISIBLE);
				Glide.with(context).load(Constant.URL_SOCIAL_PHOTO + images[1])
						.diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
						.placeholder(R.drawable.default_image)
						.into(holder.iv_image2);
				holder.iv_image2
						.setOnClickListener(new ImageListener(images, 1));

				holder.iv_image3.setVisibility(View.VISIBLE);
				Glide.with(context).load(Constant.URL_SOCIAL_PHOTO + images[2])
						.centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.default_image)
						.into(holder.iv_image3);
				holder.iv_image3
						.setOnClickListener(new ImageListener(images, 2));
			} else {
				holder.iv_image2.setVisibility(View.GONE);

				holder.iv_image3.setVisibility(View.GONE);

			}

		}
		if (!TextUtils.isEmpty(location)) {

			holder.tv_address.setText(location);
		}
		holder.iv_avatar.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				context.startActivity(new Intent(context,UserDetailActivity.class).putExtra("userInfo", userJson.getJSONObject("userInfo").toJSONString()));
			}
			
			
		});
		// TODO Auto-generated method stub
		return convertView;
	}

	public static class ViewHolder {
		ImageView iv_avatar;
		ImageView iv_image1;
		ImageView iv_image2;
		ImageView iv_image3;
		TextView tv_name;
		TextView tv_distance;
		TextView tv_content;
		TextView tv_address;
		RelativeLayout ll_pl;

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

	/**
	 * 将给定图片维持宽高比缩放后，截取正中间的正方形部分。
	 * 
	 * @param bitmap
	 *            原图
	 * @param edgeLength
	 *            希望得到的正方形部分的边长
	 * @return 缩放截取正中部分后的位图。
	 */
	public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
		if (null == bitmap || edgeLength <= 0) {
			return null;
		}

		Bitmap result = bitmap;
		int widthOrg = bitmap.getWidth();
		int heightOrg = bitmap.getHeight();

		if (widthOrg > edgeLength && heightOrg > edgeLength) {
			// 压缩到一个最小长度是edgeLength的bitmap
			int longerEdge = (int) (edgeLength * Math.max(widthOrg, heightOrg) / Math
					.min(widthOrg, heightOrg));
			int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
			int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
			Bitmap scaledBitmap;

			try {
				scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth,
						scaledHeight, true);
			} catch (Exception e) {
				return null;
			}

			// 从图中截取正中间的正方形部分。
			int xTopLeft = (scaledWidth - edgeLength) / 2;
			int yTopLeft = (scaledHeight - edgeLength) / 2;

			try {
				result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft,
						edgeLength, edgeLength);
				scaledBitmap.recycle();
			} catch (Exception e) {
				return null;
			}
		}

		return result;
	}

}
