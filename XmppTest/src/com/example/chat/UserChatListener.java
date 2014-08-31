package com.example.chat;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.content.Intent;
import android.util.Log;

import com.example.xmpptest.SingleChatActivity;
import com.example.xmpptest.XMPPTestApp;

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
				// 将消息存入Builder中
				String user = arg1.getFrom().split("@")[0];
				StringBuilder builder = XMPPTestApp.getSelf().getSingleInfo(
						user);
				builder.append(user + " : " + arg1.getBody() + "\n");
				Intent intent = new Intent(
						SingleChatActivity.SINGLE_CHAT_ACTION);
				intent.putExtra("user", user);
				XMPPTestApp.getSelf().sendBroadcast(intent);
			}
		});
	}

}
