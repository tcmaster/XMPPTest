package com.example.xmpptest;

import org.jivesoftware.smack.XMPPConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chat.OfflineChatInfoManager;
import com.example.chat.UserChatListener;
import com.example.chat.XMPPChat;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/**
 * 欢迎来到xmpp的测试demo，这个是主注册，登录类
 * 
 * @author lixiaosong
 * 
 */
public class MainActivity extends Activity {
	/**
	 * 注册或登录的用户名
	 */
	@ViewInject(R.id.username)
	private EditText m_userName;
	/**
	 * 注册或登录的密码
	 */
	@ViewInject(R.id.password)
	private EditText m_password;
	/**
	 * 点击进入下一个界面的按钮
	 */
	@ViewInject(R.id.button)
	private Button m_registerOrLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewUtils.inject(this);
	}

	@OnClick(R.id.button)
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button:
			String userName = m_userName.getText().toString();
			String password = m_password.getText().toString();
			if ((userName != null && !userName.equals(""))
					&& (password != null && !password.equals(""))) {
				// 当有用户名和密码时，开启异步任务，连接服务器进行注册/登陆的操作
				new LoginTask(userName, password).execute();
			} else {
				Toast.makeText(MainActivity.this, "请输入用户名/密码",
						Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	private class LoginTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog dlg;
		private String userName;
		private String password;

		public LoginTask(String userName, String password) {
			dlg = new ProgressDialog(MainActivity.this);
			this.userName = userName;
			this.password = password;
		}

		@Override
		protected void onPreExecute() {
			dlg.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			XMPPChat.getInstance();
			return null;
		}

		@Override
		protected void onPostExecute(Void p) {
			dlg.dismiss();
			String result = XMPPChat.getInstance().register(userName, password,
					userName + ((int) (Math.random() * 115 + 1)));
			if (result.equals("0")) {
				Toast.makeText(MainActivity.this, "服务器出现问题，请重新尝试",
						Toast.LENGTH_SHORT).show();
			} else if (result.equals("1") || result.equals("2")) {
				// 这里说明注册成功或者已经有该账号，直接登录
				boolean loginResult = XMPPChat.getInstance().login(userName,
						password);
				if (loginResult) {
					// 得到离线消息
					OfflineChatInfoManager.getOfflineInfo();
					// 将账号设置为在线状态
					XMPPChat.getInstance().setPresence(XMPPChat.ONLINE);
					// 添加接收消息的监听器
					XMPPChat.getInstance().getConnection().getChatManager()
							.addChatListener(new UserChatListener());
					// 添加接收文件的监听器
					XMPPChat.getInstance().receiveFile(XMPPTestApp.getSelf());
					// 开启心跳服务
					XMPPChat.getInstance().startHeartService(
							XMPPTestApp.getSelf());
					// 跳转到我能做什么的页面
					startActivity(new Intent(MainActivity.this,
							WhatShouldIDo.class));
				} else {
					Toast.makeText(MainActivity.this, "登陆失败，请重新尝试",
							Toast.LENGTH_SHORT).show();
				}
			} else if (result.equals("3")) {
				Toast.makeText(MainActivity.this, "注册失败，请重新尝试",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onDestroy() {
		// 关闭聊天的服务
		XMPPConnection connection = XMPPChat.getInstance().getConnection();
		if (connection != null) {
			connection.disconnect();
			XMPPChat.getInstance().stopHeartService(this);
		}
		super.onDestroy();
	}
}
