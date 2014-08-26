package com.example.chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * XMPP的工具类，定义了聊天所需的各种方法
 * 
 * @author lixiaosong
 * 
 */
public class XMPPChat {
	/**
	 * 要连接的端口号
	 */
	private int PORT = 5222;
	/**
	 * 远程服务器地址
	 */
	private String REMOTE_HOST = "182.92.187.129";
	/**
	 * 服务名
	 */
	private String SERVICE_NAME = "talk.joocola.com";
	/**
	 * Z 本类的单例对象
	 */
	private static XMPPChat chatService;
	/**
	 * XMPP的连接
	 */
	private XMPPConnection connection;
	/**
	 * 心跳服务，开启的话不断发送心跳包，需及时关闭
	 */
	private HeartService service;
	/**
	 * 文件接收与发送的管理器
	 */
	private FileTransferManager fileManager;

	/**
	 * 下面几个字段代表本用户当前的几个状态 ONLINE，QME，BUSY，LEAVE，INVISIBLE,OFFLINE
	 */
	public static final int ONLINE = 0;
	public static final int QME = 1;
	public static final int BUSY = 2;
	public static final int LEAVE = 3;
	public static final int INVISIBLE = 4;
	public static final int OFFLINE = 5;
	/**
	 * 代表注册完成的标志位，详细值请看注册方法
	 */
	public int flag1 = 0;
	/**
	 * 判断开启连接是否成功,0为默认，1为成功，2为失败
	 */
	public int flag2 = 0;
	/**
	 * 判断登录是否成功,0为默认，1为成功，2为失败
	 */
	public int flag3 = 0;
	/**
	 * 判断状态是否修改成功，当设置成在线时，说明离线消息也已经接收完成 0代表默认，1代表成功，2代表失败
	 */
	public int flag4 = 0;
	static {
		try {
			Class.forName("org.jivesoftware.smack.ReconnectionManager");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	synchronized public static XMPPChat getInstance() {
		if (chatService == null)
			chatService = new XMPPChat();
		return chatService;
	}

	private XMPPChat() {
		getConnection();
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(connection);
		FileTransferNegotiator.IBB_ONLY = false;
		if (sdm == null)
			sdm = ServiceDiscoveryManager.getInstanceFor(connection);
		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("jabber:iq:privacy");
		sdm.addFeature("jabber.org/protocol/si");
		fileManager = new FileTransferManager(connection);
	}

	public XMPPConnection getConnection() {
		if (connection == null)
			openConnection();
		return connection;
	}

	private boolean openConnection() {
		if (null == connection || !connection.isAuthenticated()) {
			try {
				ConnectionConfiguration config = new ConnectionConfiguration(
						REMOTE_HOST, PORT);
				XMPPConnection.DEBUG_ENABLED = true;
				config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
				config.setSendPresence(false); // 状态设为离线，目的为了取离线消息
				config.setReconnectionAllowed(true);
				config.setDebuggerEnabled(true);
				Log.v("lixiaosong", config.getServiceName());
				Log.v("lixiaosong", config.getHostAddresses().toString());
				connection = new XMPPConnection(config);
				connection.connect();
				configureConnection(ProviderManager.getInstance());
				flag2 = 1;
				return true;
			} catch (XMPPException e) {
				e.printStackTrace();
				flag2 = 2;
			}
			flag2 = 2;
			return false;
		}
		return false;
	}

	/**
	 * 
	 * 关闭连接
	 */
	public void closeConnection() {
		if (connection != null) {
			if (connection.isConnected())
				connection.disconnect();
			connection = null;
		}
	}

	/**
	 * 注册
	 * 
	 * @param account
	 *            注册的账号
	 * @param password
	 *            注册的密码
	 * @return 1注册成功，0服务器没有返回结果，2账号已经被使用，3注册失败
	 */
	public String register(String account, String password, String name) {
		if (connection == null) {
			flag1 = 0;
			return "0";
		}
		Registration registration = new Registration();
		registration.setType(IQ.Type.SET);
		registration.setUsername(account);
		registration.setPassword(password);
		registration.setTo(connection.getServiceName());
		registration.addAttribute("android", "geolo_createUser_android");
		registration.addAttribute("name", name);
		PacketFilter filter = new AndFilter(new PacketIDFilter(
				registration.getPacketID()), new PacketTypeFilter(IQ.class));
		PacketCollector collector = connection.createPacketCollector(filter);
		connection.sendPacket(registration);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		collector.cancel();
		if (result == null) {
			Log.v("lixiaosong", "服务器无响应");
			flag1 = 0;
			return "0";
		} else if (result.getType() == IQ.Type.RESULT) {
			Log.v("lixiaosong", "注册成功");
			flag1 = 1;
			return "1";
		} else if (result.getType() == IQ.Type.ERROR) {
			if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
				Log.v("lixiaosong", "该账号已被注册");
				flag1 = 2;
				return "2";
			} else {
				flag1 = 3;
				Log.v("lixiaosong", "注册失败");
				return "3";
			}
		}
		return "1";
	}

	/**
	 * 
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @return true代表登录成功，false代表登录失败
	 */
	public boolean login(String userName, String password) {
		if (connection != null) {
			try {
				connection.login(userName, password);
				flag3 = 1;
				return true;
			} catch (XMPPException e) {
				flag3 = 2;
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 修改用户状态
	 * 
	 * @param code
	 *            状态有ONLINE，QME，BUSY，LEAVE，INVISIBLE,OFFLINE
	 */
	public void setPresence(int code) {
		if (connection == null) {
			flag4 = 2;
			return;
		}

		Presence presence;
		switch (code) {
		case ONLINE:
			presence = new Presence(Presence.Type.available);
			connection.sendPacket(presence);
			Log.v("state", "设置在线");
			flag4 = 1;
			break;
		case QME:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.chat);
			connection.sendPacket(presence);
			Log.v("state", "设置Q我吧");
			System.out.println(presence.toXML());
			flag4 = 1;
			break;
		case BUSY:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.dnd);
			connection.sendPacket(presence);
			Log.v("state", "设置忙碌");
			System.out.println(presence.toXML());
			flag4 = 1;
			break;
		case LEAVE:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.away);
			connection.sendPacket(presence);
			Log.v("state", "设置离开");
			System.out.println(presence.toXML());
			flag4 = 1;
			break;
		case INVISIBLE:
			Roster roster = connection.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			for (RosterEntry entry : entries) {
				presence = new Presence(Presence.Type.unavailable);
				presence.setPacketID(Packet.ID_NOT_AVAILABLE);
				presence.setFrom(connection.getUser());
				presence.setTo(entry.getUser());
				connection.sendPacket(presence);
				System.out.println(presence.toXML());
			}
			// 向同一用户的其他客户端发送隐身状态
			presence = new Presence(Presence.Type.unavailable);
			presence.setPacketID(Packet.ID_NOT_AVAILABLE);
			presence.setFrom(connection.getUser());
			presence.setTo(StringUtils.parseBareAddress(connection.getUser()));
			connection.sendPacket(presence);
			Log.v("state", "设置隐身");
			flag4 = 1;
			break;
		case OFFLINE:
			presence = new Presence(Presence.Type.unavailable);
			connection.sendPacket(presence);
			Log.v("state", "设置离线");
			flag4 = 1;
			break;
		default:
			flag4 = 2;
			break;
		}
	}

	/**
	 * 加入providers的函数 ASmack在/META-INF缺少一个smack.providers 文件
	 * 
	 * @param pm
	 */
	public void configureConnection(ProviderManager pm) {
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());
		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());
		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient",
					"Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
	}

	/**
	 * 开启心跳服务
	 */
	public void startHeartService(Context context) {
		Intent service = new Intent(context, HeartService.class);
		context.startService(service);
	}

	public void stopHeartService(Context context) {
		context.stopService(new Intent(context, HeartService.class));
	}

	/**
	 * 发送文件给对方
	 * 
	 * @param user
	 *            对方用户名
	 * @param file
	 *            要发送的文件
	 * @throws XMPPException
	 * @throws InterruptedException
	 */
	public void sendFile(final String user, final File file,
			final SendFileCallBack callBack) throws XMPPException,
			InterruptedException {

		new AsyncTask<Void, Void, FileTransfer.Status>() {
			private OutgoingFileTransfer out;
			{
				String target = user
						+ "@"
						+ XMPPChat.getInstance().getConnection()
								.getServiceName() + "/Smack";
				out = fileManager.createOutgoingFileTransfer(target);
			}

			protected void onPreExecute() {
				try {
					out.sendFile(file, file.getName());
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			};

			@Override
			protected FileTransfer.Status doInBackground(Void... params) {
				while (!out.isDone()) {
					if (out.getStatus() == FileTransfer.Status.cancelled) {
						return FileTransfer.Status.cancelled;
					} else if (out.getStatus() == FileTransfer.Status.error) {
						return FileTransfer.Status.error;
					} else if (out.getStatus() == FileTransfer.Status.refused) {
						return FileTransfer.Status.refused;
					} else if (out.getStatus() == FileTransfer.Status.in_progress) {
						callBack.onProgress(out.getProgress());
					}

				}
				return FileTransfer.Status.complete;
			}

			protected void onPostExecute(FileTransfer.Status result) {
				callBack.onResult(file.getAbsolutePath(), result);
			};
		}.execute();

	}

	public void receiveFile(final Context context) {
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(connection);
		if (sdm == null)
			sdm = new ServiceDiscoveryManager(connection);
		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("jabber:iq:privacy");
		fileManager.addFileTransferListener(new FileTransferListener() {
			@Override
			public void fileTransferRequest(FileTransferRequest arg0) {
				final IncomingFileTransfer transfer = arg0.accept();
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					File dir = new File(Environment
							.getExternalStorageDirectory()
							+ File.separator
							+ "joocola_cache");
					if (!dir.exists())
						dir.mkdir();
					File file = new File(dir.getAbsolutePath()
							+ File.separator
							+ (System.currentTimeMillis() * (int) (Math
									.random() * 4 + 1)) + ".jpg");
					try {
						file.createNewFile();
						transfer.recieveFile(file);
						InputStream iStream = transfer.recieveFile();
						String line;
						BufferedReader br = new BufferedReader(new FileReader(
								file));
						while ((line = br.readLine()) != null)
							System.out.println(line);
						Log.v("lixiaosong",
								arg0.getDescription() + " "
										+ arg0.getRequestor());
						while (!transfer.isDone())
							;

					} catch (IOException e) {
						e.printStackTrace();
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				} else {

				}
			}
		});
	}

	public interface SendFileCallBack {
		public void onProgress(double progress);

		public void onResult(String filePath, FileTransfer.Status result);
	}
}
