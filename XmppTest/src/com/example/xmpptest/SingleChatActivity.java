package com.example.xmpptest;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

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
import android.widget.Toast;

import com.example.chat.SingleChat;
import com.example.chat.XMPPChat;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/**
 * 单人聊天demo类
 * 
 * @author lixiaosong
 */
public class SingleChatActivity extends Activity {
	/**
	 * 给谁发送消息
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_chat);
		ViewUtils.inject(this);
		receiver = new MyReceiver();
		registerReceiver(receiver, new IntentFilter(SINGLE_CHAT_ACTION));
	}

	@OnClick(R.id.send_btn)
	public void OnClick(View v) {
		switch (v.getId()) {
		case R.id.send_btn:
			// 点击发送，处理发送逻辑
			processSend();
			break;
		default:
			break;
		}
	}

	public void processSend() {
		if ((talkToWho_et != null && !talkToWho_et.equals(""))
				&& (whatToTalk_et != null && !whatToTalk_et.equals(""))) {
			// 得到与聊天用户的会话
			Chat chat = SingleChat.getInstance().getFriendChat(
					talkToWho_et.getText().toString(), null);
			try {
				chat.sendMessage(whatToTalk_et.getText().toString());
				StringBuilder builder = XMPPTestApp.getSelf().getSingleInfo(
						talkToWho_et.getText().toString());
				builder.append(XMPPChat.getInstance().getConnection().getUser()
						.split("@")[0]
						+ " : " + whatToTalk_et.getText().toString() + "\n");
				whatToTalk_et.setText("");
				content_tv.setText(builder.toString());
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, "用户名/输入消息不能为空", Toast.LENGTH_SHORT).show();
		}
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

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

}
