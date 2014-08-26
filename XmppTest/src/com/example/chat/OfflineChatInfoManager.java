package com.example.chat;

import org.jivesoftware.smackx.OfflineMessageManager;

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

	}
}
