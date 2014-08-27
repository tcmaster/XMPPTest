package com.example.xmpptest;

import android.app.Application;

public class XMPPTestApp extends Application {
	public static XMPPTestApp app;

	@Override
	public void onCreate() {
		super.onCreate();
		app = (XMPPTestApp) getApplicationContext();
	};

	public static XMPPTestApp getSelf() {
		return app;
	}
}
