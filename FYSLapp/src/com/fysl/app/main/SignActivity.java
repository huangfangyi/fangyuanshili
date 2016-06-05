package com.fysl.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fysl.app.R;
import com.fysl.app.ui.BaseActivity;

public class SignActivity extends BaseActivity {

	private EditText et_sign;
	private TextView tv_warning;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign);

		initView();
	}

	private void initView() {
		et_sign = (EditText) this.findViewById(R.id.et_sign);
		tv_warning = (TextView) this.findViewById(R.id.tv_warning);
		et_sign.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				int length = s.length();
				if (length > 50) {

					tv_warning.setVisibility(View.VISIBLE);
				} else {
					tv_warning.setVisibility(View.INVISIBLE);

				}

			}

			@Override
			public void afterTextChanged(Editable s) {

			}

		});

	}

	// 点击返回键，要返回当前输入框内容
	public void back(View view) {

		setBackString();

		finish();
	}

	private void setBackString() {
		String sign = et_sign.getText().toString();
		if (!(sign.length() > 50)) {

			Intent intent = new Intent();
			intent.putExtra("sign", sign);
			setResult(RESULT_OK, intent);

		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){
			
			setBackString();
			return true;
		}
		
		else
			return super.onKeyDown(keyCode, event);
	}
	
	
	

}
