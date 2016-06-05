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
package com.fysl.app.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fysl.app.Constant;
import com.fysl.app.DemoApplication;
import com.fysl.app.DemoHelper;
import com.fysl.app.R;
import com.fysl.app.RefreshLocationService;
import com.fysl.app.db.DemoDBManager;
import com.fysl.app.db.UserDao;
import com.fysl.app.main.utils.JSON2User;
import com.fysl.app.main.utils.MD5Util;
import com.fysl.app.ui.BaseActivity;
import com.hyphenate.easeui.domain.EaseUser;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
 

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 登陆页面
 * 
 */
public class LoginActivity extends BaseActivity {
	private static final String TAG = "LoginActivity";
	public static final int REQUEST_CODE_SETNICK = 1;
	private EditText usernameEditText;
	private EditText passwordEditText;

	private boolean progressShow;
	private boolean autoLogin = false;

	private String currentUsername;
	private String currentPassword;
	private final OkHttpClient client = new OkHttpClient();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// // 如果登录成功过，直接进入主页面
		// if (DemoHelper.getInstance().isLoggedIn()) {
		// autoLogin = true;
		// startActivity(new Intent(LoginActivity.this, MainActivity.class));
		//
		// return;
		// }
		setContentView(R.layout.activity_login);

		usernameEditText = (EditText) findViewById(R.id.et_username);
		passwordEditText = (EditText) findViewById(R.id.et_password);

		// 如果用户名改变，清空密码
		usernameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				passwordEditText.setText(null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		if (DemoHelper.getInstance().getCurrentUsernName() != null&&DemoApplication.getInstance().getUserInfo()!=null) {
			usernameEditText.setText(DemoApplication.getInstance().getUserInfo().getString("tel"));
		}

		Button btn_login = (Button) this.findViewById(R.id.btn_login);

		btn_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tel = usernameEditText.getText().toString().trim();
				String password = passwordEditText.getText().toString().trim();
				if (TextUtils.isEmpty(tel)) {
					Toast.makeText(getApplicationContext(), "账号不能为空...",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(password)) {
					Toast.makeText(getApplicationContext(), "密码不能为空...",
							Toast.LENGTH_SHORT).show();
					return;
				}
				loginInserver(tel, password);
			}

		});
	}

	private void loginInserver(String tel, String password) {

		final ProgressDialog progressDialog = new ProgressDialog(this);

		progressDialog.setMessage("正在登录...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();

		// 以上资料是否是强制设置的看作者自己决定，比如注册时头像不强制设置

		RequestBody formBody = new FormEncodingBuilder()

		.add("tel", tel).add("password", MD5Util.getMD5String(password))
				.build();

		Request request = new Request.Builder().url(Constant.URL_LOGIN)
				.post(formBody).build();
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Request arg0, IOException arg1) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						progressDialog.dismiss();
						Toast.makeText(getApplicationContext(), "连接服务器失败...",
								Toast.LENGTH_SHORT).show();
					}

				});
			}

			@Override
			public void onResponse(Response arg0) throws IOException {
				runOnUiThread(new Runnable() {

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

						final JSONObject userJson = json.getJSONObject("user");
						 DemoApplication.getInstance().savaUserInfo(userJson);
						// 返回自己的资料信息
						// 进行SDK登录后进入主页

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								
								  
								// TODO Auto-generated method stub
								loginHuanxin(userJson);
							}

						});
					}

				}
			}

		});

	}

	 

	private void getFriendsInfoInServer(String allFriends) {

		final ProgressDialog progressDialog = new ProgressDialog(this);

		progressDialog.setMessage("加载好友列表...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();

		// 以上资料是否是强制设置的看作者自己决定，比如注册时头像不强制设置

		RequestBody formBody = new FormEncodingBuilder()

		.add("allFriends", allFriends).build();

		Request request = new Request.Builder().url(Constant.URL_GET_FRIENDS)
				.post(formBody).build();
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onFailure(Request arg0, IOException arg1) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						progressDialog.dismiss();
						Toast.makeText(getApplicationContext(), "连接服务器失败...",
								Toast.LENGTH_SHORT).show();
					}

				});
			}

			@Override
			public void onResponse(Response arg0) throws IOException {
				runOnUiThread(new Runnable() {

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

						final JSONArray friends = json.getJSONArray("friends");
                        Map<String, EaseUser> usermap= new HashMap<String, EaseUser>();
						for(int i=0;i<friends.size();i++){
							JSONObject friend=friends.getJSONObject(i);
							EaseUser user=JSON2User.getUser(friend);
							usermap.put(user.getUsername(), user);
							
							
							
						}
						
						DemoHelper.getInstance().getContactList().putAll(usermap);
						UserDao dao= new UserDao(LoginActivity.this);
						dao.saveContactList(new ArrayList(usermap.values()));
						
						// // 返回自己的资料信息
						// // 进行SDK登录后进入主页
						//
						// runOnUiThread(new Runnable() {
						//
						// @Override
						// public void run() {
						// // TODO Auto-generated method stub
						// loginHuanxin(friends);
						// }
						//
						// });
					}

				}
			}

		});

	}

	private void loginHuanxin(final JSONObject userJson) {

		final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
		pd.setCanceledOnTouchOutside(false);

		pd.setMessage(getString(R.string.Is_landing));
		pd.show();

		// After logout，the DemoDB may still be accessed due to async callback,
		// so the DemoDB will be re-opened again.
		// close it before login to make sure DemoDB not overlap
		DemoDBManager.getInstance().closeDB();

		// reset current user name before login
		DemoHelper.getInstance().setCurrentUserName(userJson.getString("hxid"));
		//即时更新位置
		startService(new Intent(LoginActivity.this,RefreshLocationService.class));
		final long start = System.currentTimeMillis();
		// 调用sdk登陆方法登陆聊天服务器
		Log.d(TAG, "EMClient.getInstance().login");
		EMClient.getInstance().login(userJson.getString("hxid"),
				userJson.getString("hxpsw"), new EMCallBack() {

					@Override
					public void onSuccess() {
						Log.d(TAG, "login: onSuccess");

						if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
							pd.dismiss();
						}

						 
					
						// ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
						// ** manually load all local groups and
						EMClient.getInstance().groupManager().loadAllGroups();
						EMClient.getInstance().chatManager().loadAllConversations();

						// 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
						boolean updatenick = EMClient.getInstance()
								.updateCurrentUserNick(
										userJson.getString("nick"));
						if (!updatenick) {
							Log.e("LoginActivity", "update current user nick fail");
						}
						// 异步获取当前用户的昵称和头像(从自己服务器获取，demo使用的一个第三方服务)
						DemoHelper.getInstance().getUserProfileManager()
								.asyncGetCurrentUserInfo();

						// 进入主页面
						Intent intent = new Intent(LoginActivity.this,
								MainActivity.class);
						startActivity(intent);

						finish();
						
					}

					@Override
					public void onProgress(int progress, String status) {
						Log.d(TAG, "login: onProgress");
					}

					@Override
					public void onError(final int code, final String message) {
						Log.d(TAG, "login: onError: " + code);
						if (!progressShow) {
							return;
						}
						runOnUiThread(new Runnable() {
							public void run() {
								pd.dismiss();
								Toast.makeText(
										getApplicationContext(),
										getString(R.string.Login_failed)
												+ message, Toast.LENGTH_SHORT)
										.show();
							}
						});
					}
				});

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (autoLogin) {
			return;
		}
	}
}
