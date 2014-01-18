package com.charon.rocketfly.setvice;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
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

import com.charon.rocketfly.R;
import com.charon.rocketfly.RocketActivity;
import com.charon.rocketfly.util.DenstyUtil;

public class RocketService extends Service {
	protected static final String TAG = "RocketService";
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

	private Timer timer;

	private Handler mHandler = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mWindowManager = (WindowManager) this.getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
		mWindowWidth = displayMetrics.widthPixels;
		mWindowHeight = displayMetrics.heightPixels;

		mVibrator = (Vibrator) this.getApplicationContext().getSystemService(
				Context.VIBRATOR_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(TAG, "on start command create icon");
		createIcon();
		if (timer == null) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		removeIcon();
		removeLauncher();
		timer.cancel();
		timer = null;
		super.onDestroy();
	}

	/**
	 * 创建桌面悬浮窗，一旦点击就变成小火箭
	 */
	public void createIcon() {
		removeIcon();
		iconParams = new LayoutParams();
		icon = new ImageView(this.getApplicationContext());
		Log.e(TAG, "creat icon is not null");
		icon.setBackgroundResource(R.drawable.floating_desktop_tips_rocket_bg);

		icon.setOnTouchListener(new OnTouchListener() {
			float startX = 0;
			float startY = 0;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// 手指一旦点击了后，就要去创建小火箭，和下面的发射台，并且给小火箭播放动画
					startX = event.getX();
					startY = event.getY();

					icon.setBackgroundResource(R.drawable.rocket_fire);
					mFireAnimationDrawable = (AnimationDrawable) icon
							.getBackground();
					mFireAnimationDrawable.start();
					iconParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
					iconParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
					iconParams.y = iconParams.y - iconParams.height
							- icon.getHeight() * 2;

					mWindowManager.updateViewLayout(icon, iconParams);

					createLauncher();

					break;
				case MotionEvent.ACTION_MOVE:

					Log.d(TAG, "action move change the location");
					float newX = event.getRawX();
					Log.e(TAG, "iconHeight:" + icon.getHeight() + ":::"
							+ iconParams.height);
					float newY = event.getRawY() - icon.getHeight()
							- iconParams.height;
					mWindowManager.updateViewLayout(icon, iconParams);

					iconParams.x = (int) (newX - startX);
					iconParams.y = (int) (newY - startY);

					// 小火箭去移动位置
					isReadyToLaunch(event.getRawX(), event.getRawY());

					break;

				case MotionEvent.ACTION_UP:
					// 手指抬起的时候，要么小火箭去发射，要么就是恢复到原来的提示图标那样
					Log.d(TAG, "action up");
					if (isReadyToLaunch((int) event.getRawX(),
							(int) event.getRawY())) {
						Intent intent = new Intent(RocketService.this,
								RocketActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						removeIcon();
						removeLauncher();
					} else {
						Log.e(TAG, "action up create icon.");
						createIcon();
						icon.setBackgroundResource(R.drawable.floating_desktop_tips_rocket_bg);
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
			icon = null;
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
	private boolean isReadyToLaunch(float x, float y) {
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

	/**
	 * 判断当前界面是否是桌面
	 */
	private boolean isHome() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		return getHomes().contains(rti.get(0).topActivity.getPackageName());
	}

	/**
	 * 获得属于桌面的应用的应用包名称
	 * 
	 * @return 返回包含所有包名的字符串列表
	 */
	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.packageName);
		}
		return names;
	}

	private class RefreshTask extends TimerTask {

		@Override
		public void run() {
			// 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
			if (isHome()) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (icon == null) {
							Log.e(TAG,
									"refresh task create icon, and the icon is null");
							createIcon();
						}
					}
				});

			}
			// 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
			else if (!isHome()) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						removeIcon();
					}
				});
			}
		}

	}

}
