package com.fysl.app.main;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fysl.app.DemoApplication;
import com.fysl.app.R;
import com.zbar.scan.ScanCaptureAct;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentProfile extends Fragment implements View.OnClickListener {

	protected boolean isConflict;
	protected boolean hidden;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_profile, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("isConflict", false))
			return;

		initView();
	}

	private void initView() {
		TextView tv_name = (TextView) getView().findViewById(R.id.tv_name);
		ImageView iv_sex = (ImageView) getView().findViewById(R.id.iv_sex);
		ImageView iv_avatar = (ImageView) getView()
				.findViewById(R.id.iv_avatar);
		JSONObject userJSON = DemoApplication.getInstance().getUserInfo();
		tv_name.setText(userJSON.getString("nick"));
		if (userJSON.getString("sex").equals("女")) {

			iv_sex.setImageResource(R.drawable.fy_ic_sex_female);
		}
		Glide.with(getActivity()).load(userJSON.getString("avatar"))
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.fy_default_useravatar).into(iv_avatar);
		getView().findViewById(R.id.re_myinfo).setOnClickListener(this);
		getView().findViewById(R.id.re_dongtai).setOnClickListener(this);
		getView().findViewById(R.id.re_setting).setOnClickListener(this);
		getView().findViewById(R.id.re_toqr).setOnClickListener(this);
		getView().findViewById(R.id.re_myqr).setOnClickListener(this);

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
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (isConflict) {
			outState.putBoolean("isConflict", true);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.re_myinfo:

			startActivityForResult(new Intent(getActivity(),
					MyUserInfoActivity.class), 1000);
			break;
		case R.id.re_dongtai:
			startActivity(new Intent(getActivity(), MyThingsActivity.class));
			
			break;
		case R.id.re_setting:
			startActivity(new Intent(getActivity(), SettingsActivity.class));
			break;
		case R.id.re_myqr:
			startActivity(new Intent(getActivity(), MyQrActivity.class));
			break;
		case R.id.re_toqr:
			startActivity(new Intent(getActivity(), ScanCaptureAct.class));

			break;

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1000) {

			if (resultCode == Activity.RESULT_OK) {
				refresh();

			}

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void refresh() {

		TextView tv_name = (TextView) getView().findViewById(R.id.tv_name);
		ImageView iv_sex = (ImageView) getView().findViewById(R.id.iv_sex);
		ImageView iv_avatar = (ImageView) getView()
				.findViewById(R.id.iv_avatar);
		JSONObject userJSON = DemoApplication.getInstance().getUserInfo();
		tv_name.setText(userJSON.getString("nick"));
		if (userJSON.getString("sex").equals("女")) {

			iv_sex.setImageResource(R.drawable.fy_ic_sex_female);
		}
		Glide.with(getActivity()).load(userJSON.getString("avatar"))
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.fy_default_useravatar).into(iv_avatar);

	}

}
