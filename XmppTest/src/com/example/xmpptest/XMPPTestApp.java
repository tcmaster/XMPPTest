package com.example.xmpptest;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;

public class XMPPTestApp extends Application {
	/**
	 * 本APP的实例
	 */
	public static XMPPTestApp app;
	/**
	 * 临时存储单人聊天的数据
	 */
	private Map<String, StringBuilder> singleChatMap;
	/**
	 * 临时存储多人聊天的数据
	 */
	private Map<String, StringBuilder> multiChatMap;

	@Override
	public void onCreate() {
		super.onCreate();
		app = (XMPPTestApp) getApplicationContext();
		singleChatMap = new HashMap<String, StringBuilder>();
		multiChatMap = new HashMap<String, StringBuilder>();
	};

	public static XMPPTestApp getSelf() {
		return app;
	}

	public StringBuilder getSingleInfo(String user) {
		StringBuilder builder = singleChatMap.get(user);
		if (builder == null) {
			builder = new StringBuilder();
			singleChatMap.put(user, builder);
		}
		return builder;
	}

	public StringBuilder getMultiInfo(String user) {
		StringBuilder builder = multiChatMap.get(user);
		if (builder == null) {
			builder = new StringBuilder();
			multiChatMap.put(user, builder);
		}
		return builder;
	}
}
