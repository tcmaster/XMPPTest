package com.example.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.DelayInformation;

/**
 * 多人会话类，负责创建，加入，保持多人会话
 * 
 * @author lixiaosong
 */
public class MultiChat {
	public static MultiChat myChat;
	public Map<String, MultiUserChat> chatMap;

	public static MultiChat getInstance() {
		if (myChat == null)
			myChat = new MultiChat();
		return myChat;
	}

	private MultiChat() {
		chatMap = new HashMap<String, MultiUserChat>();
	}

	/**
	 * 创建会议室
	 * 
	 * @param user
	 *            用户名
	 * @param roomName
	 *            房间名
	 * @param password
	 *            密码
	 * @return
	 */
	public MultiUserChat createRoom(String user, String roomName,
			String password) {
		MultiUserChat muc = null;
		try {
			muc.create(roomName);
			muc = new MultiUserChat(XMPPChat.getInstance().getConnection(),
					roomName
							+ "@conference."
							+ XMPPChat.getInstance().getConnection()
									.getServiceName());
			Form form = muc.getConfigurationForm();
			Form submitForm = form.createAnswerForm();
			for (Iterator<FormField> fields = form.getFields(); fields
					.hasNext();) {
				FormField field = (FormField) fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType())
						&& field.getVariable() != null) {
					// 设置默认值作为答复
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			List<String> owners = new ArrayList<String>();
			owners.add(XMPPChat.getInstance().getConnection().getUser());// 用户JID
			submitForm.setAnswer("muc#roomconfig_roomowners", owners);
			// 设置聊天室是持久聊天室，即将要被保存下来
			submitForm.setAnswer("muc#roomconfig_persistentroom", true);
			// 房间仅对成员开放
			submitForm.setAnswer("muc#roomconfig_membersonly", false);
			// 允许占有者邀请其他人
			submitForm.setAnswer("muc#roomconfig_allowinvites", true);
			if (!password.equals("")) {
				// 进入是否需要密码
				submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
						true);
				// 设置进入密码
				submitForm.setAnswer("muc#roomconfig_roomsecret", password);
			}
			// 能够发现占有者真实 JID 的角色
			// submitForm.setAnswer("muc#roomconfig_whois", "anyone");
			// 登录房间对话
			submitForm.setAnswer("muc#roomconfig_enablelogging", true);
			// 仅允许注册的昵称登录
			submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
			// 允许使用者修改昵称
			submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
			// 允许用户注册房间
			submitForm.setAnswer("x-muc#roomconfig_registration", false);
			// 发送已完成的表单（有默认值）到服务器来配置聊天室
			muc.sendConfigurationForm(submitForm);
		} catch (XMPPException e) {
			e.printStackTrace();
			return null;
		}
		return muc;

	}

	/**
	 * 加入会议室
	 * 
	 * @param user
	 *            昵称
	 * @param password
	 *            会议室密码
	 * @param roomsName
	 *            会议室名
	 */
	public MultiUserChat joinMultiUserChat(String user, String roomsName,
			String password, String key) {
		MultiUserChat muc = null;
		try {
			muc = chatMap.get(roomsName);
			if (muc == null) {
				muc = new MultiUserChat(XMPPChat.getInstance().getConnection(),
						roomsName
								+ "@conference."
								+ XMPPChat.getInstance().getConnection()
										.getServiceName());
				muc.addMessageListener(new MultiChatListener(key));
				chatMap.put(roomsName, muc);
				// 聊天室服务将会决定要接受的历史记录数量，暂定为20条
				DiscussionHistory history = new DiscussionHistory();
				history.setMaxChars(60);
				// history.setSince(new Date());
				// 用户加入聊天室
				muc.join(user, password, history,
						SmackConfiguration.getPacketReplyTimeout());
			}
			return muc;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return null;
	}

	public class MultiChatListener implements PacketListener {
		private String key;

		/**
		 * 
		 * @param key
		 *            标注该聊天是哪组多人聊天
		 */
		public MultiChatListener(String key) {
			this.key = key;
		}

		@Override
		public void processPacket(Packet arg0) {
			Message message = (Message) arg0;
			DelayInformation delay = (DelayInformation) message.getExtension(
					"x", "jabber:x:delay");
		}
	}
}
