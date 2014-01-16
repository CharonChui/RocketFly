package com.charon.rocketfly;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.charon.rocketfly.util.DenstyUtil;

public class MainActivity extends Activity {
	protected static final String TAG = "MainActivity";
	private WindowManager mWindowManager;
	private int mWindowWidth;
	private int mWindowHeight;
	private static View icon;
	private static View rocket_launcher;

	private static AnimationDrawable mFireAnimationDrawable;
	private static AnimationDrawable mLauncherAnimationDrawable;

	private WindowManager.LayoutParams iconParams;
	private WindowManager.LayoutParams launcherParams;

	private static int mLauncherHeight;
	private static int mLauncherWidth;

	private Vibrator mVibrator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWindowManager = (WindowManager) this.getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
		mWindowWidth = displayMetrics.widthPixels;
		mWindowHeight = displayMetrics.heightPixels;

		mVibrator = (Vibrator) this.getApplicationContext().getSystemService(
				Context.VIBRATOR_SERVICE);
	}

	public void start(View view) {
		createIcon();
		finish();
	}

	public void cancel(View view) {
		removeIcon();
		removeLauncher();
	}

	/**
	 * 创建桌面悬浮窗，一旦点击就变成小火箭
	 */
	public void createIcon() {
		removeIcon();
		iconParams = new LayoutParams();
		icon = new ImageView(this.getApplicationContext());
		icon.setBackgroundResource(R.drawable.floating_desktop_tips_rocket_bg);

		icon.setOnTouchListener(new OnTouchListener() {
			int startX;
			int startY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// 手指一旦点击了后，就要去创建小火箭，和下面的发射台，并且给小火箭播放动画

					startX = (int) event.getRawX();
					startY = (int) event.getRawY();

					icon.setBackgroundResource(R.drawable.rocket_fire);
					mFireAnimationDrawable = (AnimationDrawable) icon
							.getBackground();
					mFireAnimationDrawable.start();
					iconParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
					iconParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
					Log.d(TAG, "actin down change it to rocket" + "startX:"
							+ startX + "getX:" + (int) event.getX()
							+ "iconHeight:" + icon.getHeight()
							+ "paramsHeight:" + iconParams.height);
					iconParams.y = startY - icon.getHeight()*2;
					mWindowManager.updateViewLayout(icon, iconParams);

					createLauncher();

					break;
				case MotionEvent.ACTION_MOVE:

					Log.d(TAG, "action move change the location");
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();
					int dx = newX - startX;
					int dy = newY - startY;
					iconParams.x += dx;
					iconParams.y += dy;
					mWindowManager.updateViewLayout(icon, iconParams);
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					// 小火箭去移动位置
					isReadyToLaunch(newX, newY);

					break;

				case MotionEvent.ACTION_UP:
					// 手指抬起的时候，要么小火箭去发射，要么就是恢复到原来的提示图标那样
					Log.d(TAG, "action up");
					if (isReadyToLaunch((int) event.getRawX(),
							(int) event.getRawY())) {
						startActivity(new Intent(MainActivity.this
								.getApplicationContext(), RocketActivity.class));
						removeIcon();
						removeLauncher();
					} else {
						icon.setBackgroundResource(R.drawable.floating_desktop_tips_rocket_bg);
						createIcon();
					}

					break;
				}

				return true;
			}
		});

		iconParams.gravity = Gravity.LEFT | Gravity.TOP;
		iconParams.x = mWindowWidth;
		iconParams.y = mWindowHeight / 2;
		iconParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		iconParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		iconParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		iconParams.format = PixelFormat.TRANSLUCENT;
		iconParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;

		mWindowManager.addView(icon, iconParams);
	}

	private void removeIcon() {
		if (icon != null && icon.getParent() != null) {
			mWindowManager.removeView(icon);
		}
		removeLauncher();
	}

	/**
	 * 创建桌面发射台
	 */
	private void createLauncher() {
		removeLauncher();

		launcherParams = new LayoutParams();
		rocket_launcher = new ImageView(this.getApplicationContext());
		changelauncherState(false);

		launcherParams.height = (int) DenstyUtil.convertDpToPixel(80,
				this.getApplicationContext());
		launcherParams.width = (int) DenstyUtil.convertDpToPixel(200,
				this.getApplicationContext());
		mLauncherHeight = launcherParams.height;
		mLauncherWidth = launcherParams.width;

		// 这个x、y是起始添加的位置
		launcherParams.x = mWindowWidth / 2 - mLauncherWidth / 2;
		launcherParams.y = mWindowHeight - mLauncherHeight;
		launcherParams.gravity = Gravity.LEFT | Gravity.TOP;

		Log.d(TAG, "create launcher. width::" + rocket_launcher.getWidth());
		launcherParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		launcherParams.format = PixelFormat.TRANSLUCENT;
		launcherParams.type = WindowManager.LayoutParams.TYPE_TOAST;

		mWindowManager.addView(rocket_launcher, launcherParams);
	}

	private void removeLauncher() {
		if (rocket_launcher != null && rocket_launcher.getParent() != null) {
			mWindowManager.removeView(rocket_launcher);
		}
	}

	/**
	 * 更改发射台的状态
	 * 
	 * @param isReadFly
	 *            是否可以进入发射状态
	 */
	private void changelauncherState(boolean isReadFly) {
		if (rocket_launcher == null) {
			return;
		}

		if (isReadFly) {
			rocket_launcher.setBackgroundResource(R.drawable.desktop_bg_tips_3);
			if (mLauncherAnimationDrawable != null) {
				mLauncherAnimationDrawable.stop();
			}
		} else {
			rocket_launcher.setBackgroundResource(R.drawable.status_tip);

			// 创建发射台
			mLauncherAnimationDrawable = (AnimationDrawable) rocket_launcher
					.getBackground();
			if (!mLauncherAnimationDrawable.isRunning()) {
				mLauncherAnimationDrawable.start();
			}
		}
	}

	/**
	 * 判断是否可以进行发射
	 * 
	 * @param x
	 *            当前火箭的距x轴的距离
	 * @param y
	 *            当前火箭的距y轴的距离
	 * @return true为进入发射状态，反之为false
	 */
	private boolean isReadyToLaunch(int x, int y) {
		Log.e(TAG, "::y::" + y + "::launcherParams.y::" + launcherParams.y);
		if ((x > launcherParams.x && x < launcherParams.x
				+ launcherParams.width)
				&& (y > launcherParams.y)) {
			changelauncherState(true);
			Log.d(TAG, "is ready to launch.. true");
			mVibrator.vibrate(100);
			return true;
		}
		changelauncherState(false);
		return false;
	}
}
