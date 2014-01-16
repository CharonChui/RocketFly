package com.charon.rocketfly;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class RocketActivity extends Activity {

	protected static final String TAG = "RocketActivity";
	private WindowManager mWindowManager;
	private int mWindowWidth;
	private int mWindowHeight;

	private ImageView iv_rocket;
	private ImageView iv_cloud;
	private ImageView iv_cloud_line;
	private RelativeLayout rl_cloud;

	private AnimationDrawable fireAnimationDrawable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "RocketActivity...onCreate");
		setContentView(R.layout.activity_rocket);
		mWindowManager = (WindowManager) this.getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
		mWindowWidth = displayMetrics.widthPixels;
		mWindowHeight = displayMetrics.heightPixels;

		findView();
		initView();
	}

	private void findView() {
		iv_rocket = (ImageView) findViewById(R.id.iv_rocket);
		iv_cloud = (ImageView) findViewById(R.id.iv_cloud);
		iv_cloud_line = (ImageView) findViewById(R.id.iv_cloud_line);
		rl_cloud = (RelativeLayout) findViewById(R.id.rl_cloud);
	}

	private void initView() {
		iv_rocket.setPressed(true);
		iv_rocket.setFocusable(true);
		iv_rocket.setVisibility(View.VISIBLE);
	}

	// @Override
	// public void onWindowFocusChanged(boolean hasFocus) {
	// super.onWindowFocusChanged(hasFocus);
	// iv_rocket.setBackgroundResource(R.drawable.rocket_fire);
	// fireAnimationDrawable = (AnimationDrawable) iv_rocket
	// .getBackground();
	// fireAnimationDrawable.start();
	//
	// fly();
	//
	// AlphaAnimation cloudAppearAnimation = new AlphaAnimation(0.1f, 1.0f);
	// cloudAppearAnimation.setDuration(500);
	//
	// Animation appearAnimation = new AlphaAnimation(0.1f, 1.0f);
	// appearAnimation.setDuration(500);
	// appearAnimation.setStartOffset(300);
	//
	// iv_cloud.startAnimation(cloudAppearAnimation);
	// iv_cloud_line.startAnimation(appearAnimation);
	// }

	@Override
	protected void onStart() {
		super.onStart();
		iv_rocket.setBackgroundResource(R.drawable.rocket_fire);
		fireAnimationDrawable = (AnimationDrawable) iv_rocket.getBackground();
		fireAnimationDrawable.start();

		fly();
	}

	/**
	 * 火箭飞起来的动画，同时下方播放冒烟的动画
	 */
	private void fly() {
		Log.e(TAG, "fly....");
		Animation animation = AnimationUtils.loadAnimation(
				this.getApplicationContext(), R.anim.rocket_up);
		animation.setFillAfter(true);

		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// 开始发射的时候去博凡动画
				MediaPlayer player = MediaPlayer.create(RocketActivity.this,
						R.raw.rocket);
				player.setLooping(false);
				player.setVolume(1.0f, 1.0f);
				player.start();

				AlphaAnimation cloudAppearAnimation = new AlphaAnimation(0.1f,
						1.0f);
				cloudAppearAnimation.setDuration(500);

				Animation appearAnimation = new AlphaAnimation(0.1f, 1.0f);
				appearAnimation.setDuration(500);
				appearAnimation.setStartOffset(300);

				iv_cloud.startAnimation(cloudAppearAnimation);
				iv_cloud_line.startAnimation(appearAnimation);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// 火箭播放完成后就去把云彩都消失
				removeClound();
				// finish();
			}
		});

		iv_rocket.startAnimation(animation);

		createClound();
	}

	private void createClound() {
		iv_cloud.setVisibility(View.VISIBLE);
		iv_cloud_line.setVisibility(View.VISIBLE);
	}

	private void removeClound() {
		AlphaAnimation disappearAnimation = new AlphaAnimation(1.0f, 0.0f);
		disappearAnimation.setDuration(1000);
		disappearAnimation.setFillAfter(true);
		disappearAnimation
				.setInterpolator(new AccelerateDecelerateInterpolator());

		disappearAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				iv_rocket.setVisibility(View.GONE);
				iv_cloud.setVisibility(View.GONE);
				iv_cloud_line.setVisibility(View.GONE);

				finish();
			}
		});
		rl_cloud.startAnimation(disappearAnimation);

		// iv_cloud.setVisibility(View.GONE);
		// iv_cloud_line.setVisibility(View.GONE);
	}
}
