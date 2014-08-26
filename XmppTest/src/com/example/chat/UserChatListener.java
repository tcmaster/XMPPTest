package com.example.chat;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.util.Log;

/**
 * 聊天信息接收监听器
 * 
 * @author lixiaosong
 * 
 */
public class UserChatListener implements ChatManagerListener {

	@Override
	public void chatCreated(Chat arg0, boolean arg1) {
		arg0.addMessageListener(new MessageListener() {

			@Override
			public void processMessage(Chat arg0, Message arg1) {
				Log.v("lixiaosong", arg1.getFrom());
				Log.v("lixiaosong", arg1.getTo());
				Log.v("lixiaosong", arg1.getBody());
			}
		});
	}

}
