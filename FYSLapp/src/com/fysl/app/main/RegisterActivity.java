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

import com.fysl.app.R;
import com.fysl.app.main.utils.SetTelCountTimer;
import com.fysl.app.ui.BaseActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 注册页
 * 
 */
public class RegisterActivity extends BaseActivity implements OnClickListener {

	private EditText et_usertel;
	private EditText et_code;
	private EditText et_password;
	private Button btn_register;
	private TextChange textChange;
	private Button btn_code;
	private SetTelCountTimer setTelCountTimer;
	private ImageView iv_hide;
	private ImageView iv_show;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initView();
	}

	private void initView() {

		et_usertel = (EditText) this.findViewById(R.id.et_usertel);
		et_code = (EditText) this.findViewById(R.id.et_code);
		et_password = (EditText) this.findViewById(R.id.et_password);
		btn_register = (Button) this.findViewById(R.id.btn_register);
		btn_code = (Button) this.findViewById(R.id.btn_code);
		iv_hide = (ImageView) this.findViewById(R.id.iv_hide);
		iv_show = (ImageView) this.findViewById(R.id.iv_show);

		// 同时监听多个EditText
		textChange = new TextChange();
		et_usertel.addTextChangedListener(textChange);
		et_code.addTextChangedListener(textChange);
		et_password.addTextChangedListener(textChange);

		// btn_code:点击后倒计时，且这中间不能点击，等时间结束，可以点击重新发送
		setTelCountTimer = new SetTelCountTimer(btn_code);
		// 设置点击监听

		btn_register.setOnClickListener(this);
		btn_code.setOnClickListener(this);
		iv_hide.setOnClickListener(this);
		iv_show.setOnClickListener(this);
	}

	class TextChange implements TextWatcher {

		@Override
		public void afterTextChanged(Editable arg0) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence cs, int start, int before,
				int count) {

			boolean Sign1 = et_usertel.getText().length() > 0;
			boolean Sign2 = et_code.getText().length() > 0;
			boolean Sign3 = et_password.getText().length() > 0;

			if (Sign1 & Sign2 & Sign3) {

				btn_register.setEnabled(true);
			} else {

				btn_register.setEnabled(false);
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_register:
			String tel = et_usertel.getText().toString().trim();
			String password = et_password.getText().toString().trim();
			// 验证手机号格式
			if (!isMobileNO(tel)) {

				Toast.makeText(RegisterActivity.this, "请输入正确的手机号码...",
						Toast.LENGTH_SHORT).show();
				return;
			}
			Intent intent = new Intent(RegisterActivity.this,
					RegisterNextActivity.class);
			intent.putExtra("tel", tel);
			intent.putExtra("password", password);
			startActivity(intent);

			break;
		case R.id.iv_show:
			showPassword(false);

			break;
		case R.id.iv_hide:
			//状态显示为灰色图标，表示点击后切换为显示亮图标
			showPassword(true);
			break;
		case R.id.btn_code:
			setTelCountTimer.start();
			break;

		}

	}

	private void showPassword(boolean isShow) {
		if (isShow) {
			et_password.setTransformationMethod(HideReturnsTransformationMethod
					.getInstance());
			iv_show.setVisibility(View.VISIBLE);
			iv_hide.setVisibility(View.GONE);
		} else {

			et_password.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
			iv_show.setVisibility(View.GONE);
			iv_hide.setVisibility(View.VISIBLE);
		}
		// 最后将光标移至字符串尾部

		CharSequence charSequence = et_password.getText();
		if (charSequence instanceof Spannable) {
			Spannable spanText = (Spannable) charSequence;
			Selection.setSelection(spanText, charSequence.length());
		}
	}

	/**
	 * 验证手机格式
	 */
	public boolean isMobileNO(String mobiles) {

		String telRegex = "[1][3578]\\d{9}";

		return mobiles.matches(telRegex);

	}
}
