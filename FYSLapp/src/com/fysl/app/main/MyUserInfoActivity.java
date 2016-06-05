package com.fysl.app.main;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.fysl.app.ui.BaseActivity;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class MyUserInfoActivity extends BaseActivity implements OnClickListener {

	final static String TAG = "MyUserInfoActivity.class";
	private ImageView iv_photo;
	private TextView tv_nick;
	private TextView tv_sex;
	private TextView tv_birth;
	private TextView tv_region;
	private TextView tv_sign;
	private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int PHOTO_REQUEST_CUT = 3;// 结果
	private static final int SET_AVATAR = 4;// 设置头像
	private static final int SET_SEX = 5;// 设置性别
	private static final int SET_BIRTH = 6;// 设置生日
	private static final int SET_REGION = 7;// 设置所在城市
	private static final int SET_SIGN = 8;// 设置个性签名

	public static final String TEMP_DIR = Environment
			.getExternalStorageDirectory().getPath().toString()
			+ "/fysl/";// 临时目录
	public static final String TEMP_DIR_CHECK = Environment
			.getExternalStorageDirectory().getPath().toString()
			+ "/fysl";
	private String imageName = "";
	private Bitmap bitmap = null;

	// 更新Ui
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case PHOTO_REQUEST_CUT:
				// Bitmap bitmap = (Bitmap) msg.obj;
				// iv_photo.setImageBitmap(bitmap);
				updateInfo("avatar", imageName, null, iv_photo);
				break;
			case SET_SEX:
				String _sex = (String) msg.obj;
				updateInfo("sex", _sex, tv_sex, null);

				// tv_sex.setText(_sex);
				break;
			case SET_BIRTH:
				String _birth = (String) msg.obj;
				updateInfo("birthday", _birth, tv_birth, null);

				// tv_birth.setText(_birth);
				break;
			case SET_REGION:
				String _region = (String) msg.obj;
				updateInfo("region", _region, tv_region, null);
				// tv_region.setText(_region);
				break;
			case SET_SIGN:

				String _sign = (String) msg.obj;
				updateInfo("sign", _sign, tv_sign, null);
				// tv_sign.setText(_sign);
				break;
			}

		}

	};

	private String province;
	private String city;
	private String sign;
	private final OkHttpClient client = new OkHttpClient();
	private static final MediaType MEDIA_TYPE_PNG = MediaType
			.parse("image/png");

	// 是否更新了资料
	private boolean IS_UPDATE = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myuserinfo);
		initView();
	}

	private void initView() {
		iv_photo = (ImageView) this.findViewById(R.id.iv_photo);
		tv_nick = (TextView) this.findViewById(R.id.tv_nick);
		tv_sex = (TextView) this.findViewById(R.id.tv_sex);
		tv_birth = (TextView) this.findViewById(R.id.tv_birth);
		tv_region = (TextView) this.findViewById(R.id.tv_region);
		tv_sign = (TextView) this.findViewById(R.id.tv_sign);

		JSONObject userJSON = DemoApplication.getInstance().getUserInfo();
		Glide.with(MyUserInfoActivity.this).load(userJSON.getString("avatar"))
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.fy_default_useravatar).into(iv_photo);
		tv_nick.setText(userJSON.getString("nick"));
		tv_sex.setText(userJSON.getString("sex"));
		tv_birth.setText(userJSON.getString("birthday"));
		tv_region.setText(userJSON.getString("province") + " "
				+ userJSON.getString("city"));
		tv_sign.setText(userJSON.getString("sign"));

		this.findViewById(R.id.re_sex).setOnClickListener(this);
		this.findViewById(R.id.re_birth).setOnClickListener(this);
		this.findViewById(R.id.re_region).setOnClickListener(this);
		this.findViewById(R.id.re_sign).setOnClickListener(this);
		this.findViewById(R.id.re_avatar).setOnClickListener(this);
		this.findViewById(R.id.re_nick).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.re_avatar:
			setAvatar();
			break;
		case R.id.re_nick:
			showNameAlert(tv_nick);
			break;
		case R.id.re_sex:
			setSex();

			break;
		case R.id.re_birth:
			setBirthday();
			break;
		case R.id.re_region:

			setRegion();

			break;
		case R.id.re_sign:
			setSign();
			break;

		}
	}

	private void updateInfo(final String key, final String value,
			final TextView changeView, final ImageView avatarView) {

		final ProgressDialog progressDialog = new ProgressDialog(this);

		progressDialog.setMessage("正在更新...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
		RequestBody formBody = null;
		if (key.equals("avatar")) {
			File file = new File(TEMP_DIR, imageName);
			if (file.exists()) {

				MultipartBuilder builder = new MultipartBuilder()
						.type(MultipartBuilder.FORM);
				builder.addFormDataPart("file", imageName,
						RequestBody.create(MEDIA_TYPE_PNG, file))
						.addFormDataPart("value", value)
						.addFormDataPart("key", key)
						.addFormDataPart("hxid",
								DemoHelper.getInstance().getCurrentUsernName());

				formBody = builder.build();

			}
		}

		else {
			// 请求体
			formBody = new FormEncodingBuilder()
					.add("key", key)
					.add("hxid", DemoHelper.getInstance().getCurrentUsernName())
					.add("value", value).build();
		}
		if (formBody == null) {
			Toast.makeText(getApplicationContext(), "头像更换出错",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Request request = new Request.Builder().url(Constant.URL_UPDATE)
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
						IS_UPDATE = true;
						// 返回自己的资料信息
						// 进行SDK登录后进入主页
						JSONObject userjson = json.getJSONObject("user");
						DemoApplication.getInstance().savaUserInfo(userjson);
						runOnUiThread(new Runnable() {

							@Override
							public void run() {

								if (!key.equals("avatar")) {

									changeView.setText(value);

								} else {

									avatarView.setImageBitmap(bitmap);

								}
							}

						});

					}

				}
			}

		});

	}

	private void setSign() {

		startActivityForResult(new Intent(MyUserInfoActivity.this,
				SignActivity.class), SET_SIGN);

	}

	private void setRegion() {

		startActivityForResult(new Intent(MyUserInfoActivity.this,
				RegionActivity.class), SET_REGION);
	}

	// 设置生日
	private void setBirthday() {
		OnDateSetListener callback = new OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {

				// 月份
				String month = String.valueOf(monthOfYear + 1);

				// 日/月 个位数的前面没有0，自己补上
				String day = String.valueOf(dayOfMonth);
				if (monthOfYear < 9) {
					month = "0" + month;
				}
				if (dayOfMonth < 10) {
					day = "0" + day;
				}
				// 全局变量保存设置的值-然后更新UI

				Message msg = handler.obtainMessage();
				msg.what = SET_BIRTH;
				msg.obj = String.valueOf(year) + "-" + month + "-" + day;// 以2016-04-01的样式显示
				msg.sendToTarget();

			}

		};
		DatePickerDialog datePicker = new DatePickerDialog(
				MyUserInfoActivity.this, callback, 1990, 4, 20);// context,监听回掉，年，月，日
		datePicker.show();

	}

	private void setSex() {

		showMyDialog("男", "女", SET_SEX);
	}

	// 设置头像
	private void setAvatar() {
		// 选择照片的dialog--相册或者拍照
		showMyDialog("相册", "拍照", SET_AVATAR);

	}

	// dialog---点击前往相册或者调用照相机，裁剪图片返回一个头像图片
	private void showMyDialog(String title1, String titel2, final int Type) {

		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.show();
		Window window = dlg.getWindow();
		// *** 主要就是在这里实现这种效果的.
		// 设置窗口的内容页面,fy_dialog_alert.xml文件中定义view内容
		window.setContentView(R.layout.fy_dialog_alert);
		// 为确认按钮添加事件,执行操作
		final TextView tv_content1 = (TextView) window
				.findViewById(R.id.tv_content1);
		tv_content1.setText(title1);

		tv_content1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (Type == SET_AVATAR) {
					getImage(PHOTO_REQUEST_GALLERY);
				} else if (Type == SET_SEX) {
					updateSex(tv_content1.getText().toString().trim());
				}

				dlg.cancel();
			}
		});
		final TextView tv_content2 = (TextView) window
				.findViewById(R.id.tv_content2);
		tv_content2.setText(titel2);
		tv_content2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (Type == SET_AVATAR) {
					getImage(PHOTO_REQUEST_TAKEPHOTO);
				} else if (Type == SET_SEX) {
					updateSex(tv_content2.getText().toString().trim());
				}
				dlg.cancel();
			}
		});

	}

	private void updateSex(String _sex) {
		// 临时保存设置的值--全局变量

		Message msg = handler.obtainMessage();
		msg.what = SET_SEX;
		msg.obj = _sex;
		msg.sendToTarget();

	}

	private void getImage(int type) {
		// 无论是相册还是拍照，先把预定的文件名确定好

		getImageName();
		if (!checkDir()) {

			Toast.makeText(getApplicationContext(), "找不到存储卡...",
					Toast.LENGTH_LONG).show();
			return;
		}
		if (type == PHOTO_REQUEST_TAKEPHOTO) {

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// 指定调用相机拍照后照片的储存路径

			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(TEMP_DIR, imageName)));
			startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
			return;
		}

		if (type == PHOTO_REQUEST_GALLERY) {

			Intent intent = new Intent(Intent.ACTION_PICK, null);
			intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					"image/*");
			startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
			return;
		}
	}

	private String getImageName() {

		// 用一个全局变量保存当前图片的文件名

		imageName = getNowTime() + ".PNG";

		return imageName;

	}

	// 检查目录
	private boolean checkDir() {

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			File dir = new File(TEMP_DIR_CHECK);

			if (!dir.exists()) {
				dir.mkdirs();
			}
			return true;
		} else {

			return false;
		}
	}

	// 以当前时间作为图片的文件名，精确到秒
	private String getNowTime() {

		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmssSS",
				Locale.getDefault());
		return dateFormat.format(date);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTO_REQUEST_TAKEPHOTO:

				startPhotoZoom(Uri.fromFile(new File(TEMP_DIR, imageName)), 240);
				break;

			case PHOTO_REQUEST_GALLERY:
				if (data != null)
					startPhotoZoom(data.getData(), 240);
				break;

			case PHOTO_REQUEST_CUT:
				// 至此已经得到一个头像的图片，先在本地更新
				bitmap = BitmapFactory.decodeFile(TEMP_DIR + imageName);
				Message msg = handler.obtainMessage();
				msg.what = PHOTO_REQUEST_CUT;
				msg.obj = bitmap;
				msg.sendToTarget();

				break;
			case SET_REGION:
				if (data != null) {
					province = data.getStringExtra("province");
					city = data.getStringExtra("city");
					Message msg2 = handler.obtainMessage();
					msg2.what = SET_REGION;
					msg2.obj = province + " " + city;
					msg2.sendToTarget();

				}

				break;

			case SET_SIGN:
				if (data != null) {
					sign = data.getStringExtra("sign");
					Message msg3 = handler.obtainMessage();
					msg3.what = SET_SIGN;
					msg3.obj = sign;
					msg3.sendToTarget();

				}

				break;
			}

		}

	}

	private void startPhotoZoom(Uri uri1, int size) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri1, "image/*");
		// crop为true是设置在开启的intent中设置显示的view可以剪裁
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		// outputX,outputY 是剪裁图片的宽高
		intent.putExtra("outputX", size);
		intent.putExtra("outputY", size);
		intent.putExtra("return-data", false);

		intent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(TEMP_DIR, imageName)));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

	private void showNameAlert(final TextView textView) {

		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.show();
		Window window = dlg.getWindow();
		// *** 主要就是在这里实现这种效果的.
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(R.layout.fy_alertdialog);
		// 设置能弹出输入法
		dlg.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		// 为确认按钮添加事件,执行退出应用操作
		Button ok = (Button) window.findViewById(R.id.btn_ok);
		TextView title = (TextView) window.findViewById(R.id.title);
		final EditText ed_name = (EditText) window.findViewById(R.id.ed_name);
		ed_name.setText(textView.getText().toString().trim());

		title.setText("修改昵称");

		ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final String newName = ed_name.getText().toString().trim();

				if (TextUtils.isEmpty(newName)
						|| newName.equals(textView.getText().toString().trim())) {
					dlg.cancel();
					return;
				}

				// tv_name.setText(newName);
				updateInfo("nick", newName, textView, null);
				dlg.cancel();
			}
		});
		// 关闭alert对话框架
		Button cancel = (Button) window.findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.cancel();
			}
		});

	}

	public void back(View view) {
		if (IS_UPDATE) {

			setResult(RESULT_OK);

		}
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (IS_UPDATE) {

				setResult(RESULT_OK);

			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
