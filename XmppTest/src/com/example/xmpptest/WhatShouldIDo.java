package com.example.xmpptest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

/**
 * XMPP demo功能演示类，告诉用户本demo将要演示的功能
 * 
 * @author lixiaosong
 * 
 */
public class WhatShouldIDo extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_what_should_ido);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.what_should_ido, menu);
		return true;
	}

}
