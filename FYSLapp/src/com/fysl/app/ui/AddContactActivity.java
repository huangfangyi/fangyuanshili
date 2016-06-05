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
package com.fysl.app.ui;

import java.io.IOException;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMContactManager;
import com.alibaba.fastjson.JSONObject;
import com.fysl.app.Constant;
import com.fysl.app.DemoApplication;
import com.fysl.app.DemoHelper;
import com.fysl.app.R;
import com.fysl.app.main.UserDetailActivity;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddContactActivity extends BaseActivity {
	private EditText editText;
	private LinearLayout searchedUserLayout;
	private TextView nameText, mTextView;
	private Button searchBtn;
	private ImageView avatar;
	private InputMethodManager inputMethodManager;
	private String toAddUsername;
	private ProgressDialog progressDialog;
	private final OkHttpClient client = new OkHttpClient();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_add_contact);
		mTextView = (TextView) findViewById(R.id.add_list_friends);

		editText = (EditText) findViewById(R.id.edit_note);
		String strAdd = getResources().getString(R.string.add_friend);
		mTextView.setText(strAdd);
		// String strUserName = getResources().getString(R.string.user_name);
		editText.setHint("对方手机号");
		searchedUserLayout = (LinearLayout) findViewById(R.id.ll_user);
		nameText = (TextView) findViewById(R.id.name);
		searchBtn = (Button) findViewById(R.id.search);
		avatar = (ImageView) findViewById(R.id.avatar);
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	/**
	 * 查找contact
	 * 
	 * @param v
	 */
	public void searchContact(View v) {
		final String name = editText.getText().toString();
		String saveText = searchBtn.getText().toString();

		if (getString(R.string.button_search).equals(saveText)) {
			toAddUsername = name;
			if (TextUtils.isEmpty(name)) {
				new EaseAlertDialog(this, R.string.Please_enter_a_username)
						.show();
				return;
			}

			// TODO 从服务器获取此contact,如果不存在提示不存在此用户

			// 服务器存在此用户，显示此用户和添加按钮
			searchUserInServer(toAddUsername);
			// searchedUserLayout.setVisibility(View.VISIBLE);
			// nameText.setText(toAddUsername);

		}
	}

	private void searchUserInServer(String value) {

		final ProgressDialog pd = new ProgressDialog(AddContactActivity.this);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("正在处理...");
		pd.show();
		 

		RequestBody formBody = new FormEncodingBuilder().add("uid", value)

		.build();

		// Constant.KEY_TEL
		Request request = new Request.Builder().url(Constant.URL_SEARCH_USER).post(formBody)

		.build();
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Request arg0, IOException arg1) {

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						pd.dismiss();
						Toast.makeText(AddContactActivity.this, "连不上服务器",
								Toast.LENGTH_SHORT).show();

					}

				});
			}

			@Override
			public void onResponse(Response arg0) throws IOException {

				if (arg0.isSuccessful()) {
					String result = arg0.body().string();
					System.out.println("result--->" + result);
					final JSONObject json = JSONObject.parseObject(Constant
							.jsonTokener(result));

					if (json.getInteger("code") == 1000) {
						
						
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								pd.dismiss();

							}

						});

						JSONObject userInfo = json.getJSONObject("user");

						startActivity(new Intent(AddContactActivity.this,
								UserDetailActivity.class).putExtra("userInfo",
								userInfo.toJSONString()));
 
					} else {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								pd.dismiss();
								Toast.makeText(getApplicationContext(),
										"未找到该用户", Toast.LENGTH_SHORT).show();
							}

						});

					}

				} else {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							pd.dismiss();
						}

					});
					throw new IOException("Unexpected code " + arg0);
				}

			}

		});

	}

	public static boolean isNo(String str) {

		boolean result = str.matches("[0-9]+");
		return result;
	}

	/**
	 * 添加contact
	 * 
	 * @param view
	 */
	public void addContact(View view) {
		if (EMClient.getInstance().getCurrentUser()
				.equals(nameText.getText().toString())) {
			new EaseAlertDialog(this, R.string.not_add_myself).show();
			return;
		}

		if (DemoHelper.getInstance().getContactList()
				.containsKey(nameText.getText().toString())) {
			// 提示已在好友列表中(在黑名单列表里)，无需添加
			if (EMClient.getInstance().contactManager().getBlackListUsernames()
					.contains(nameText.getText().toString())) {
				new EaseAlertDialog(this, R.string.user_already_in_contactlist)
						.show();
				return;
			}
			new EaseAlertDialog(this, R.string.This_user_is_already_your_friend)
					.show();
			return;
		}

		progressDialog = new ProgressDialog(this);
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

					JSONObject myInfo = new JSONObject();
					myInfo=DemoApplication.getInstance().getUserInfo();
					// myInfo.put("nick",
					// DemoApplication.getInstance().getJSON().get("real_name"));

					EMClient.getInstance().contactManager()
							.addContact(toAddUsername, myInfo.toJSONString());
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s1 = getResources().getString(
									R.string.send_successful);
							Toast.makeText(AddContactActivity.this, s1, 1)
									.show();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s2 = getResources().getString(
									R.string.Request_add_buddy_failure);
							Toast.makeText(AddContactActivity.this,
									s2 + e.getMessage(), 1).show();
						}
					});
				}
			}
		}).start();
	}

	public void back(View v) {
		finish();
	}
}
