package com.example.chat;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.ping.PingManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 发送心跳包的服务
 * 
 * @author lixiaosong
 * 
 */
public class HeartService extends Service {
	/**
	 * 如果这个flag为true，则心跳包服务停止
	 */
	static boolean flag = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				XMPPConnection connection = XMPPChat.getInstance()
						.getConnection();
				while (!flag) {
					/**
					 * 每隔10秒钟，发送一个心跳包
					 */
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (connection != null && !flag) {
						Log.v("心跳包发送", "ping 一下");
						PingManager manager = PingManager
								.getInstanceFor(connection);
						manager.pingMyServer();
					}
				}
				XMPPChat.getInstance().closeConnection(HeartService.this);
				flag = false;
			}
		}).start();
		return super.onStartCommand(intent, flags, startId);
	}

	public void stopHeartService() {
		flag = true;
		stopSelf();
	}

	@Override
	public void onDestroy() {
		stopHeartService();
		super.onDestroy();
	}

}
