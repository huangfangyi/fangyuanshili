package com.fysl.app.main.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.fysl.app.Constant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.util.HanziToPinyin;

@SuppressLint("DefaultLocale")
public class JSON2User {

	@SuppressLint("DefaultLocale")
	public static EaseUser getUser(JSONObject json) {
		EaseUser user = new EaseUser(json.getString("hxid"));
		user.setNick(json.getString("nick"));
		user.setAvatar(json.getString("avatar"));
		user.setUserInfo(json.toJSONString());
		String headerName = null;
		if (!TextUtils.isEmpty(user.getNick())) {
			headerName = user.getNick().trim();
		} else {
			headerName = user.getUsername().trim();
		}

		if (user.getUsername().equals(Constant.NEW_FRIENDS_USERNAME)
				|| user.getUsername().equals(Constant.GROUP_USERNAME)
				|| user.getUsername().equals(Constant.CHAT_ROOM)
				|| user.getUsername().equals(Constant.CHAT_ROBOT)) {
			user.setInitialLetter("");
		} else if (Character.isDigit(headerName.charAt(0))) {
			user.setInitialLetter("#");
		} else {
			user.setInitialLetter(HanziToPinyin.getInstance()
					.get(headerName.substring(0, 1)).get(0).target.substring(0,
					1).toUpperCase());
			char header = user.getInitialLetter().toLowerCase().charAt(0);
			if (header < 'a' || header > 'z') {
				user.setInitialLetter("#");
			}
		}

		return user;

	}

	public static JSONObject getJson(EaseUser user) {

		JSONObject json = new JSONObject();
		String userInfo = user.getUserInfo();
		if (!TextUtils.isEmpty(userInfo)) {

			json = JSONObject.parseObject(userInfo);
		}
		return json;
	}

}
