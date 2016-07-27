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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.fysl.app.Constant;
import com.fysl.app.DemoApplication;
import com.fysl.app.DemoHelper;
import com.fysl.app.R;
import com.fysl.app.RefreshLocationService;
import com.fysl.app.db.DemoDBManager;
import com.fysl.app.main.utils.MD5Util;
import com.fysl.app.ui.BaseActivity;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class RegisterNextActivity extends BaseActivity implements
		OnClickListener {

	final static String TAG = "RegisterNextActivity.class";
	private ImageView iv_photo;
	private EditText et_usernick;
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
	// 更新Ui
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case PHOTO_REQUEST_CUT:
				Bitmap bitmap = (Bitmap) msg.obj;
				iv_photo.setImageBitmap(bitmap);
				break;
			case SET_SEX:
				String _sex = (String) msg.obj;
				tv_sex.setText(_sex);
				break;
			case SET_BIRTH:
				String _birth = (String) msg.obj;
				tv_birth.setText(_birth);
				break;
			case SET_REGION:
				String _region = (String) msg.obj;
				tv_region.setText(_region);
				break;
			case SET_SIGN:
				String _sign = (String) msg.obj;
				tv_sign.setText(_sign);
				break;
			}

		}

	};

	private String sex;
	private String birthday;
	private String province;
	private String city;
	private String sign;
	private String tel;
	private String password;
	private final OkHttpClient client = new OkHttpClient();
	private static final MediaType MEDIA_TYPE_PNG = MediaType
			.parse("image/png");
	private boolean progressShow = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_next);

		tel = this.getIntent().getStringExtra("tel");
		password = this.getIntent().getStringExtra("password");
		if (tel == null || password == null) {
			finish();
			return;
		}
		initView();
	}

	private void initView() {
		iv_photo = (ImageView) this.findViewById(R.id.iv_photo);
		et_usernick = (EditText) this.findViewById(R.id.et_usernick);
		tv_sex = (TextView) this.findViewById(R.id.tv_sex);
		tv_birth = (TextView) this.findViewById(R.id.tv_birth);
		tv_region = (TextView) this.findViewById(R.id.tv_region);
		tv_sign = (TextView) this.findViewById(R.id.tv_sign);

		iv_photo.setOnClickListener(this);
		this.findViewById(R.id.re_sex).setOnClickListener(this);
		this.findViewById(R.id.re_birth).setOnClickListener(this);
		this.findViewById(R.id.re_region).setOnClickListener(this);
		this.findViewById(R.id.re_sign).setOnClickListener(this);
		this.findViewById(R.id.btn_register).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.iv_photo:
			setAvatar();
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

		case R.id.btn_register:
			registerInServer();
			break;

		}
	}

	private void registerInServer() {
		// 先进行设置项的判断，然后进行上传
		String nickname = et_usernick.getText().toString().trim();
		if (TextUtils.isEmpty(nickname)) {
			Toast.makeText(getApplicationContext(), "请设置昵称", Toast.LENGTH_SHORT)
					.show();
			et_usernick.requestFocus();

			return;
		}
		if (TextUtils.isEmpty(sex)) {
			Toast.makeText(getApplicationContext(), "请设置性别", Toast.LENGTH_SHORT)
					.show();
			setSex();
			return;
		}
		if (TextUtils.isEmpty(birthday)) {
			Toast.makeText(getApplicationContext(), "请设置生日", Toast.LENGTH_SHORT)
					.show();
			setBirthday();
			return;
		}
		if (TextUtils.isEmpty(province) || TextUtils.isEmpty(city)) {
			Toast.makeText(getApplicationContext(), "请设置城市", Toast.LENGTH_SHORT)
					.show();
			setRegion();
			return;
		}
		if (TextUtils.isEmpty(sign)) {
			Toast.makeText(getApplicationContext(), "请设置签名", Toast.LENGTH_SHORT)
					.show();
			setSign();
			return;
		}

		final ProgressDialog progressDialog = new ProgressDialog(this);

		progressDialog.setMessage("正在注册...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();

		// 以上资料是否是强制设置的看作者自己决定，比如注册时头像不强制设置
		File file = new File(TEMP_DIR, imageName);
		String filename = "false";
		RequestBody formBody=null;
		if (file.exists()&&!TextUtils.isEmpty(imageName)) {
			Log.e("avatar---->>>", "avatar  exist");
			filename = imageName;

			MultipartBuilder builder = new MultipartBuilder()
					.type(MultipartBuilder.FORM);
			builder.addFormDataPart("file", filename,
					RequestBody.create(MEDIA_TYPE_PNG, file))
		           .addFormDataPart("image", filename)
					.addFormDataPart("nick", nickname)
					.addFormDataPart("sex", sex)
					.addFormDataPart("province", province)
					.addFormDataPart("city", city)
					.addFormDataPart("sign", sign)
					.addFormDataPart("birthday", birthday)

					.addFormDataPart("tel", tel)
					.addFormDataPart("password", MD5Util.getMD5String(password))

			;

			formBody = builder.build();

		} else {
			Log.e("avatar---->>>", "avatar not exist");
			// 请求体
			formBody = new FormEncodingBuilder()

			.add("nick", nickname).add("sex", sex).add("province", province)
					.add("city", city).add("sign", sign)
					.add("birthday", birthday).add("image", filename)
					.add("tel", tel)
					.add("password", MD5Util.getMD5String(password)).build();
		}
		if(formBody==null){
			
			Toast.makeText(getApplicationContext(), "请求体错误...",
					Toast.LENGTH_SHORT).show();
		}

		Request request = new Request.Builder().url(Constant.URL_REGISTER)
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
						// 返回自己的资料信息
						// 进行SDK登录后进入主页

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								login(userJson);
							}

						});
					}else if(json.getInteger("code") == 2000){

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
                                Toast.makeText(getApplicationContext(),"该手机号已经被注册...",Toast.LENGTH_SHORT).show();
							}

						});
					}else{

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(getApplicationContext(),"注册失败...",Toast.LENGTH_SHORT).show();
							}

						});
					}

				}
			}

		});

	}

	private void login(final JSONObject userJson) {

		progressShow = true;
		final ProgressDialog pd = new ProgressDialog(RegisterNextActivity.this);
		pd.setCanceledOnTouchOutside(false);
		pd.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				Log.d(TAG, "EMClient.getInstance().onCancel");
				progressShow = false;
			}
		});
		pd.setMessage(getString(R.string.Is_landing));
		pd.show();

		// After logout，the DemoDB may still be accessed due to async callback,
		// so the DemoDB will be re-opened again.
		// close it before login to make sure DemoDB not overlap
		DemoDBManager.getInstance().closeDB();

		// reset current user name before login
		DemoHelper.getInstance().setCurrentUserName(userJson.getString("hxid"));
		//即时更新位置
				startService(new Intent(RegisterNextActivity.this,RefreshLocationService.class));
		final long start = System.currentTimeMillis();
		// 调用sdk登陆方法登陆聊天服务器
		Log.d(TAG, "EMClient.getInstance().login");
		EMClient.getInstance().login(userJson.getString("hxid"),
				userJson.getString("hxpsw"), new EMCallBack() {

					@Override
					public void onSuccess() {
						Log.d(TAG, "login: onSuccess");

						if (!RegisterNextActivity.this.isFinishing()
								&& pd.isShowing()) {
							pd.dismiss();
						}

						// ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
						// ** manually load all local groups and
						EMClient.getInstance().groupManager().loadAllGroups();
						EMClient.getInstance().chatManager()
								.loadAllConversations();

						// 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
						boolean updatenick = EMClient.getInstance()
								.updateCurrentUserNick(
										userJson.getString("nick"));
						if (!updatenick) {
							Log.e("LoginActivity",
									"update current user nick fail");
						}
						// 异步获取当前用户的昵称和头像(从自己服务器获取，demo使用的一个第三方服务)
						// DemoHelper.getInstance().getUserProfileManager()
						// .asyncGetCurrentUserInfo();
						// 登录成功以后把本地用户的用户资料保存。
						DemoApplication.getInstance().savaUserInfo(userJson);
						// 进入主页面
						Intent intent = new Intent(RegisterNextActivity.this,
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

	private void setSign() {

		startActivityForResult(new Intent(RegisterNextActivity.this,
				SignActivity.class), SET_SIGN);

	}

	private void setRegion() {

		startActivityForResult(new Intent(RegisterNextActivity.this,
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
				birthday = String.valueOf(year) + month + day;
				Message msg = handler.obtainMessage();
				msg.what = SET_BIRTH;
				msg.obj = String.valueOf(year) + "-" + month + "-" + day;// 以2016-04-01的样式显示
				msg.sendToTarget();

			}

		};
		DatePickerDialog datePicker = new DatePickerDialog(
				RegisterNextActivity.this, callback, 1994, 4, 20);// context,监听回掉，年，月，日
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
		sex = _sex;
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
				Bitmap bitmap = BitmapFactory.decodeFile(TEMP_DIR + imageName);
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
}
