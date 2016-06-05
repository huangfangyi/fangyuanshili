package com.fysl.app.main;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fysl.app.Constant;
import com.fysl.app.DemoApplication;
import com.fysl.app.DemoHelper;
import com.fysl.app.R;
import com.fysl.app.main.utils.JSON2User;
import com.fysl.app.ui.BaseActivity;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class UserDetailActivity extends BaseActivity {

	private final OkHttpClient client = new OkHttpClient();
	private String hxid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_userdetail);

		String userInfo = this.getIntent().getStringExtra("userInfo");
		if (TextUtils.isEmpty(userInfo)) {
			finish();
			return;
		}
		JSONObject json = JSONObject.parseObject(userInfo);
		initView(json);

		if (DemoHelper.getInstance().getContactList()
				.containsKey(json.getString("hxid"))) {

			refresh(json.getString("hxid"));
		}
	}

	private void initView(final JSONObject json) {

		ImageView iv_sex = (ImageView) this.findViewById(R.id.iv_sex);
		if (json.getString("sex").equals("女")) {

			iv_sex.setImageResource(R.drawable.fy_ic_sex_female);
		}

		TextView tv_name = (TextView) this.findViewById(R.id.tv_name);
		tv_name.setText(json.getString("nick"));
		hxid = json.getString("hxid");

		TextView tv_region = (TextView) this.findViewById(R.id.tv_region);
		tv_region.setText(json.getString("province") + " "
				+ json.getString("city"));
		TextView tv_age = (TextView) this.findViewById(R.id.tv_age);
		tv_age.setText(json.getString("age"));

		TextView tv_sign = (TextView) this.findViewById(R.id.tv_sign);
		tv_sign.setText(json.getString("sign"));

		Button btn_add = (Button) this.findViewById(R.id.btn_add);
		Button btn_chat = (Button) this.findViewById(R.id.btn_chat);
		if (DemoHelper.getInstance().getContactList()
				.containsKey(json.getString("hxid"))) {

			btn_add.setVisibility(View.GONE);
			btn_chat.setVisibility(View.VISIBLE);
		} else {

			btn_add.setVisibility(View.VISIBLE);
			btn_chat.setVisibility(View.GONE);
		}
		if (DemoHelper.getInstance().getCurrentUsernName()
				.equals(json.getString("hxid"))) {

			btn_add.setVisibility(View.GONE);
			btn_chat.setVisibility(View.GONE);
		}

		btn_add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addContact(json.getString("hxid"));
			}

		});

		btn_chat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(UserDetailActivity.this,
						ChatActivity.class).putExtra(
						EaseConstant.EXTRA_USER_ID, json.getString("hxid")));
				// addContact(json.getString("hxid"));
			}

		});

		ImageView iv_avatar = (ImageView) this.findViewById(R.id.iv_avatar);
		if (json.getString("avatar") != null) {

			Glide.with(UserDetailActivity.this).load(json.getString("avatar"))
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.fy_default_useravatar)
					.into(iv_avatar);
		}

	}

	private void refresh(String id) {

		RequestBody formBody = new FormEncodingBuilder().add("hxid", id)
				.build();
		// Constant.KEY_TEL
		Request request = new Request.Builder().url(Constant.URL_GET_USERINFO)
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
					final JSONObject json = JSONObject.parseObject(Constant
							.jsonTokener(result));

					if (json.getInteger("code") == 1000) {

						final JSONObject userInfo = json
								.getJSONObject("user");
						if (userInfo != null) {
							if (DemoHelper.getInstance().getContactList()
									.containsKey(userInfo.getString("hxid"))) {

								EaseUser user = JSON2User.getUser(userInfo);
								DemoHelper.getInstance().saveContact(user);
								DemoHelper.getInstance().getContactList()
										.put(user.getUsername(), user);
							}

							runOnUiThread(new Runnable() {

								@SuppressLint("NewApi")
								@Override
								public void run() {
									if (!UserDetailActivity.this.isDestroyed()
											&& !UserDetailActivity.this
													.isFinishing()) {

										initView(userInfo);
									}

								}

							});

						}

						// DemoApplication.getInstance().setJSON(userInfo);
						// loginHuanxin(userInfo.getString("hxid"),
						// userInfo.getString("hx_password"), pd);
					}

				} else {

					throw new IOException("Unexpected code " + arg0);
				}

			}

		});

	}

	/**
	 * 添加contact
	 * 
	 * @param view
	 */
	public void addContact(final String hxid) {

		if (EMClient.getInstance().getCurrentUser().equals(hxid)) {
			new EaseAlertDialog(this, R.string.not_add_myself).show();
			return;
		}

		if (DemoHelper.getInstance().getContactList().containsKey(hxid)) {
			// 提示已在好友列表中(在黑名单列表里)，无需添加
			if (EMClient.getInstance().contactManager().getBlackListUsernames()
					.contains(hxid)) {
				new EaseAlertDialog(this, R.string.user_already_in_contactlist)
						.show();
				return;
			}
			new EaseAlertDialog(this, R.string.This_user_is_already_your_friend)
					.show();
			return;
		}

		final ProgressDialog progressDialog = new ProgressDialog(this);
		String stri = getResources().getString(R.string.Is_sending_a_request);
		progressDialog.setMessage(stri);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();

		new Thread(new Runnable() {
			public void run() {

				try {
					// demo写死了个reason，实际应该让用户手动填入
					// String s =
					// getResources().getString(R.string.Add_a_friend);

					JSONObject myInfo = DemoApplication.getInstance()
							.getUserInfo();
					myInfo.put("password", "");
					myInfo.put("tel", "");
					EMClient.getInstance().contactManager()
							.addContact(hxid, myInfo.toJSONString());
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s1 = getResources().getString(
									R.string.send_successful);
							Toast.makeText(getApplicationContext(), s1,
									Toast.LENGTH_LONG).show();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s2 = getResources().getString(
									R.string.Request_add_buddy_failure);
							Toast.makeText(getApplicationContext(),
									s2 + e.getMessage(), Toast.LENGTH_LONG)
									.show();
						}
					});
				}
			}
		}).start();
	}

}
