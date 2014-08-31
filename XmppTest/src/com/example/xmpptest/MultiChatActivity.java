package com.example.xmpptest;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.chat.MultiChat;
import com.example.chat.XMPPChat;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class MultiChatActivity extends Activity {
	/**
	 * 给哪个房间发送消息
	 */
	@ViewInject(R.id.talkToWho_et)
	private EditText talkToWho_et;
	/**
	 * 给他发送消息的内容
	 */
	@ViewInject(R.id.whatToTalk_et)
	private EditText whatToTalk_et;
	/**
	 * 发送消息
	 */
	@ViewInject(R.id.send_btn)
	private Button send_btn;
	/**
	 * 显示内容
	 */
	@ViewInject(R.id.content_tv)
	private TextView content_tv;
	public static final String SINGLE_CHAT_ACTION = "single_chat";
	private MyReceiver receiver;
	public static final String MULTI_CHAT_ACTION = "multi_chat";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multi_chat);
		ViewUtils.inject(this);
		receiver = new MyReceiver();
		registerReceiver(receiver, new IntentFilter(MULTI_CHAT_ACTION));
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 当收到消息以后，更新消息即可
			content_tv.setText(XMPPTestApp.getSelf().getSingleInfo(
					intent.getStringExtra("user")));
			talkToWho_et.setText(intent.getStringExtra("user"));
		}
	}

	@OnClick(R.id.send_btn)
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send_btn:
			sendMessage();
			break;
		}
	}

	private void sendMessage() {
		if ((talkToWho_et != null && !talkToWho_et.equals(""))
				&& (whatToTalk_et != null && !whatToTalk_et.equals(""))) {
			MultiUserChat chat = MultiChat.getInstance()
					.joinMultiUserChat(
							XMPPChat.getInstance().getConnection().getUser()
									.split("@")[0],
							talkToWho_et.getText().toString(), "",
							talkToWho_et.getText().toString());
			try {
				chat.sendMessage(whatToTalk_et.getText().toString());
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		} else {

		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

}
