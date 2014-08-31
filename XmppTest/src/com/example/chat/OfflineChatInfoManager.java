package com.example.chat;

import java.util.Iterator;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.packet.DelayInformation;

import com.example.xmpptest.XMPPTestApp;

/**
 * 离线消息的管理类，负责获取当前用户的离线消息，将离线消息存入数据库，待日后使用
 * 
 * @author lixiaosong
 * 
 */
public class OfflineChatInfoManager {
	/**
	 * 得到离线消息
	 */
	public static void getOfflineInfo() {
		OfflineMessageManager manager = new OfflineMessageManager(XMPPChat
				.getInstance().getConnection());
		try {
			Iterator<Message> it = manager.getMessages();
			while (it.hasNext()) {
				Message msg = it.next();
				String user = msg.getFrom().split("@")[0];
				String body = msg.getBody();
				DelayInformation info = (DelayInformation) msg.getExtension(
						"x", "jabber:x:delay");
				String delayTime = "";
				if (info != null)
					delayTime = info.getStamp().toLocaleString();
				StringBuilder builder = XMPPTestApp.getSelf().getSingleInfo(
						user);
				builder.append(user + " : " + body + " t :" + delayTime + "\n");
			}
			manager.deleteMessages();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
}
