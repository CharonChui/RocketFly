package com.charon.rocketfly;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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

	private MediaPlayer mMediaPlayer;

	private Vibrator mVibrator;

	private static View clound;
	private static View clound_line;

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
		removeClound();
	}

	private void createIcon() {
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
					Log.d(TAG, "actin down change it to rocket");
					icon.setBackgroundResource(R.drawable.rocket_fire);
					mFireAnimationDrawable = (AnimationDrawable) icon
							.getBackground();
					mFireAnimationDrawable.start();

					createLauncher();

					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
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
					isReadyToLaunch(iconParams.x, iconParams.y);

					break;

				case MotionEvent.ACTION_UP:
					// 手指抬起的时候，要么小火箭去发射，要么就是恢复到原来的提示图标那样
					Log.d(TAG, "action up");
					if (isReadyToLaunch(iconParams.x, iconParams.y)) {
						fly();
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
		iconParams.windowAnimations = android.R.anim.accelerate_interpolator;

		mWindowManager.addView(icon, iconParams);
	}

	private void removeIcon() {
		if (icon != null && icon.getParent() != null) {
			mWindowManager.removeView(icon);
		}
		removeLauncher();
	}

	private void createLauncher() {
		removeLauncher();
		removeClound();

		launcherParams = new LayoutParams();
		rocket_launcher = new ImageView(this.getApplicationContext());
		changelauncherState(false);

		launcherParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		Log.d(TAG, "create launcher. width::" + rocket_launcher.getWidth());
		launcherParams.height = (int) DenstyUtil.convertDpToPixel(80,
				this.getApplicationContext());
		launcherParams.width = (int) DenstyUtil.convertDpToPixel(200,
				this.getApplicationContext());
		launcherParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		launcherParams.format = PixelFormat.TRANSLUCENT;
		launcherParams.type = WindowManager.LayoutParams.TYPE_TOAST;

		mLauncherHeight = launcherParams.height;
		mLauncherWidth = launcherParams.width;

		mWindowManager.addView(rocket_launcher, launcherParams);
	}

	private void removeLauncher() {
		if (rocket_launcher != null && rocket_launcher.getParent() != null) {
			mWindowManager.removeView(rocket_launcher);
		}
	}

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

	private boolean isReadyToLaunch(int x, int y) {
		int minWidth = mWindowWidth / 2 - mLauncherWidth / 2;
		int maxWidth = mWindowWidth - (mWindowWidth / 2 - mLauncherWidth / 2);
		if ((x > minWidth && x < maxWidth)
				&& (y > mWindowHeight
						- mLauncherHeight
						- getStatusBarHeight(MainActivity.this
								.getApplicationContext()))) {
			changelauncherState(true);
			Log.d(TAG, "is ready to launch.. true");
			mVibrator.vibrate(300);
			return true;
		} else {
			changelauncherState(false);
			Log.d(TAG, "is ready to launch.. false");
			return false;
		}
	}

	/**
	 * 火箭飞起来的动画，同时下方播放冒烟的动画
	 */
	private void fly() {
		Log.e(TAG, "fly....");
		// Animation animation = AnimationUtils.loadAnimation(
		// this.getApplicationContext(), R.anim.rocket_up);
		//
		// Animation disappearAnimation = new AlphaAnimation(1.0f, 0.0f);
		// disappearAnimation.setDuration(1000);
		// disappearAnimation.setFillAfter(true);
		//
		// animation.setAnimationListener(new AnimationListener() {
		//
		// @Override
		// public void onAnimationStart(Animation animation) {
		// // 开始发射的时候去博凡动画
		// mMediaPlayer = MediaPlayer.create(MainActivity.this,
		// R.raw.rocket);
		// mMediaPlayer.start();
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationEnd(Animation animation) {
		// // 火箭播放完成后就去把云彩都消失
		// mMediaPlayer.stop();
		// mMediaPlayer.release();
		// }
		// });
		//
		// icon.startAnimation(animation);

		new LaunchTask().execute();
		createClound();
	}

	private void createClound() {
		removeClound();
		launcherParams = new LayoutParams();
		clound = new ImageView(this.getApplicationContext());
		clound.setBackgroundResource(R.drawable.desktop_smoke_m);
		launcherParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		Log.d(TAG, "create launcher. width::" + rocket_launcher.getWidth());
		launcherParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		launcherParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		launcherParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		launcherParams.format = PixelFormat.TRANSLUCENT;
		launcherParams.type = WindowManager.LayoutParams.TYPE_TOAST;

		mWindowManager.addView(clound, launcherParams);
	}

	private void removeClound() {
		if (clound != null && clound.getParent() != null) {
			mWindowManager.removeView(clound);
		}
	}

	/**
	 * 开始执行发射小火箭的任务。
	 */
	class LaunchTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// 在这里对小火箭的位置进行改变，从而产生火箭升空的效果
			while (iconParams.y > 0) {
				iconParams.y = iconParams.y - 10;
				publishProgress();
				try {
					Thread.sleep(8);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			mWindowManager.updateViewLayout(icon, iconParams);
		}

		@Override
		protected void onPostExecute(Void result) {
			// 火箭升空结束后，回归到悬浮窗状态
			// updateViewStatus();
			// mParams.x = (int) (xDownInScreen - xInView);
			// mParams.y = (int) (yDownInScreen - yInView);
			// windowManager.updateViewLayout(FloatWindowSmallView.this,
			// mParams);
			
//			Animation disappearAnimation = new AlphaAnimation(1.0f, 0.0f);
//			disappearAnimation.setDuration(1000);
//			disappearAnimation.setFillAfter(true);
//			clound.startAnimation(disappearAnimation);
			
			removeClound();
			icon.setBackgroundResource(R.drawable.floating_desktop_tips_rocket_bg);
			createIcon();
		}

	}

	/**
	 * 用于获取状态栏的高度。
	 * 
	 * @return 返回状态栏高度的像素值。
	 */
	public static int getStatusBarHeight(Context context) {
		int statusBarHeight = 0;
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object o = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = (Integer) field.get(o);
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
	}
}
