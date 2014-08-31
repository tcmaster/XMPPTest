package com.example.xmpptest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/**
 * XMPP demo功能演示类，告诉用户本demo将要演示的功能
 * 
 * @author lixiaosong
 * 
 */
public class WhatShouldIDo extends Activity {
	/**
	 * 点击该按钮进入单人聊天
	 */
	@ViewInject(R.id.singleChat)
	private Button m_singleChat_btn;
	/**
	 * 点击该按钮进入多人聊天
	 */
	@ViewInject(R.id.multiChat)
	private Button m_multiChat_btn;
	/**
	 * 点击进入图片传输
	 */
	@ViewInject(R.id.transferFile)
	private Button m_fileTransfer_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_what_should_ido);
		ViewUtils.inject(this);
	}

	@OnClick({ R.id.singleChat, R.id.multiChat, R.id.transferFile })
	public void onClick(View v) {
		Class clazz = null;
		switch (v.getId()) {
		case R.id.singleChat:
			// 单人聊天类
			clazz = SingleChatActivity.class;
			break;
		case R.id.multiChat:
			// 多人聊天类
			clazz = MultiChatActivity.class;
			break;
		case R.id.transferFile:
			// 文件传输类
			clazz = FileTransferDemo.class;
			break;
		}
		jumpToOtherInterface(clazz);
	}

	/**
	 * 跳转到相关界面
	 * 
	 * @param myClass
	 *            要跳转到的界面的class
	 */
	private <T> void jumpToOtherInterface(Class<T> myClass) {
		if (myClass != null) {
			Intent intent = new Intent(this, myClass);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		}
	}
}
