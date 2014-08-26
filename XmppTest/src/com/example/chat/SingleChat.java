package com.example.chat;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;

/**
 * 单人会话类，负责创建，加入，保持单人会话,调用这个类时，必须事先调用XMPPChat类，建立好相应的连接，否则会报错
 * 
 * @author lixiaosong
 */
public class SingleChat {
	/**
	 * 单例类
	 */
	private static SingleChat singleChat;
	/**
	 * 管理单个聊天窗口
	 */
	private Map<String, Chat> chatList;

	synchronized public static SingleChat getInstance() {
		if (singleChat == null)
			singleChat = new SingleChat();
		return singleChat;
	}

	private SingleChat() {
		chatList = new HashMap<String, Chat>();
	}

	/**
	 * 获得与某个用户的单人会话,如果当前已经有该会话，则直接得到该会话
	 * 
	 */
	public Chat getFriendChat(String friendName, MessageListener listener) {
		XMPPConnection connection = XMPPChat.getInstance().getConnection();
		if (connection != null) {
			Chat chat = chatList.get(friendName);
			if (chat == null) {
				chat = connection.getChatManager().createChat(
						friendName + "@" + connection.getServiceName(),
						listener);
				chat.addMessageListener(listener);
				chatList.put(friendName, chat);
			}
			return chat;
		}
		return null;
	}

}
