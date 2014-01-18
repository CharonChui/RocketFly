package com.charon.rocketfly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.charon.rocketfly.setvice.RocketService;

public class MainActivity extends Activity {
	protected static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void start(View view) {
		startService(new Intent(this, RocketService.class));
		finish();
	}

	public void cancel(View view) {
		stopService(new Intent(this, RocketService.class));
	}

}
