package com.fysl.app.main;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.fysl.app.DemoApplication;
import com.fysl.app.R;
import com.fysl.app.ui.BaseActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
 

public class MyQrActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrcode_generate);

		// Button button = (Button) findViewById(R.id.generate_button);
		// button.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// EditText editText = (EditText) findViewById(R.id.code_content);
	//	String groupId = this.getIntent().getStringExtra("groupId");
		String key="";
		String value="";
		 
			key="userInfo:";
			
			value=DemoApplication.getInstance().getUserInfo().toJSONString();
		 
		Bitmap qrcode = generateQRCode(key
				+value );
		ImageView imageView = (ImageView) findViewById(R.id.code_image);
		imageView.setImageBitmap(qrcode);
		// }
		// });

	}

	private Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
		int w = matrix.getWidth();
		int h = matrix.getHeight();
		int[] rawData = new int[w * h];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int color = Color.WHITE;
				if (matrix.get(i, j)) {
					color = Color.BLACK;
				}
				rawData[i + (j * w)] = color;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
		bitmap.setPixels(rawData, 0, w, 0, 0, w, h);
		return bitmap;
	}

	private Bitmap generateQRCode(String content) {
		try {
			QRCodeWriter writer = new QRCodeWriter();
			// MultiFormatWriter writer = new MultiFormatWriter();
			BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE,
					500, 500);
			return bitMatrix2Bitmap(matrix);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}
}