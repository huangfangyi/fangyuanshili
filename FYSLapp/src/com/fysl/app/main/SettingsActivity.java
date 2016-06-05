package com.fysl.app.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fysl.app.DemoHelper;
import com.fysl.app.DemoModel;
import com.fysl.app.R;
import com.fysl.app.ui.BaseActivity;
import com.fysl.app.ui.BlacklistActivity;
import com.fysl.app.ui.DiagnoseActivity;
import com.fysl.app.ui.OfflinePushNickActivity;
import com.fysl.app.ui.UserProfileActivity;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.widget.EaseSwitchButton;
 

public class SettingsActivity extends BaseActivity implements OnClickListener {
	
	/**
	 * 设置新消息通知布局
	 */
	private RelativeLayout rl_switch_notification;
	/**
	 * 设置声音布局
	 */
	private RelativeLayout rl_switch_sound;
	/**
	 * 设置震动布局
	 */
	private RelativeLayout rl_switch_vibrate;
	/**
	 * 设置扬声器布局
	 */
	private RelativeLayout rl_switch_speaker;


	/**
	 * 声音和震动中间的那条线
	 */
	private TextView textview1, textview2;

	private LinearLayout blacklistContainer;
	
	private LinearLayout userProfileContainer;
	
	/**
	 * 退出按钮
	 */
	private Button logoutBtn;

	private RelativeLayout rl_switch_chatroom_leave;
	
    private RelativeLayout rl_switch_delete_msg_when_exit_group;
    private RelativeLayout rl_switch_auto_accept_group_invitation;
 
	/**
	 * 诊断
	 */
	private LinearLayout llDiagnose;
	/**
	 * iOS离线推送昵称
	 */
	private LinearLayout pushNick;
	
    private EaseSwitchButton notifiSwitch;
    private EaseSwitchButton soundSwitch;
    private EaseSwitchButton vibrateSwitch;
    private EaseSwitchButton speakerSwitch;
    private EaseSwitchButton ownerLeaveSwitch;
    private EaseSwitchButton switch_delete_msg_when_exit_group;
    private EaseSwitchButton switch_auto_accept_group_invitation;
    private DemoModel settingsModel;
    private EMOptions chatOptions;
	
    @Override
	protected void onCreate(Bundle arg0) {
		
		super.onCreate(arg0);
		 
		setContentView(R.layout.em_fragment_conversation_settings);
		
		
		
		rl_switch_notification = (RelativeLayout) findViewById(R.id.rl_switch_notification);
		rl_switch_sound = (RelativeLayout) findViewById(R.id.rl_switch_sound);
		rl_switch_vibrate = (RelativeLayout) findViewById(R.id.rl_switch_vibrate);
		rl_switch_speaker = (RelativeLayout) findViewById(R.id.rl_switch_speaker);
		rl_switch_chatroom_leave = (RelativeLayout) findViewById(R.id.rl_switch_chatroom_owner_leave);
		rl_switch_delete_msg_when_exit_group = (RelativeLayout) findViewById(R.id.rl_switch_delete_msg_when_exit_group);
		rl_switch_auto_accept_group_invitation = (RelativeLayout) findViewById(R.id.rl_switch_auto_accept_group_invitation);
		
		notifiSwitch = (EaseSwitchButton) findViewById(R.id.switch_notification);
		soundSwitch = (EaseSwitchButton) findViewById(R.id.switch_sound);
		vibrateSwitch = (EaseSwitchButton) findViewById(R.id.switch_vibrate);
		speakerSwitch = (EaseSwitchButton) findViewById(R.id.switch_speaker);
		ownerLeaveSwitch = (EaseSwitchButton) findViewById(R.id.switch_owner_leave);
		switch_delete_msg_when_exit_group = (EaseSwitchButton) findViewById(R.id.switch_delete_msg_when_exit_group);
		switch_auto_accept_group_invitation = (EaseSwitchButton) findViewById(R.id.switch_auto_accept_group_invitation);
		
		
		logoutBtn = (Button) findViewById(R.id.btn_logout);
		if(!TextUtils.isEmpty(EMClient.getInstance().getCurrentUser())){
			logoutBtn.setText(getString(R.string.button_logout) );
		}

		textview1 = (TextView) findViewById(R.id.textview1);
		textview2 = (TextView) findViewById(R.id.textview2);
		
		blacklistContainer = (LinearLayout) findViewById(R.id.ll_black_list);
		userProfileContainer = (LinearLayout) findViewById(R.id.ll_user_profile);
		llDiagnose=(LinearLayout) findViewById(R.id.ll_diagnose);
		pushNick=(LinearLayout) findViewById(R.id.ll_set_push_nick);
		
		settingsModel = DemoHelper.getInstance().getModel();
		chatOptions = EMClient.getInstance().getOptions();
		
		blacklistContainer.setOnClickListener(this);
		userProfileContainer.setOnClickListener(this);
		rl_switch_notification.setOnClickListener(this);
		rl_switch_sound.setOnClickListener(this);
		rl_switch_vibrate.setOnClickListener(this);
		rl_switch_speaker.setOnClickListener(this);
		logoutBtn.setOnClickListener(this);
		llDiagnose.setOnClickListener(this);
		pushNick.setOnClickListener(this);
		rl_switch_chatroom_leave.setOnClickListener(this);
		rl_switch_delete_msg_when_exit_group.setOnClickListener(this);
		rl_switch_auto_accept_group_invitation.setOnClickListener(this);
		
		// 震动和声音总开关，来消息时，是否允许此开关打开
		// the vibrate and sound notification are allowed or not?
		if (settingsModel.getSettingMsgNotification()) {
			notifiSwitch.openSwitch();
		} else {
		    notifiSwitch.closeSwitch();
		}
		
		// 是否打开声音
		// sound notification is switched on or not?
		if (settingsModel.getSettingMsgSound()) {
		    soundSwitch.openSwitch();
		} else {
		    soundSwitch.closeSwitch();
		}
		
		// 是否打开震动
		// vibrate notification is switched on or not?
		if (settingsModel.getSettingMsgVibrate()) {
		    vibrateSwitch.openSwitch();
		} else {
		    vibrateSwitch.closeSwitch();
		}

		// 是否打开扬声器
		// the speaker is switched on or not?
		if (settingsModel.getSettingMsgSpeaker()) {
		    speakerSwitch.openSwitch();
		} else {
		    speakerSwitch.closeSwitch();
		}

		// 是否允许聊天室owner leave
		if(settingsModel.isChatroomOwnerLeaveAllowed()){
		    ownerLeaveSwitch.openSwitch();
		}else{
		    ownerLeaveSwitch.closeSwitch();
		}
		
		// delete messages when exit group?
		if(settingsModel.isDeleteMessagesAsExitGroup()){
		    switch_delete_msg_when_exit_group.openSwitch();
		} else {
		    switch_delete_msg_when_exit_group.closeSwitch();
		}
		
		if (settingsModel.isAutoAcceptGroupInvitation()) {
		    switch_auto_accept_group_invitation.openSwitch();
		} else {
		    switch_auto_accept_group_invitation.closeSwitch();
		}
		
//         this.findViewById(R.id.tv_changePSW).setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
// 				startActivity(new Intent(SettingsActivity.this,ChangePwdActivity.class));
//			}
//        	 
//         });
    }
    
    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_switch_notification:
			if (notifiSwitch.isSwitchOpen()) {
			    notifiSwitch.closeSwitch();
				rl_switch_sound.setVisibility(View.GONE);
				rl_switch_vibrate.setVisibility(View.GONE);
				textview1.setVisibility(View.GONE);
				textview2.setVisibility(View.GONE);

				settingsModel.setSettingMsgNotification(false);
			} else {
			    notifiSwitch.openSwitch();
				rl_switch_sound.setVisibility(View.VISIBLE);
				rl_switch_vibrate.setVisibility(View.VISIBLE);
				textview1.setVisibility(View.VISIBLE);
				textview2.setVisibility(View.VISIBLE);
				settingsModel.setSettingMsgNotification(true);
			}
			break;
		case R.id.rl_switch_sound:
			if (soundSwitch.isSwitchOpen()) {
			    soundSwitch.closeSwitch();
			    settingsModel.setSettingMsgSound(false);
			} else {
			    soundSwitch.openSwitch();
			    settingsModel.setSettingMsgSound(true);
			}
			break;
		case R.id.rl_switch_vibrate:
			if (vibrateSwitch.isSwitchOpen()) {
			    vibrateSwitch.closeSwitch();
			    settingsModel.setSettingMsgVibrate(false);
			} else {
			    vibrateSwitch.openSwitch();
			    settingsModel.setSettingMsgVibrate(true);
			}
			break;
		case R.id.rl_switch_speaker:
			if (speakerSwitch.isSwitchOpen()) {
			    speakerSwitch.closeSwitch();
			    settingsModel.setSettingMsgSpeaker(false);
			} else {
			    speakerSwitch.openSwitch();
			    settingsModel.setSettingMsgVibrate(true);
			}
			break;
		case R.id.rl_switch_chatroom_owner_leave:
		    if(ownerLeaveSwitch.isSwitchOpen()){
		        ownerLeaveSwitch.closeSwitch();
		        settingsModel.allowChatroomOwnerLeave(false);
		        chatOptions.allowChatroomOwnerLeave(false);
		    }else{
		        ownerLeaveSwitch.openSwitch();
		        settingsModel.allowChatroomOwnerLeave(true);
		        chatOptions.allowChatroomOwnerLeave(true);
		    }
		    break;
		case R.id.rl_switch_delete_msg_when_exit_group:
            if(switch_delete_msg_when_exit_group.isSwitchOpen()){
                switch_delete_msg_when_exit_group.closeSwitch();
                settingsModel.setDeleteMessagesAsExitGroup(false);
                chatOptions.setDeleteMessagesAsExitGroup(false);
            }else{
                switch_delete_msg_when_exit_group.openSwitch();
                settingsModel.setDeleteMessagesAsExitGroup(true);
                chatOptions.setDeleteMessagesAsExitGroup(true);
            }
		    break;
        case R.id.rl_switch_auto_accept_group_invitation:
            if(switch_auto_accept_group_invitation.isSwitchOpen()){
                switch_auto_accept_group_invitation.closeSwitch();
                settingsModel.setAutoAcceptGroupInvitation(false);
                chatOptions.setAutoAcceptGroupInvitation(false);
            }else{
                switch_auto_accept_group_invitation.openSwitch();
                settingsModel.setAutoAcceptGroupInvitation(true);
                chatOptions.setAutoAcceptGroupInvitation(true);
            }
            break;		    
		case R.id.btn_logout: //退出登陆
			logout();
			break;
		case R.id.ll_black_list:
			startActivity(new Intent(SettingsActivity.this, BlacklistActivity.class));
			break;
		case R.id.ll_diagnose:
			startActivity(new Intent(SettingsActivity.this, DiagnoseActivity.class));
			break;
		case R.id.ll_set_push_nick:
			startActivity(new Intent(SettingsActivity.this, OfflinePushNickActivity.class));
			break;
		case R.id.ll_user_profile:
			startActivity(new Intent(SettingsActivity.this, UserProfileActivity.class).putExtra("setting", true)
			        .putExtra("username", EMClient.getInstance().getCurrentUser()));
			break;
		default:
			break;
		}
		
	}

	void logout() {
		final ProgressDialog pd = new ProgressDialog(SettingsActivity.this);
		String st = getResources().getString(R.string.Are_logged_out);
		pd.setMessage(st);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		DemoHelper.getInstance().logout(false,new EMCallBack() {
			
			@Override
			public void onSuccess() {
				SettingsActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						pd.dismiss();
						// 重新显示登陆页面
					    finish();
						startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
						
					}
				});
			}
			
			@Override
			public void onProgress(int progress, String status) {
				
			}
			
			@Override
			public void onError(int code, String message) {
				SettingsActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						pd.dismiss();
						Toast.makeText(SettingsActivity.this, "unbind devicetokens failed", Toast.LENGTH_SHORT).show();
						
						
					}
				});
			}
		});
	}

}
