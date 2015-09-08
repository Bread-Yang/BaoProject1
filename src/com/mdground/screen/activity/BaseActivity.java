package com.mdground.screen.activity;

import android.support.v4.app.FragmentActivity;

import com.mdground.screen.utils.AppManager;

public class BaseActivity extends FragmentActivity {

	protected void onCreate(android.os.Bundle arg0) {
		super.onCreate(arg0);
		AppManager.getAppManager().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().finishActivity(this);
	}

}
