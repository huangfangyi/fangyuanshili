package com.fysl.app.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.fysl.app.R;
import com.fysl.app.ui.BaseActivity;

public class FirstActivity extends BaseActivity implements OnClickListener {
	@Override
	protected void onCreate(Bundle arg0) {

		super.onCreate(arg0);
		setContentView(R.layout.activity_first);
		this.findViewById(R.id.btn_register).setOnClickListener(this);
		this.findViewById(R.id.btn_login).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btn_register:

			startActivity(new Intent(FirstActivity.this, RegisterActivity.class));
			break;
		case R.id.btn_login:
			startActivity(new Intent(FirstActivity.this, LoginActivity.class));
			break;
		}

	}
}
