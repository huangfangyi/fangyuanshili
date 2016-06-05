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
package com.fysl.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;

import com.hyphenate.easeui.EaseConstant;

public class Constant extends EaseConstant {
	public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
	public static final String GROUP_USERNAME = "item_groups";
	public static final String CHAT_ROOM = "item_chatroom";
	public static final String ACCOUNT_REMOVED = "account_removed";
	public static final String ACCOUNT_CONFLICT = "conflict";
	public static final String CHAT_ROBOT = "item_robots";
	public static final String MESSAGE_ATTR_ROBOT_MSGTYPE = "msgtype";
	public static final String ACTION_GROUP_CHANAGED = "action_group_changed";
	public static final String ACTION_CONTACT_CHANAGED = "action_contact_changed";

	// 去除BOM头
	public static final String jsonTokener(String in) {
		// consume an optional byte order mark (BOM) if it exists
		if (in != null && in.startsWith("\ufeff")) {
			in = in.substring(1);
		}
		return in;
	}

	// 服务器相关
	public static final String API_HOST = "http://120.24.211.126/fysl";
	public static final String URL_REGISTER = API_HOST + "/register.php";
	public static final String URL_UPDATE = API_HOST + "/update.php";
	public static final String URL_UPDATE_LOCATION = API_HOST
			+ "/update_location.php";
	public static final String URL_SINGLE_INFO = API_HOST + "/get_userinfo.php";
	public static final String URL_PUBLISH = API_HOST + "/publish.php";
	public static final String URL_GET_PEOPLE = API_HOST + "/get_people.php";
	public static final String URL_GET_SOCIAL = API_HOST + "/get_social.php";
	public static final String URL_GET_MY_SOCIAL = API_HOST + "/get_my_social.php";
	public static final String URL_SOCIAL_PHOTO = "http://120.24.211.126/fysl/upload/";
	public static final String URL_GET_FRIENDS = API_HOST
			+ "/get_allfriends.php";
	public static final String URL_GET_USERINFO = API_HOST
			+ "/get_userinfo.php";
	public static final String URL_SEARCH_USER = API_HOST
			+ "/search_friends.php";
	
	public static final String URL_LOGIN = API_HOST
			+ "/login.php";
	
	// 圆角处理
	public static Bitmap toRoundCorner(Bitmap bitmap, float ratio) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, bitmap.getWidth() / ratio,
				bitmap.getHeight() / ratio, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;

	}
}
