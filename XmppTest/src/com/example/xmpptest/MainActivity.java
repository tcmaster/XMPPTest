package com.example.xmpptest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	// 下面的IP地址是自己机器上的IP地址
	private final ConnectionConfiguration config = new ConnectionConfiguration(
			"192.168.0.101", 5222, "");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		login();

	}

	private void login() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.v("thread start", "ok");
				config.setSASLAuthenticationEnabled(false);
				config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
				XMPPConnection.DEBUG_ENABLED = true;
				XMPPConnection connection = new XMPPConnection(config);
				try {
					connection.connect();
					connection.login("test", "test");// 登陆

					// 这之前的代码是关于登陆的内容，此时如果成功的话就说明已经登陆到服务器了
					// 下面是即时聊天相关的内容
					System.out.println(connection.getUser());
					ChatManager chatmanager = connection.getChatManager();
					Chat newChat = chatmanager.createChat("test3@tcideapad",
							new MessageListener() {
								@Override
								public void processMessage(Chat arg0,
										Message arg1) {
									Log.v("test",
											"Received from 【" + arg1.getFrom()
													+ "】 message: "
													+ arg1.getBody());
								}
							});
					chatmanager.addChatListener(new ChatManagerListener() {

						@Override
						public void chatCreated(Chat arg0, boolean arg1) {
							arg0.addMessageListener(new MessageListener() {

								@Override
								public void processMessage(Chat arg0,
										Message arg1) {
									Log.v("test",
											"Received from 【" + arg1.getFrom()
													+ "】 message: "
													+ arg1.getBody());

								}
							});
						}
					});
					newChat.sendMessage("个人的一小步，世界的一大步");

					Roster roster = connection.getRoster();
					Collection<RosterEntry> entries = roster.getEntries();
					for (RosterEntry entry : entries) {
						System.out.print(entry.getName() + " - "
								+ entry.getUser() + " - " + entry.getType()
								+ " - " + entry.getGroups().size());
						Presence presence = roster.getPresence(entry.getUser());
						System.out.println(" - " + presence.getStatus() + " - "
								+ presence.getFrom());
					}

					// 添加花名册监听器，监听好友状态的改变。
					roster.addRosterListener(new RosterListener() {

						@Override
						public void entriesAdded(Collection<String> addresses) {
							System.out.println("entriesAdded");
						}

						@Override
						public void entriesUpdated(Collection<String> addresses) {
							System.out.println("entriesUpdated");
						}

						@Override
						public void entriesDeleted(Collection<String> addresses) {
							System.out.println("entriesDeleted");
						}

						@Override
						public void presenceChanged(Presence presence) {
							System.out.println("presenceChanged - >"
									+ presence.getStatus());
						}

					});
					for (RosterGroup g : roster.getGroups()) {
						for (RosterEntry entry : g.getEntries()) {
							System.out.println("Group " + g.getName() + " >> "
									+ entry.getName() + " - " + entry.getUser()
									+ " - " + entry.getType() + " - "
									+ entry.getGroups().size());
						}
					}

					// 发送消息
					BufferedReader cmdIn = new BufferedReader(
							new InputStreamReader(System.in));
					while (true) {
						try {
							String cmd = cmdIn.readLine();
							if ("!q".equalsIgnoreCase(cmd)) {
								break;
							}
							newChat.sendMessage(cmd);
						} catch (Exception ex) {
						}
					}
					connection.disconnect();
					System.exit(0);
				} catch (XMPPException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
